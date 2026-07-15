package com.company.resourceallocation.entity;

/**
 * Trạng thái vòng đời của Project.
 * COMPLETED là dữ liệu điều khiển BR3: không cho phân bổ/sửa allocation vào dự án đã đóng.
 */
public enum ProjectStatus {
    PLANNING,
    ACTIVE,
    COMPLETED
}
