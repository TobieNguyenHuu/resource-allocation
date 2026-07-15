package com.company.resourceallocation.exception;

import org.springframework.http.HttpStatus;

/** Trùng projectCode → HTTP 409, code PROJECT_CODE_EXISTS. */
public class ProjectCodeAlreadyExistsException extends ApiException {

    public ProjectCodeAlreadyExistsException(String projectCode) {
        super(HttpStatus.CONFLICT, "PROJECT_CODE_EXISTS",
                "Project code already exists: " + projectCode);
    }
}
