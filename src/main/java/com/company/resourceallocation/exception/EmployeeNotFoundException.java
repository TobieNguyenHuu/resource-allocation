package com.company.resourceallocation.exception;

import org.springframework.http.HttpStatus;

/** Không tìm thấy Employee theo id → HTTP 404, code EMPLOYEE_NOT_FOUND (§10.3 PRD). */
public class EmployeeNotFoundException extends ApiException {

    public EmployeeNotFoundException(Long employeeId) {
        super(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "Employee not found: id=" + employeeId);
    }
}
