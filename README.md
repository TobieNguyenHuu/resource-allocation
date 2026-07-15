# Project Resource Allocation Management System

Hệ thống quản lý phân bổ nhân sự vào dự án. Backend REST API (Java 17 + Spring Boot), cưỡng chế bất biến: **tổng allocation của một nhân viên không bao giờ vượt 100%**.

> Tài liệu kế hoạch (PRD, Architecture, Epics) nằm ở:
> `BMAD-METHOD-main/_bmad-output/planning-artifacts/prds/prd-resource-allocation-2026-07-15/`

## Tech Stack

- Java 17 (biên dịch được trên JDK 21)
- Spring Boot **3.5.16** (Spring Web, Spring Data JPA, Validation, Actuator)
  > Lưu ý: dòng Spring Boot 3.5 đã EOL 30/06/2026 (không còn patch OSS mới). Chọn vì ổn định + tài liệu dày cho mục đích học tập; có thể nâng lên 4.1.x nếu cần bản còn được hỗ trợ.
- PostgreSQL 16
- Maven

## Business Rules (lõi)

- **BR1**: `0 < allocationPercent <= 100`.
- **BR2**: tổng allocation của một nhân viên `<= 100%` (pessimistic lock chống race).
- **BR3**: không cho phân bổ/sửa vào dự án `COMPLETED`.

## Chạy dự án

### Cách 1 — Docker Compose (khuyến nghị)

```bash
docker compose up --build
```

App: http://localhost:8080 · Health: http://localhost:8080/actuator/health · Swagger UI: http://localhost:8080/swagger-ui.html

## API docs & Postman

- **Swagger UI**: http://localhost:8080/swagger-ui.html — tài liệu API tương tác (OpenAPI: `/v3/api-docs`).
- **Postman**: import `postman/ResourceAllocation.postman_collection.json` (biến `baseUrl` = http://localhost:8080).

### Cách 2 — Chạy local (cần PostgreSQL sẵn)

```bash
# Tạo DB 'resource_allocation' trên PostgreSQL localhost:5432 (user/pass: postgres/postgres)
mvn spring-boot:run
```

Cấu hình kết nối override bằng biến môi trường: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

## Database

Schema tạo bằng script `src/main/resources/db/schema.sql` (deliverable), tự chạy khi khởi động (`spring.sql.init`). JPA đặt `ddl-auto: validate` — Hibernate không sinh DDL, chỉ kiểm tra ánh xạ.

## Trạng thái triển khai (theo Epic)

- [x] **Epic 1 — Foundation**: Spring Boot init, PostgreSQL + SQL schema, Global Exception Handler + error envelope, Docker Compose.
- [x] **Epic 2 — Employee Management**: CRUD nhân viên + endpoint workload (FR-1,2,3).
- [x] **Epic 3 — Project Management**: CRUD dự án + cập nhật status (FR-4,5,6).
- [x] **Epic 4 — Resource Allocation Core**: Create/Update/Delete với BR1–BR3, pessimistic lock (race-safe), exclude-old-value (FR-7,8,9,10). Verified toàn bộ §4.3.A + edge-case §11.
- [x] **Epic 5 — Reporting**: utilization / available / overloaded (FR-11,12,13) — GROUP BY / HAVING.
- [~] **Epic 6 — Bonus**: [x] Swagger/OpenAPI (Story 6.1); [ ] AI Recommendation (6.2); [ ] AI Risk Detection (6.3).
