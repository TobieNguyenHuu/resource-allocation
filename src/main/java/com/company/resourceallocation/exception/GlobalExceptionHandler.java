package com.company.resourceallocation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global Exception Handler (§10 PRD, NFR-4).
 *
 * <p>Chuyển mọi exception thành {@link ApiError} với đúng HTTP status + {@code code}.
 * Lỗi nghiệp vụ (4xx) log mức WARN; lỗi hệ thống (5xx) log mức ERROR.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Mọi exception nghiệp vụ (ApiException và lớp con). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        log.warn("{} [{}] tại {}: {}", ex.getClass().getSimpleName(), ex.getCode(),
                request.getRequestURI(), ex.getMessage());
        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                ex.getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getDetails()
        );
        return ResponseEntity.status(status).body(body);
    }

    /** Bean Validation fail trên @RequestBody (@Valid) → 400 VALIDATION_ERROR + fieldErrors. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()))
                .toList();
        log.warn("Validation failed tại {}: {} field", request.getRequestURI(), fieldErrors.size());
        ApiError body = ApiError.validation(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_ERROR",
                "Request validation failed",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    /** Body không đọc được: JSON hỏng hoặc giá trị enum không hợp lệ (vd status sai) → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex,
                                                      HttpServletRequest request) {
        log.warn("Malformed request body tại {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "MALFORMED_REQUEST",
                "Malformed request body or invalid field value",
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    /** Vi phạm ràng buộc toàn vẹn DB (unique/FK/check) — safety net khi qua được pre-check → 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        log.warn("Data integrity violation tại {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        ApiError body = ApiError.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "DATA_INTEGRITY_VIOLATION",
                "Request violates a data integrity constraint",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /** Lỗi không lường trước → 500 INTERNAL_ERROR. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không lường trước tại {}", request.getRequestURI(), ex);
        ApiError body = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(body);
    }
}
