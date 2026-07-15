package com.company.resourceallocation.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Lớp cơ sở cho mọi exception nghiệp vụ của hệ thống.
 *
 * <p>Mỗi exception mang sẵn {@link HttpStatus} và một {@code code} máy-đọc-được
 * (UPPER_SNAKE, bảng §10.3 PRD), tuỳ chọn thêm {@code details} cho envelope.
 * {@link GlobalExceptionHandler} chỉ cần một handler duy nhất cho toàn bộ nhánh này.
 *
 * <p>Các exception cụ thể (EmployeeNotFound, AllocationExceeded, ProjectNotActive...)
 * được thêm ở Epic 4 bằng cách kế thừa lớp này.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final transient Map<String, Object> details;

    protected ApiException(HttpStatus status, String code, String message) {
        this(status, code, message, null);
    }

    protected ApiException(HttpStatus status, String code, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
