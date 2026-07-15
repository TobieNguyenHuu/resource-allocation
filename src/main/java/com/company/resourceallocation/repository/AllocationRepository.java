package com.company.resourceallocation.repository;

import com.company.resourceallocation.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository cho {@link Allocation}.
 *
 * <p>{@code sumAllocationByEmployee} là nền tảng kiểm tra BR2 (Total Allocation, A1 = SUM đơn giản).
 * COALESCE trả 0 khi nhân viên chưa có allocation.
 */
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    boolean existsByEmployeeIdAndProjectId(Long employeeId, Long projectId);

    @Query("SELECT COALESCE(SUM(a.allocationPercent), 0) FROM Allocation a WHERE a.employee.id = :employeeId")
    int sumAllocationByEmployee(@Param("employeeId") Long employeeId);
}
