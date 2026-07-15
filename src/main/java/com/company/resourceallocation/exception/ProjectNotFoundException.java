package com.company.resourceallocation.exception;

import org.springframework.http.HttpStatus;

/** Không tìm thấy Project theo id → HTTP 404, code PROJECT_NOT_FOUND (§10.3 PRD). */
public class ProjectNotFoundException extends ApiException {

    public ProjectNotFoundException(Long projectId) {
        super(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "Project not found: id=" + projectId);
    }
}
