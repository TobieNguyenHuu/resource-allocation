package com.company.resourceallocation.controller;

import com.company.resourceallocation.dto.request.CreateAllocationRequest;
import com.company.resourceallocation.dto.request.UpdateAllocationRequest;
import com.company.resourceallocation.dto.response.AllocationResponse;
import com.company.resourceallocation.service.AllocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST API cho Allocation (FR-7, FR-9, FR-10). Không chứa business logic. */
@RestController
@RequestMapping("/allocations")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AllocationResponse create(@Valid @RequestBody CreateAllocationRequest request) {
        return allocationService.create(request);
    }

    @PutMapping("/{id}")
    public AllocationResponse update(@PathVariable Long id,
                                     @Valid @RequestBody UpdateAllocationRequest request) {
        return allocationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        allocationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
