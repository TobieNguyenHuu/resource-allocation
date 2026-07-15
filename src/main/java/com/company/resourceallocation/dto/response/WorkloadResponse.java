package com.company.resourceallocation.dto.response;

/**
 * Response workload của một Employee (FR-3).
 * {@code totalAllocation} = SUM(allocationPercent); {@code available} = 100 - totalAllocation.
 */
public record WorkloadResponse(
        Long employeeId,
        String employeeName,
        int totalAllocation,
        int available
) {
}
