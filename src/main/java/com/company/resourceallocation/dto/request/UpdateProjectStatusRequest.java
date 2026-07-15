package com.company.resourceallocation.dto.request;

import com.company.resourceallocation.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;

/** Payload cập nhật trạng thái Project (FR-6). */
public record UpdateProjectStatusRequest(
        @NotNull ProjectStatus status
) {
}
