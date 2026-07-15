package com.company.resourceallocation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Payload cập nhật Allocation (FR-9). BR1 vẫn áp dụng: {@code 0 < allocationPercent <= 100}.
 * {@code roleInProject} tuỳ chọn — null nghĩa là giữ nguyên.
 */
public record UpdateAllocationRequest(
        @NotNull @Min(1) @Max(100) Integer allocationPercent,
        String roleInProject
) {
}
