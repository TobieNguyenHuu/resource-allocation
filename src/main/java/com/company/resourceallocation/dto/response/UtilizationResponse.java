package com.company.resourceallocation.dto.response;

/** Một dòng báo cáo utilization (FR-11): tổng allocation của một nhân viên. */
public record UtilizationResponse(
        Long employeeId,
        String employeeCode,
        String fullName,
        int totalAllocation
) {
}
