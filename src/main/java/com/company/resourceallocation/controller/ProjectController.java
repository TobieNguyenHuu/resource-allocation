package com.company.resourceallocation.controller;

import com.company.resourceallocation.dto.request.ProjectRequest;
import com.company.resourceallocation.dto.request.UpdateProjectStatusRequest;
import com.company.resourceallocation.dto.response.ProjectResponse;
import com.company.resourceallocation.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API cho Project (FR-4, FR-5, FR-6). */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        return projectService.create(request);
    }

    @GetMapping
    public List<ProjectResponse> list() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @PutMapping("/{id}/status")
    public ProjectResponse updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody UpdateProjectStatusRequest request) {
        return projectService.updateStatus(id, request);
    }
}
