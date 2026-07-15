package com.company.resourceallocation.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Envelope lỗi thống nhất toàn hệ thống (§10 PRD).
 * Sinh bởi {@link GlobalExceptionHandler}. Các trường null bị lược khỏi JSON.
 *
 * <pre>
 * {
 *   "timestamp": "2026-07-15T10:30:00Z",
 *   "status": 409,
 *   "error": "Conflict",
 *   "code": "ALLOCATION_EXCEEDED",
 *   "message": "Employee allocation exceeds 100%",
 *   "path": "/allocations",
 *   "details": { ... },       // tuỳ chọn (vd AllocationExceeded)
 *   "fieldErrors": [ ... ]    // tuỳ chọn (lỗi validation 400)
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Map<String, Object> details,
        List<FieldError> fieldErrors
) {
    /** Một lỗi cấp field cho response validation (400). */
    public record FieldError(String field, Object rejectedValue, String reason) {
    }

    public static ApiError of(int status, String error, String code, String message, String path) {
        return new ApiError(Instant.now(), status, error, code, message, path, null, null);
    }

    public static ApiError of(int status, String error, String code, String message, String path,
                              Map<String, Object> details) {
        return new ApiError(Instant.now(), status, error, code, message, path, details, null);
    }

    public static ApiError validation(int status, String error, String code, String message, String path,
                                      List<FieldError> fieldErrors) {
        return new ApiError(Instant.now(), status, error, code, message, path, null, fieldErrors);
    }
}
