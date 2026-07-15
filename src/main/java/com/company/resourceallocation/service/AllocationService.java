package com.company.resourceallocation.service;

import com.company.resourceallocation.dto.request.CreateAllocationRequest;
import com.company.resourceallocation.dto.request.UpdateAllocationRequest;
import com.company.resourceallocation.dto.response.AllocationResponse;
import com.company.resourceallocation.entity.Allocation;
import com.company.resourceallocation.entity.Employee;
import com.company.resourceallocation.entity.Project;
import com.company.resourceallocation.entity.ProjectStatus;
import com.company.resourceallocation.exception.AllocationExceededException;
import com.company.resourceallocation.exception.AllocationNotFoundException;
import com.company.resourceallocation.exception.DuplicateAllocationException;
import com.company.resourceallocation.exception.EmployeeNotFoundException;
import com.company.resourceallocation.exception.ProjectNotActiveException;
import com.company.resourceallocation.exception.ProjectNotFoundException;
import com.company.resourceallocation.repository.AllocationRepository;
import com.company.resourceallocation.repository.EmployeeRepository;
import com.company.resourceallocation.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service lõi — hiện thực toàn bộ Business Rule BR1–BR3 của phân bổ nguồn lực (FR-7, FR-9, FR-10).
 *
 * <p>Mọi thao tác ghi chạy trong {@code @Transactional} và KHÓA GHI dòng Employee
 * ({@code findByIdForUpdate}) TRƯỚC khi tính SUM — tuần tự hóa mọi thay đổi allocation của
 * cùng một nhân viên, diệt tận gốc race condition E10 (BR2).
 */
@Service
public class AllocationService {

    private static final Logger log = LoggerFactory.getLogger(AllocationService.class);
    private static final int MAX_ALLOCATION = 100;

    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    public AllocationService(AllocationRepository allocationRepository,
                             EmployeeRepository employeeRepository,
                             ProjectRepository projectRepository) {
        this.allocationRepository = allocationRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Tạo Allocation (FR-7). Thứ tự kiểm tra đúng Validation Flow §4.3.A (fail-fast, rẻ trước — đắt sau).
     * BR1 đã được cưỡng chế ở tầng Bean Validation (Controller) trước khi vào đây.
     */
    @Transactional
    public AllocationResponse create(CreateAllocationRequest req) {
        // Bước 1: nạp + KHÓA Employee (SELECT ... FOR UPDATE)
        Employee employee = employeeRepository.findByIdForUpdate(req.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(req.employeeId()));

        // Bước 2: nạp Project
        Project project = projectRepository.findById(req.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(req.projectId()));

        // Bước 3 (BR3): chặn dự án COMPLETED
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new ProjectNotActiveException(project.getId());
        }

        // Bước 4 (A3): chặn trùng cặp (employee, project)
        if (allocationRepository.existsByEmployeeIdAndProjectId(employee.getId(), project.getId())) {
            throw new DuplicateAllocationException(employee.getId(), project.getId());
        }

        // Bước 5–6 (BR2): tính tổng (đã an toàn nhờ khóa) và kiểm trần 100%
        int currentTotal = allocationRepository.sumAllocationByEmployee(employee.getId());
        if (currentTotal + req.allocationPercent() > MAX_ALLOCATION) {
            throw new AllocationExceededException(employee.getId(), currentTotal, req.allocationPercent());
        }

        // Bước 7: ghi + log
        Allocation saved = allocationRepository.save(new Allocation(
                employee, project, req.allocationPercent(), req.roleInProject(),
                req.startDate(), req.endDate()));
        log.info("Create Allocation: allocationId={} employeeId={} projectId={} percent={}",
                saved.getId(), employee.getId(), project.getId(), req.allocationPercent());
        return AllocationResponse.from(saved);
    }

    /**
     * Cập nhật Allocation (FR-9). Điểm mấu chốt (E1): tổng dùng để kiểm BR2 phải LOẠI TRỪ
     * giá trị cũ của chính bản ghi đang sửa — nếu không sẽ đếm hai lần và chặn sai.
     */
    @Transactional
    public AllocationResponse update(Long allocationId, UpdateAllocationRequest req) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new AllocationNotFoundException(allocationId));

        // KHÓA Employee của allocation này trước khi tính SUM
        employeeRepository.findByIdForUpdate(allocation.getEmployee().getId());

        // BR3: không cho sửa allocation thuộc dự án đã COMPLETED
        if (allocation.getProject().getStatus() == ProjectStatus.COMPLETED) {
            throw new ProjectNotActiveException(allocation.getProject().getId());
        }

        // BR2 với exclude-old-value
        int total = allocationRepository.sumAllocationByEmployee(allocation.getEmployee().getId());
        int otherTotal = total - allocation.getAllocationPercent(); // ← loại trừ giá trị cũ
        if (otherTotal + req.allocationPercent() > MAX_ALLOCATION) {
            throw new AllocationExceededException(
                    allocation.getEmployee().getId(), otherTotal, req.allocationPercent());
        }

        int oldPercent = allocation.getAllocationPercent();
        allocation.setAllocationPercent(req.allocationPercent());
        if (req.roleInProject() != null) {
            allocation.setRoleInProject(req.roleInProject());
        }
        log.info("Update Allocation: allocationId={} old={} new={}",
                allocationId, oldPercent, req.allocationPercent());
        return AllocationResponse.from(allocation); // dirty-checking tự flush
    }

    /**
     * Xoá Allocation (FR-10). Cho phép kể cả khi Project đã COMPLETED (chỉ thêm/tăng mới bị BR3 chặn).
     */
    @Transactional
    public void delete(Long allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new AllocationNotFoundException(allocationId));
        Long employeeId = allocation.getEmployee().getId();
        int percent = allocation.getAllocationPercent();
        allocationRepository.delete(allocation);
        log.info("Remove Allocation: allocationId={} employeeId={} percent={}",
                allocationId, employeeId, percent);
    }
}
