-- ============================================================================
-- Project Resource Allocation Management System — Database Schema
-- Deliverable §9 PRD. Chạy tự động khi khởi động (spring.sql.init) và cũng có thể
-- chạy tay bằng psql. Idempotent (IF NOT EXISTS) để khởi động lại nhiều lần an toàn.
-- ============================================================================

CREATE TABLE IF NOT EXISTS employee (
    employee_id   BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(20)  NOT NULL UNIQUE,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL,
    role          VARCHAR(50),
    department    VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS project (
    project_id   BIGSERIAL PRIMARY KEY,
    project_code VARCHAR(20)  NOT NULL UNIQUE,
    project_name VARCHAR(200) NOT NULL,
    customer     VARCHAR(100),
    start_date   DATE,
    end_date     DATE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PLANNING'
                 CHECK (status IN ('PLANNING', 'ACTIVE', 'COMPLETED'))
);

CREATE TABLE IF NOT EXISTS allocation (
    allocation_id      BIGSERIAL PRIMARY KEY,
    employee_id        BIGINT  NOT NULL REFERENCES employee(employee_id),
    project_id         BIGINT  NOT NULL REFERENCES project(project_id),
    allocation_percent INTEGER NOT NULL
                       CHECK (allocation_percent > 0 AND allocation_percent <= 100), -- BR1 tại tầng DB
    role_in_project    VARCHAR(100),
    start_date         DATE,
    end_date           DATE,
    CONSTRAINT uq_alloc_emp_prj UNIQUE (employee_id, project_id)  -- A3: 1 allocation / cặp (employee, project)
);

-- Tối ưu truy vấn SUM(allocation_percent) theo nhân viên (BR2) và các báo cáo.
CREATE INDEX IF NOT EXISTS idx_alloc_employee ON allocation(employee_id);
