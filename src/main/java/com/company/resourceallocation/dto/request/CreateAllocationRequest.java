package com.company.resourceallocation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Payload tạo Allocation (FR-7).
 * Bean Validation cưỡng chế BR1 tại tầng ngoài: {@code 0 < allocationPercent <= 100}.
 */
public record CreateAllocationRequest(
        @NotNull Long employeeId,
        @NotNull Long projectId,
        @NotNull @Min(1) @Max(100) Integer allocationPercent,
        @NotBlank String roleInProject,
        LocalDate startDate,
        LocalDate endDate
) {
}
