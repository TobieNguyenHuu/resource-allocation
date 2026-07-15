package com.company.resourceallocation.dto.response;

/** Một dòng báo cáo nhân viên quá tải (FR-13): tổng allocation > 90%. */
public record OverloadedResponse(
        Long employeeId,
        String employeeCode,
        String fullName,
        int totalAllocation
) {
}
