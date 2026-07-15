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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link AllocationService} — tầng chứa toàn bộ Business Rule BR1–BR3.
 * Mock các repository nên không cần database.
 */
@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {

    @Mock
    private AllocationRepository allocationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private AllocationService allocationService;

    private Employee employee;
    private Project activeProject;
    private Project completedProject;

    @BeforeEach
    void setUp() {
        employee = new Employee("EMP001", "Tuan Ho Anh", "tuan@company.com", "Dev", "Q1");
        ReflectionTestUtils.setField(employee, "id", 1L);

        activeProject = new Project("NCG", "NCG Platform", "NCG Corp", null, null, ProjectStatus.ACTIVE);
        ReflectionTestUtils.setField(activeProject, "id", 2L);

        completedProject = new Project("OLD", "Old Project", null, null, null, ProjectStatus.COMPLETED);
        ReflectionTestUtils.setField(completedProject, "id", 3L);
    }

    // ---------- CREATE ----------

    @Test
    @DisplayName("Create: hợp lệ khi tổng đạt đúng 100% (boundary)")
    void create_success_atBoundary() {
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(activeProject));
        when(allocationRepository.existsByEmployeeIdAndProjectId(1L, 2L)).thenReturn(false);
        when(allocationRepository.sumAllocationByEmployee(1L)).thenReturn(50);
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(inv -> {
            Allocation a = inv.getArgument(0);
            ReflectionTestUtils.setField(a, "id", 10L);
            return a;
        });

        AllocationResponse res = allocationService.create(
                new CreateAllocationRequest(1L, 2L, 50, "Backend Developer", null, null));

        assertThat(res.allocationId()).isEqualTo(10L);
        assertThat(res.allocationPercent()).isEqualTo(50);
        verify(allocationRepository).save(any(Allocation.class));
    }

    @Test
    @DisplayName("Create: BR2 — tổng vượt 100% ném AllocationExceededException, không lưu")
    void create_exceeds100_throws() {
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(activeProject));
        when(allocationRepository.existsByEmployeeIdAndProjectId(1L, 2L)).thenReturn(false);
        when(allocationRepository.sumAllocationByEmployee(1L)).thenReturn(50);

        assertThatThrownBy(() -> allocationService.create(
                new CreateAllocationRequest(1L, 2L, 60, "Dev", null, null)))
                .isInstanceOf(AllocationExceededException.class)
                .satisfies(ex -> {
                    var details = ((AllocationExceededException) ex).getDetails();
                    assertThat(details).containsEntry("currentAllocation", 50);
                    assertThat(details).containsEntry("requestedAllocation", 60);
                    assertThat(details).containsEntry("resultingAllocation", 110);
                });

        verify(allocationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create: BR3 — dự án COMPLETED ném ProjectNotActiveException")
    void create_completedProject_throws() {
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(3L)).thenReturn(Optional.of(completedProject));

        assertThatThrownBy(() -> allocationService.create(
                new CreateAllocationRequest(1L, 3L, 10, "Dev", null, null)))
                .isInstanceOf(ProjectNotActiveException.class);

        verify(allocationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create: A3 — trùng cặp (employee, project) ném DuplicateAllocationException")
    void create_duplicate_throws() {
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(activeProject));
        when(allocationRepository.existsByEmployeeIdAndProjectId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> allocationService.create(
                new CreateAllocationRequest(1L, 2L, 10, "Dev", null, null)))
                .isInstanceOf(DuplicateAllocationException.class);
    }

    @Test
    @DisplayName("Create: employee không tồn tại ném EmployeeNotFoundException")
    void create_employeeNotFound_throws() {
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocationService.create(
                new CreateAllocationRequest(1L, 2L, 10, "Dev", null, null)))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    @DisplayName("Create: project không tồn tại ném ProjectNotFoundException")
    void create_projectNotFound_throws() {
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocationService.create(
                new CreateAllocationRequest(1L, 2L, 10, "Dev", null, null)))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    // ---------- UPDATE ----------

    @Test
    @DisplayName("Update (E1): 50->70 khi đang full — loại trừ giá trị cũ, vẫn vượt -> 409, giữ nguyên")
    void update_excludeOldValue_stillExceeds() {
        Allocation alloc = new Allocation(employee, activeProject, 50, "Dev", null, null);
        ReflectionTestUtils.setField(alloc, "id", 5L);
        when(allocationRepository.findById(5L)).thenReturn(Optional.of(alloc));
        // employee đang có tổng 100% (bản ghi này 50 + bản ghi khác 50)
        when(allocationRepository.sumAllocationByEmployee(1L)).thenReturn(100);

        assertThatThrownBy(() -> allocationService.update(5L, new UpdateAllocationRequest(70, null)))
                .isInstanceOf(AllocationExceededException.class)
                .satisfies(ex ->
                        // otherTotal = 100 - 50 = 50 (đã loại trừ giá trị cũ)
                        assertThat(((AllocationExceededException) ex).getDetails())
                                .containsEntry("currentAllocation", 50));

        assertThat(alloc.getAllocationPercent()).isEqualTo(50); // không đổi
    }

    @Test
    @DisplayName("Update: giảm 50->40 hợp lệ")
    void update_reduce_success() {
        Allocation alloc = new Allocation(employee, activeProject, 50, "Dev", null, null);
        ReflectionTestUtils.setField(alloc, "id", 5L);
        when(allocationRepository.findById(5L)).thenReturn(Optional.of(alloc));
        when(allocationRepository.sumAllocationByEmployee(1L)).thenReturn(100);

        AllocationResponse res = allocationService.update(5L, new UpdateAllocationRequest(40, null));

        assertThat(res.allocationPercent()).isEqualTo(40);
        assertThat(alloc.getAllocationPercent()).isEqualTo(40);
    }

    @Test
    @DisplayName("Update: BR3 — allocation thuộc dự án COMPLETED ném ProjectNotActiveException")
    void update_completedProject_throws() {
        Allocation alloc = new Allocation(employee, completedProject, 30, "Dev", null, null);
        ReflectionTestUtils.setField(alloc, "id", 6L);
        when(allocationRepository.findById(6L)).thenReturn(Optional.of(alloc));

        assertThatThrownBy(() -> allocationService.update(6L, new UpdateAllocationRequest(40, null)))
                .isInstanceOf(ProjectNotActiveException.class);

        assertThat(alloc.getAllocationPercent()).isEqualTo(30); // không đổi
    }

    @Test
    @DisplayName("Update: id không tồn tại ném AllocationNotFoundException")
    void update_notFound_throws() {
        when(allocationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocationService.update(99L, new UpdateAllocationRequest(30, null)))
                .isInstanceOf(AllocationNotFoundException.class);
    }

    // ---------- DELETE ----------

    @Test
    @DisplayName("Delete: thành công gọi repository.delete")
    void delete_success() {
        Allocation alloc = new Allocation(employee, activeProject, 30, "Dev", null, null);
        ReflectionTestUtils.setField(alloc, "id", 7L);
        when(allocationRepository.findById(7L)).thenReturn(Optional.of(alloc));

        allocationService.delete(7L);

        verify(allocationRepository).delete(alloc);
    }

    @Test
    @DisplayName("Delete: id không tồn tại ném AllocationNotFoundException, không xoá")
    void delete_notFound_throws() {
        when(allocationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocationService.delete(99L))
                .isInstanceOf(AllocationNotFoundException.class);

        verify(allocationRepository, never()).delete(any());
    }
}
