package com.company.resourceallocation.exception;

import org.springframework.http.HttpStatus;

/**
 * A3 — đã tồn tại Allocation cho cặp (employee, project). Muốn đổi % phải dùng Update.
 * HTTP 409, code DUPLICATE_ALLOCATION (§10.3 PRD).
 */
public class DuplicateAllocationException extends ApiException {

    public DuplicateAllocationException(Long employeeId, Long projectId) {
        super(HttpStatus.CONFLICT, "DUPLICATE_ALLOCATION",
                "Allocation already exists for employeeId=" + employeeId + ", projectId=" + projectId);
    }
}
