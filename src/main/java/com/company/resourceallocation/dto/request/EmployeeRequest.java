package com.company.resourceallocation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload tạo Employee (FR-1). Bean Validation cưỡng chế shape:
 * employeeCode/fullName/email bắt buộc, email đúng định dạng.
 */
public record EmployeeRequest(
        @NotBlank String employeeCode,
        @NotBlank String fullName,
        @NotBlank @Email String email,
        String role,
        String department
) {
}
