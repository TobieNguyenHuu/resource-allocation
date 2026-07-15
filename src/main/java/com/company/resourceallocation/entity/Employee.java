package com.company.resourceallocation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Nhân viên — thực thể có thể được phân bổ vào nhiều Project.
 * Ánh xạ bảng {@code employee} (schema.sql). Định danh nghiệp vụ: {@code employeeCode} (unique).
 */
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true)
    private String employeeCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "role")
    private String role;

    @Column(name = "department")
    private String department;

    protected Employee() {
        // JPA
    }

    public Employee(String employeeCode, String fullName, String email, String role, String department) {
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }
}
