package com.company.resourceallocation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Allocation — bản ghi phân bổ một {@link Employee} vào một {@link Project} với một tỷ lệ %.
 * Thực thể trung tâm mang toàn bộ Business Rule (BR1–BR3). Ánh xạ bảng {@code allocation}.
 */
@Entity
@Table(name = "allocation")
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "allocation_percent", nullable = false)
    private Integer allocationPercent;

    @Column(name = "role_in_project")
    private String roleInProject;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    protected Allocation() {
        // JPA
    }

    public Allocation(Employee employee, Project project, Integer allocationPercent,
                      String roleInProject, LocalDate startDate, LocalDate endDate) {
        this.employee = employee;
        this.project = project;
        this.allocationPercent = allocationPercent;
        this.roleInProject = roleInProject;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Project getProject() {
        return project;
    }

    public Integer getAllocationPercent() {
        return allocationPercent;
    }

    public void setAllocationPercent(Integer allocationPercent) {
        this.allocationPercent = allocationPercent;
    }

    public String getRoleInProject() {
        return roleInProject;
    }

    public void setRoleInProject(String roleInProject) {
        this.roleInProject = roleInProject;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
