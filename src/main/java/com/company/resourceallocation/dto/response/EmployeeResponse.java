package com.company.resourceallocation.dto.response;

import com.company.resourceallocation.entity.Employee;

/** Response biểu diễn một Employee (không lộ JPA Entity ra Controller — AR-5). */
public record EmployeeResponse(
        Long employeeId,
        String employeeCode,
        String fullName,
        String email,
        String role,
        String department
) {
    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(
                e.getId(),
                e.getEmployeeCode(),
                e.getFullName(),
                e.getEmail(),
                e.getRole(),
                e.getDepartment()
        );
    }
}
