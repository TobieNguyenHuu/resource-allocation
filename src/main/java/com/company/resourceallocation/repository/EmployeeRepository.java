package com.company.resourceallocation.repository;

import com.company.resourceallocation.entity.Employee;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository cho {@link Employee}.
 *
 * <p>{@code sumAllocationByEmployee} phục vụ FR-3 (workload). Ở Epic 2 dùng native query
 * trên bảng {@code allocation} (chưa có Allocation entity — sẽ tạo ở Epic 4). Đây là
 * lối tính Total Allocation theo A1 (SUM đơn giản), có COALESCE để trả 0 khi chưa có allocation.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeCode(String employeeCode);

    /**
     * Nạp Employee kèm KHÓA GHI dòng (SELECT ... FOR UPDATE) — cơ chế concurrency cho BR2 (AR-3, E10).
     * Gọi TRƯỚC khi tính SUM trong create/update allocation để tuần tự hóa mọi thay đổi
     * allocation của cùng một nhân viên.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Employee e WHERE e.id = :id")
    Optional<Employee> findByIdForUpdate(@Param("id") Long id);

    @Query(value = "SELECT COALESCE(SUM(allocation_percent), 0) FROM allocation WHERE employee_id = :employeeId",
            nativeQuery = true)
    int sumAllocationByEmployee(@Param("employeeId") Long employeeId);
}
