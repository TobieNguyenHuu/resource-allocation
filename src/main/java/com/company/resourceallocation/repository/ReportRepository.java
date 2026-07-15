package com.company.resourceallocation.repository;

import com.company.resourceallocation.entity.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Truy vấn báo cáo (Epic 5) dùng aggregate GROUP BY / HAVING trên Employee LEFT/INNER JOIN Allocation.
 * Trả {@code Object[]} (id, employeeCode, fullName, total) — Service map sang DTO tương ứng.
 */
public interface ReportRepository extends Repository<Employee, Long> {

    /** FR-11: tổng allocation của TỪNG nhân viên (kể cả người chưa có allocation → 0). */
    @Query("""
            SELECT e.id, e.employeeCode, e.fullName, COALESCE(SUM(a.allocationPercent), 0)
            FROM Employee e LEFT JOIN Allocation a ON a.employee = e
            GROUP BY e.id, e.employeeCode, e.fullName
            ORDER BY e.id
            """)
    List<Object[]> utilization();

    /** FR-12: nhân viên còn trống (tổng < 100). HAVING lọc ngay trong SQL. */
    @Query("""
            SELECT e.id, e.employeeCode, e.fullName, COALESCE(SUM(a.allocationPercent), 0)
            FROM Employee e LEFT JOIN Allocation a ON a.employee = e
            GROUP BY e.id, e.employeeCode, e.fullName
            HAVING COALESCE(SUM(a.allocationPercent), 0) < 100
            ORDER BY e.id
            """)
    List<Object[]> available();

    /** FR-13: nhân viên quá tải (tổng > 90). INNER JOIN vì phải có allocation; HAVING > 90. */
    @Query("""
            SELECT e.id, e.employeeCode, e.fullName, SUM(a.allocationPercent)
            FROM Employee e JOIN Allocation a ON a.employee = e
            GROUP BY e.id, e.employeeCode, e.fullName
            HAVING SUM(a.allocationPercent) > 90
            ORDER BY e.id
            """)
    List<Object[]> overloaded();
}
