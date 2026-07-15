package com.company.resourceallocation.service;

import com.company.resourceallocation.dto.request.EmployeeRequest;
import com.company.resourceallocation.dto.response.EmployeeResponse;
import com.company.resourceallocation.dto.response.WorkloadResponse;
import com.company.resourceallocation.entity.Employee;
import com.company.resourceallocation.exception.EmployeeCodeAlreadyExistsException;
import com.company.resourceallocation.exception.EmployeeNotFoundException;
import com.company.resourceallocation.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service quản lý Employee (FR-1, FR-2, FR-3).
 * Business logic sống ở tầng này (NFR-1); Controller chỉ điều phối.
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest req) {
        if (employeeRepository.existsByEmployeeCode(req.employeeCode())) {
            throw new EmployeeCodeAlreadyExistsException(req.employeeCode());
        }
        Employee saved = employeeRepository.save(new Employee(
                req.employeeCode(), req.fullName(), req.email(), req.role(), req.department()));
        return EmployeeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream().map(EmployeeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return employeeRepository.findById(id)
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public WorkloadResponse getWorkload(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        int total = employeeRepository.sumAllocationByEmployee(id);
        return new WorkloadResponse(employee.getId(), employee.getFullName(), total, 100 - total);
    }
}
