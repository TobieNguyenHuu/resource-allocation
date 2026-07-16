-- ============================================================================
-- Dữ liệu mẫu (seed) cho Resource Allocation Management System.
-- Chạy tay để có sẵn data demo:  psql -d resource_allocation -f seed.sql
-- Idempotent: ON CONFLICT DO NOTHING nên chạy lại nhiều lần không lỗi/không trùng.
-- Tham chiếu employee/project qua mã (code) để không phụ thuộc id tự sinh.
-- ============================================================================

-- Nhân viên
INSERT INTO employee (employee_code, full_name, email, role, department) VALUES
    ('EMP001', 'Tuan Ho Anh',   'tuanha@company.com',  'Senior Developer', 'FSOFT-Q1'),
    ('EMP002', 'Nam Nguyen',    'nam@company.com',     'Developer',        'FSOFT-Q1'),
    ('EMP003', 'Lan Tran',      'lan@company.com',     'Tester',           'FSOFT-Q2')
ON CONFLICT (employee_code) DO NOTHING;

-- Dự án
INSERT INTO project (project_code, project_name, customer, start_date, end_date, status) VALUES
    ('NCG',  'NCG Platform',  'NCG Corp',      '2026-01-01', '2026-12-31', 'ACTIVE'),
    ('GRID', 'Grid System',   'Grid Inc',      '2026-02-01', '2026-10-31', 'ACTIVE'),
    ('AI',   'Internal AI',   'Internal',      '2026-03-01', NULL,          'PLANNING'),
    ('OLD',  'Legacy Migrate','Old Customer',  '2025-01-01', '2025-12-31', 'COMPLETED')
ON CONFLICT (project_code) DO NOTHING;

-- Allocation (tham chiếu qua code → an toàn với id tự sinh)
INSERT INTO allocation (employee_id, project_id, allocation_percent, role_in_project, start_date, end_date)
SELECT e.employee_id, p.project_id, 50, 'Backend Developer', '2026-01-01', '2026-12-31'
FROM employee e, project p
WHERE e.employee_code = 'EMP001' AND p.project_code = 'NCG'
ON CONFLICT (employee_id, project_id) DO NOTHING;

INSERT INTO allocation (employee_id, project_id, allocation_percent, role_in_project, start_date, end_date)
SELECT e.employee_id, p.project_id, 30, 'Backend Developer', '2026-02-01', '2026-10-31'
FROM employee e, project p
WHERE e.employee_code = 'EMP001' AND p.project_code = 'GRID'
ON CONFLICT (employee_id, project_id) DO NOTHING;

INSERT INTO allocation (employee_id, project_id, allocation_percent, role_in_project, start_date, end_date)
SELECT e.employee_id, p.project_id, 80, 'Tester', '2026-01-01', NULL
FROM employee e, project p
WHERE e.employee_code = 'EMP003' AND p.project_code = 'NCG'
ON CONFLICT (employee_id, project_id) DO NOTHING;
