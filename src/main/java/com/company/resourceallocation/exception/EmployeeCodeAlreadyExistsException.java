package com.company.resourceallocation.exception;

import org.springframework.http.HttpStatus;

/** Trùng employeeCode → HTTP 409, code EMPLOYEE_CODE_EXISTS. */
public class EmployeeCodeAlreadyExistsException extends ApiException {

    public EmployeeCodeAlreadyExistsException(String employeeCode) {
        super(HttpStatus.CONFLICT, "EMPLOYEE_CODE_EXISTS",
                "Employee code already exists: " + employeeCode);
    }
}
