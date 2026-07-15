package com.company.resourceallocation.exception;

import org.springframework.http.HttpStatus;

/**
 * BR3 — không cho phân bổ/sửa allocation vào Project đã COMPLETED.
 * HTTP 409, code PROJECT_NOT_ACTIVE (§10.3 PRD).
 */
public class ProjectNotActiveException extends ApiException {

    public ProjectNotActiveException(Long projectId) {
        super(HttpStatus.CONFLICT, "PROJECT_NOT_ACTIVE",
                "Cannot allocate to a COMPLETED project: id=" + projectId);
    }
}
