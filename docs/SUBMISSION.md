# Resource Allocation Management System — Báo cáo nộp bài (Tuần 1)

- **Học viên:** _[Điền họ tên đầy đủ]_
- **Mã/Số:** _[Điền số của bạn, ví dụ 3]_
- **Ngày:** 16/07/2026
- **GitHub repo:** _[Dán link repo sau khi push, ví dụ https://github.com/<user>/resource-allocation]_

---

## 1. Tổng quan

Hệ thống REST API quản lý phân bổ nhân sự vào dự án, xây dựng bằng **Java 17 + Spring Boot 3.5.16 + Spring Data JPA + PostgreSQL 16 + Maven**, đóng gói bằng **Docker Compose**. Bất biến nghiệp vụ cốt lõi: **tổng allocation của một nhân viên không bao giờ vượt 100%**.

## 2. Cách chạy

```bash
git clone <repo-url>
cd resource-allocation
docker compose up --build
```
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

## 3. Chức năng đã hoàn thành (FR-1 → FR-13)

| Module | Chức năng | API |
|--------|-----------|-----|
| Employee | Tạo / xem / danh sách / workload | `POST,GET /employees`, `GET /employees/{id}/workload` |
| Project | Tạo / xem / cập nhật status | `POST,GET /projects`, `PUT /projects/{id}/status` |
| Allocation | Tạo / sửa / xoá (BR1–BR3) | `POST /allocations`, `PUT,DELETE /allocations/{id}` |
| Report | Utilization / Available / Overloaded | `GET /reports/*` |

## 4. Business Rules (điểm cốt lõi)

- **BR1** — `0 < allocationPercent <= 100`: cưỡng chế 2 tầng (Bean Validation `@Min/@Max` + CHECK constraint ở DB).
- **BR2** — tổng allocation mỗi nhân viên `<= 100%`: kiểm ở Service layer (không thể dùng annotation vì là ràng buộc xuyên bản ghi). Chống race condition bằng **pessimistic lock** (`SELECT ... FOR UPDATE`) trên Employee.
- **BR3** — không cho phân bổ/sửa vào dự án `COMPLETED`.
- **Edge case xử lý:** update allocation **loại trừ giá trị cũ** trước khi kiểm BR2 (tránh double-counting); xoá cho phép cả trên dự án COMPLETED.

## 5. Thiết kế kỹ thuật

- Kiến trúc phân tầng: **Controller → Service → Repository**; Business Rule chỉ nằm ở Service.
- **Error envelope thống nhất** (`@RestControllerAdvice`): mọi lỗi trả `{timestamp, status, error, code, message, path}`, riêng lỗi vượt 100% có thêm `details`.
- DTO ⇄ Entity (không lộ JPA Entity ra Controller).
- Schema tạo bằng **SQL script** (`src/main/resources/db/schema.sql`), JPA `ddl-auto: validate`.

## 6. Kiểm thử (đã verify end-to-end)

Toàn bộ kịch bản đã test thật bằng curl/Postman trên app chạy trong Docker:

| Kịch bản | Kết quả |
|----------|---------|
| Tạo allocation hợp lệ | 201 |
| Tổng vượt 100% (BR2) | 409 `ALLOCATION_EXCEEDED` + details |
| allocationPercent = 0 (BR1) | 400 `VALIDATION_ERROR` |
| Phân bổ vào dự án COMPLETED (BR3) | 409 `PROJECT_NOT_ACTIVE` |
| Update 50%→70% khi đã full (exclude-old-value) | 409, giữ nguyên bản ghi |
| Trùng cặp (employee, project) | 409 `DUPLICATE_ALLOCATION` |
| 2 request đồng thời (race) | Đúng 1×201 + 1×409, tổng không vượt 100% |
| Báo cáo utilization/available/overloaded | Đúng dữ liệu (GROUP BY/HAVING) |

## 7. Deliverables

- [x] Source Code (GitHub) — _link ở đầu file_
- [x] SQL Script (`db/schema.sql`)
- [x] README.md
- [x] Postman Collection (`postman/ResourceAllocation.postman_collection.json`)
- [x] API Screenshot (`docs/screenshots/`)
- [x] AI Review Report (mục 8 dưới đây)

## 8. AI Review Report (phát triển với AI)

Dự án được phát triển theo quy trình **AI-assisted (BMAD)** với Claude:

- **Planning:** AI tạo PRD chi tiết (phân tích Business Logic, Validation Flow, edge-case, error contract) → Architecture (chọn pessimistic lock cho BR2, thiết kế tầng, schema) → Epics/Stories.
- **Đề xuất kỹ thuật đáng chú ý từ AI:**
  - Dùng **pessimistic lock trên Employee** thay vì chỉ `@Transactional` để chống race condition E10 (2 request đồng thời cùng vượt trần).
  - Cảnh báo bẫy **exclude-old-value** khi update allocation (nếu quên trừ giá trị cũ sẽ đếm hai lần → chặn sai).
  - **Defense-in-depth** ở DB: CHECK constraint (BR1) + UNIQUE (chống trùng) song song với kiểm tra ở Service.
  - Tách lớp cơ sở `ApiException` để mọi lỗi nghiệp vụ dùng chung một Global Exception Handler.
- **Verify:** AI tự chạy app trong Docker và test toàn bộ kịch bản BR1–BR3 + edge-case + race condition bằng curl, xác nhận kết quả trước khi kết luận hoàn thành.
- **Đánh giá:** cách tiếp cận AI giúp phát hiện sớm các rủi ro concurrency và edge-case mà nếu code tay thuần dễ bỏ sót; đồng thời đảm bảo tính nhất quán giữa PRD → Architecture → code.
