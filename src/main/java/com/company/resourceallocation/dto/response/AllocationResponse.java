package com.company.resourceallocation.dto.response;

import com.company.resourceallocation.entity.Allocation;

import java.time.LocalDate;

/** Response biểu diễn một Allocation. */
public record AllocationResponse(
        Long allocationId,
        Long employeeId,
        Long projectId,
        Integer allocationPercent,
        String roleInProject,
        LocalDate startDate,
        LocalDate endDate
) {
    public static AllocationResponse from(Allocation a) {
        return new AllocationResponse(
                a.getId(),
                a.getEmployee().getId(),
                a.getProject().getId(),
                a.getAllocationPercent(),
                a.getRoleInProject(),
                a.getStartDate(),
                a.getEndDate()
        );
    }
}
