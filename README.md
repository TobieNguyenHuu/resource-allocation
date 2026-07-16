# Resource Allocation Management System

Hệ thống REST API quản lý phân bổ nhân sự (resource allocation) vào các dự án cho công ty outsourcing. Một nhân viên có thể tham gia nhiều dự án với tỷ lệ phần trăm thời gian khác nhau; hệ thống cưỡng chế bất biến nghiệp vụ cốt lõi: **tổng allocation của một nhân viên không bao giờ vượt 100%**.

## Mục lục

- [Tính năng](#tính-năng)
- [Công nghệ](#công-nghệ)
- [Business Rules](#business-rules)
- [Kiến trúc](#kiến-trúc)
- [Chạy dự án](#chạy-dự-án)
- [API Endpoints](#api-endpoints)
- [Định dạng lỗi](#định-dạng-lỗi)
- [Database](#database)
- [Cấu trúc project](#cấu-trúc-project)
- [Kiểm thử](#kiểm-thử)

## Tính năng

- **Employee Management** — quản lý nhân viên và tra cứu workload (tổng % đã phân bổ, % còn trống).
- **Project Management** — quản lý dự án theo vòng đời trạng thái `PLANNING → ACTIVE → COMPLETED`.
- **Resource Allocation** — phân bổ / điều chỉnh / gỡ bỏ nhân sự khỏi dự án, cưỡng chế đầy đủ 3 Business Rule.
- **Reporting** — báo cáo utilization, resource khả dụng, và nhân viên quá tải bằng aggregate query.
- **Swagger/OpenAPI** — tài liệu API tương tác.

## Công nghệ

| Thành phần | Lựa chọn |
|-----------|----------|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.5.16 (Web, Data JPA, Validation, Actuator) |
| Database | PostgreSQL 16 |
| Build | Maven |
| Tài liệu API | springdoc-openapi (Swagger UI) |
| Triển khai | Docker & Docker Compose |

## Business Rules

| # | Quy tắc | Cưỡng chế ở đâu |
|---|---------|-----------------|
| **BR1** | `0 < allocationPercent <= 100` | Bean Validation (`@Min/@Max`) + CHECK constraint ở DB |
| **BR2** | Tổng allocation của một nhân viên `<= 100%` | Service layer + **pessimistic lock** trên Employee (chống race condition) |
| **BR3** | Không cho phân bổ / sửa allocation vào dự án `COMPLETED` | Service layer |

Xử lý edge-case đáng chú ý:
- Khi **cập nhật** allocation, tổng dùng để kiểm BR2 được tính **sau khi loại trừ giá trị cũ** của chính bản ghi đang sửa (tránh đếm hai lần).
- **Xoá** allocation được phép kể cả trên dự án `COMPLETED` (chỉ thao tác thêm/tăng mới bị BR3 chặn).

## Kiến trúc

Phân tầng nghiêm ngặt, Business Rule chỉ nằm ở tầng Service:

```
Controller  →  Service  →  Repository  →  PostgreSQL
   (REST)    (business)   (Spring Data JPA)
```

- **Validation 2 tầng**: Bean Validation cho shape/BR1; kiểm tra ở Service cho BR2/BR3 (ràng buộc xuyên bản ghi, không thể dùng annotation).
- **Error envelope thống nhất** qua `@RestControllerAdvice` — mọi lỗi trả về cùng một cấu trúc JSON.
- **DTO ⇄ Entity** — không lộ JPA Entity ra tầng Controller.
- **Concurrency**: `SELECT ... FOR UPDATE` (pessimistic write lock) trên dòng Employee bao trọn thao tác "tính tổng + ghi" trong một transaction, đảm bảo hai request đồng thời không thể cùng vượt trần 100%.

## Chạy dự án

### Cách 1 — Docker Compose (khuyến nghị)

```bash
docker compose up --build
```

Khởi động cả ứng dụng và PostgreSQL. Sau khi lên:

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health check: <http://localhost:8080/actuator/health>

Dừng: `docker compose down`

### Cách 2 — Chạy local (cần PostgreSQL sẵn)

```bash
# Tạo database 'resource_allocation' trên PostgreSQL localhost:5432 (user/pass mặc định postgres/postgres)
mvn spring-boot:run
```

Ghi đè thông tin kết nối bằng biến môi trường: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

## API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST` | `/employees` | Tạo nhân viên |
| `GET` | `/employees` | Danh sách nhân viên |
| `GET` | `/employees/{id}` | Chi tiết nhân viên |
| `GET` | `/employees/{id}/workload` | Workload (tổng allocation + % còn trống) |
| `POST` | `/projects` | Tạo dự án |
| `GET` | `/projects` | Danh sách dự án |
| `GET` | `/projects/{id}` | Chi tiết dự án |
| `PUT` | `/projects/{id}/status` | Cập nhật trạng thái dự án |
| `POST` | `/allocations` | Tạo allocation (BR1–BR3) |
| `PUT` | `/allocations/{id}` | Cập nhật allocation |
| `DELETE` | `/allocations/{id}` | Xoá allocation |
| `GET` | `/reports/utilization` | Tổng allocation từng nhân viên |
| `GET` | `/reports/available-resources?minAvailable=` | Nhân viên còn trống |
| `GET` | `/reports/overloaded` | Nhân viên quá tải (> 90%) |

Bộ sưu tập Postman: `postman/ResourceAllocation.postman_collection.json` (đặt biến `baseUrl = http://localhost:8080`).

## Định dạng lỗi

Mọi lỗi trả về theo một envelope thống nhất:

```json
{
  "timestamp": "2026-07-15T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "ALLOCATION_EXCEEDED",
  "message": "Employee allocation exceeds 100%",
  "path": "/allocations",
  "details": {
    "employeeId": 1,
    "currentAllocation": 95,
    "requestedAllocation": 10,
    "resultingAllocation": 105,
    "maxAllocation": 100
  }
}
```

| HTTP | `code` | Khi nào |
|------|--------|---------|
| 400 | `VALIDATION_ERROR` | Vi phạm Bean Validation (kèm `fieldErrors`) |
| 400 | `MALFORMED_REQUEST` | JSON hỏng hoặc giá trị enum không hợp lệ |
| 404 | `EMPLOYEE_NOT_FOUND` / `PROJECT_NOT_FOUND` / `ALLOCATION_NOT_FOUND` | Không tìm thấy tài nguyên |
| 409 | `ALLOCATION_EXCEEDED` | Tổng allocation vượt 100% (BR2) |
| 409 | `PROJECT_NOT_ACTIVE` | Phân bổ / sửa vào dự án COMPLETED (BR3) |
| 409 | `DUPLICATE_ALLOCATION` | Trùng cặp (employee, project) |
| 409 | `EMPLOYEE_CODE_EXISTS` / `PROJECT_CODE_EXISTS` | Trùng mã |

## Database

Schema được tạo bằng script `src/main/resources/db/schema.sql`, tự chạy khi khởi động (`spring.sql.init`). JPA đặt `ddl-auto: validate` — Hibernate không sinh DDL, chỉ kiểm tra ánh xạ Entity ⇄ bảng. Dữ liệu mẫu: `src/main/resources/db/seed.sql` (chạy tay `psql -d resource_allocation -f seed.sql`).

Ba bảng: `employee`, `project`, `allocation`. Ràng buộc defense-in-depth: `CHECK (allocation_percent > 0 AND <= 100)` (BR1), `UNIQUE (employee_id, project_id)` (chống trùng), khoá ngoại, và index `idx_alloc_employee` tối ưu truy vấn tổng theo nhân viên.

## Cấu trúc project

```
src/main/java/com/company/resourceallocation/
├── controller/   # REST endpoints (Employee, Project, Allocation, Report)
├── service/      # Business logic (BR1–BR3, pessimistic lock)
├── repository/   # Spring Data JPA repositories
├── entity/       # JPA entities (Employee, Project, Allocation, ProjectStatus)
├── dto/          # Request / Response DTOs
├── exception/    # Global exception handler + error envelope
└── config/       # OpenAPI (Swagger) config
src/main/resources/
├── application.yml
└── db/schema.sql
```

## Kiểm thử

**Unit test** (JUnit 5 + Mockito, không cần database — mock repository):

```bash
mvn test
```

22 test bao phủ tầng Service — nơi chứa toàn bộ Business Rule:
- `AllocationServiceTest` — BR1/BR2/BR3, loại trừ giá trị cũ khi update (edge-case), trùng lặp, not-found, xoá.
- `EmployeeServiceTest` — tạo, trùng mã, tính workload.
- `ProjectServiceTest` — mặc định trạng thái, trùng mã, cập nhật trạng thái, not-found.

Ngoài ra toàn bộ Business Rule và edge-case đã được kiểm chứng **end-to-end** trên ứng dụng chạy trong Docker (bao gồm cả race condition với hai request đồng thời). Xem báo cáo phát triển tại `AI-Review-Report.md`.
