package com.company.resourceallocation.service;

import com.company.resourceallocation.dto.request.EmployeeRequest;
import com.company.resourceallocation.dto.response.EmployeeResponse;
import com.company.resourceallocation.dto.response.WorkloadResponse;
import com.company.resourceallocation.entity.Employee;
import com.company.resourceallocation.exception.EmployeeCodeAlreadyExistsException;
import com.company.resourceallocation.exception.EmployeeNotFoundException;
import com.company.resourceallocation.repository.EmployeeRepository;
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

/** Unit test cho {@link EmployeeService}. */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    @DisplayName("Create: thành công khi employeeCode chưa tồn tại")
    void create_success() {
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            ReflectionTestUtils.setField(e, "id", 1L);
            return e;
        });

        EmployeeResponse res = employeeService.create(
                new EmployeeRequest("EMP001", "Tuan Ho Anh", "tuan@company.com", "Dev", "Q1"));

        assertThat(res.employeeId()).isEqualTo(1L);
        assertThat(res.employeeCode()).isEqualTo("EMP001");
    }

    @Test
    @DisplayName("Create: trùng employeeCode ném EmployeeCodeAlreadyExistsException, không lưu")
    void create_duplicateCode_throws() {
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(
                new EmployeeRequest("EMP001", "Dup", "dup@company.com", null, null)))
                .isInstanceOf(EmployeeCodeAlreadyExistsException.class);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("FindById: id không tồn tại ném EmployeeNotFoundException")
    void findById_notFound_throws() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(99L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    @DisplayName("Workload: totalAllocation = SUM, available = 100 - total")
    void getWorkload_computesAvailable() {
        Employee employee = new Employee("EMP001", "Tuan Ho Anh", "tuan@company.com", "Dev", "Q1");
        ReflectionTestUtils.setField(employee, "id", 1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.sumAllocationByEmployee(1L)).thenReturn(80);

        WorkloadResponse res = employeeService.getWorkload(1L);

        assertThat(res.totalAllocation()).isEqualTo(80);
        assertThat(res.available()).isEqualTo(20);
        assertThat(res.employeeName()).isEqualTo("Tuan Ho Anh");
    }

    @Test
    @DisplayName("Workload: nhân viên chưa có allocation -> total 0, available 100")
    void getWorkload_noAllocation() {
        Employee employee = new Employee("EMP002", "Nam Nguyen", "nam@company.com", null, null);
        ReflectionTestUtils.setField(employee, "id", 2L);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(employeeRepository.sumAllocationByEmployee(2L)).thenReturn(0);

        WorkloadResponse res = employeeService.getWorkload(2L);

        assertThat(res.totalAllocation()).isZero();
        assertThat(res.available()).isEqualTo(100);
    }
}
