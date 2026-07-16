# AI Review Report — Resource Allocation Management System

Báo cáo quá trình phát triển hệ thống với sự hỗ trợ của AI (Claude Code), theo yêu cầu "AI-assisted Development" của đề bài.

---

## 1. Tổng quan

Hệ thống Backend (Spring Boot + PostgreSQL + Docker) được xây dựng theo quy trình **AI-assisted, layer-by-layer, có kiểm chứng thực tế ở từng bước** thay vì viết code hàng loạt rồi mới chạy. Quy trình đi từ **PRD → Architecture → Epics → code → verify**; mỗi Epic sau khi hoàn thành đều được build/chạy thật trong Docker và test bằng curl để xác nhận trước khi sang phần kế tiếp.

## 2. Các thành phần được xây dựng

| Thành phần | Nội dung |
|-----------|----------|
| Entity (JPA) | 3 entity (`Employee`, `Project`, `Allocation`) + enum `ProjectStatus`, map vào schema PostgreSQL tạo bằng SQL script |
| Repository | Spring Data JPA + query aggregate (`SUM`, `GROUP BY`, `HAVING`, `JOIN`) + query **pessimistic lock** (`SELECT ... FOR UPDATE`) |
| DTO | Java `record` cho request/response + mapping thủ công (tách entity khỏi API) |
| Service | 3 service, 3 business rule, `@Transactional`, logging Create/Update/Remove |
| Exception | 8 custom exception + `@RestControllerAdvice` global handler + error envelope thống nhất |
| Controller | REST API (14 endpoint) + Bean Validation |
| Bonus | Swagger (springdoc-openapi 2.8.17), Docker Compose, Unit test (22 test, Mockito) |

## 3. Kiểm chứng (verification)

- **End-to-end API**: toàn bộ kịch bản PASS — chạy thật với server + PostgreSQL thật trong Docker: CRUD Employee/Project/Allocation, BR1/BR2/BR3, workload, 3 report (utilization/available/overloaded), và exception handling.
- **Race condition (BR2)**: bắn 2 request tạo allocation đồng thời (mỗi cái 60%) cho cùng nhân viên → đúng **1×201 + 1×409**, tổng không vượt 100% (pessimistic lock hoạt động, không lost update).
- **Unit test**: **22/22 PASS** (business rules với Mockito, không cần database).
- **Docker**: `docker compose up --build` dựng full stack thành công; app ghi được vào DB container (cách ly hoàn toàn với DB local), health check `/actuator/health` trả UP.

## 4. Vấn đề phát hiện & xử lý trong quá trình phát triển

Những điểm AI phát hiện và xử lý (đáng chú ý về mặt nghiệp vụ và concurrency):

1. **BR2 là ràng buộc xuyên bản ghi** — AI chỉ ra rằng "tổng allocation ≤ 100%" **không thể** kiểm bằng annotation (`@Max`) vì phải cộng nhiều dòng; phải kiểm ở tầng Service sau khi truy vấn `SUM`.

2. **Race condition khi tạo allocation đồng thời** — chỉ dùng `@Transactional` là chưa đủ: hai request song song đều đọc tổng = 50% rồi cùng thêm 60% → 170%. AI đề xuất **pessimistic lock** (`SELECT ... FOR UPDATE`) trên dòng Employee để tuần tự hóa; đã verify bằng 2 request đồng thời.

3. **Bẫy "double-counting" khi update allocation** — khi sửa % của một allocation, tổng dùng để kiểm BR2 phải **loại trừ giá trị cũ** của chính bản ghi đó, nếu không sẽ đếm hai lần và chặn sai (ví dụ 50%→70% khi đang full bị chặn đúng, nhưng nếu quên trừ 50 cũ sẽ tính nhầm thành 100+70).

4. **Chọn phiên bản Spring Boot** — bản mới nhất là 4.1.0 (nền Spring Framework 7), nhưng chọn **3.5.16** (bản 3.5.x cuối) vì ổn định và tài liệu dày cho mục đích học tập; ghi rõ tradeoff trong README.

5. **springdoc-openapi đúng dòng** — Spring Boot 3.5 cần **springdoc 2.8.x**, không phải 3.x (3.x dành cho Spring Boot 4).

6. **Xử lý JSON/enum không hợp lệ** — ban đầu giá trị enum sai (vd `status: "ONGOING"`) rơi vào handler `Exception` chung → 500. Đã thêm handler `HttpMessageNotReadableException` → trả **400 `MALFORMED_REQUEST`** theo đúng error envelope.

7. **Khởi tạo schema an toàn** — dùng SQL script `schema.sql` (deliverable) chạy qua `spring.sql.init` với `CREATE TABLE IF NOT EXISTS` (idempotent), kết hợp `ddl-auto=validate` để Hibernate chỉ đối chiếu ánh xạ chứ không tự sinh DDL.

8. **Thứ tự khởi động Docker** — container `app` khai báo `depends_on: db (condition: service_healthy)` để chờ PostgreSQL sẵn sàng (health check `pg_isready`) trước khi kết nối, tránh lỗi khởi động sớm.
