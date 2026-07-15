package com.company.resourceallocation.dto.response;

import com.company.resourceallocation.entity.Project;
import com.company.resourceallocation.entity.ProjectStatus;

import java.time.LocalDate;

/** Response biểu diễn một Project. */
public record ProjectResponse(
        Long projectId,
        String projectCode,
        String projectName,
        String customer,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status
) {
    public static ProjectResponse from(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getProjectCode(),
                p.getProjectName(),
                p.getCustomer(),
                p.getStartDate(),
                p.getEndDate(),
                p.getStatus()
        );
    }
}
