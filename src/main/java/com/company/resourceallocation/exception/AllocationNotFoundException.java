package com.company.resourceallocation.exception;

import org.springframework.http.HttpStatus;

/** Không tìm thấy Allocation theo id → HTTP 404, code ALLOCATION_NOT_FOUND (§10.3 PRD). */
public class AllocationNotFoundException extends ApiException {

    public AllocationNotFoundException(Long allocationId) {
        super(HttpStatus.NOT_FOUND, "ALLOCATION_NOT_FOUND", "Allocation not found: id=" + allocationId);
    }
}
