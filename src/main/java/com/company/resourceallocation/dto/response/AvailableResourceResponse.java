package com.company.resourceallocation.dto.response;

/** Một dòng báo cáo resource khả dụng (FR-12): phần trăm còn trống của một nhân viên. */
public record AvailableResourceResponse(
        Long employeeId,
        String employeeCode,
        String fullName,
        int available
) {
}
