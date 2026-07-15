package com.company.resourceallocation.controller;

import com.company.resourceallocation.dto.request.EmployeeRequest;
import com.company.resourceallocation.dto.response.EmployeeResponse;
import com.company.resourceallocation.dto.response.WorkloadResponse;
import com.company.resourceallocation.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API cho Employee (FR-1, FR-2, FR-3). Không chứa business logic. */
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(request);
    }

    @GetMapping
    public List<EmployeeResponse> list() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @GetMapping("/{id}/workload")
    public WorkloadResponse getWorkload(@PathVariable Long id) {
        return employeeService.getWorkload(id);
    }
}
