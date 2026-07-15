package com.company.resourceallocation.dto.request;

import com.company.resourceallocation.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Payload tạo Project (FR-4). {@code status} tuỳ chọn — mặc định PLANNING nếu null.
 * Giá trị status không hợp lệ bị Jackson từ chối khi deserialize → 400 (MALFORMED_REQUEST).
 */
public record ProjectRequest(
        @NotBlank String projectCode,
        @NotBlank String projectName,
        String customer,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status
) {
}
