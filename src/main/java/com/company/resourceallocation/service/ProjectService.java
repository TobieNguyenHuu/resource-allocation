package com.company.resourceallocation.service;

import com.company.resourceallocation.dto.request.ProjectRequest;
import com.company.resourceallocation.dto.request.UpdateProjectStatusRequest;
import com.company.resourceallocation.dto.response.ProjectResponse;
import com.company.resourceallocation.entity.Project;
import com.company.resourceallocation.entity.ProjectStatus;
import com.company.resourceallocation.exception.ProjectCodeAlreadyExistsException;
import com.company.resourceallocation.exception.ProjectNotFoundException;
import com.company.resourceallocation.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Service quản lý Project (FR-4, FR-5, FR-6). */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponse create(ProjectRequest req) {
        if (projectRepository.existsByProjectCode(req.projectCode())) {
            throw new ProjectCodeAlreadyExistsException(req.projectCode());
        }
        // status mặc định PLANNING nếu client không truyền.
        ProjectStatus status = req.status() != null ? req.status() : ProjectStatus.PLANNING;
        Project saved = projectRepository.save(new Project(
                req.projectCode(), req.projectName(), req.customer(),
                req.startDate(), req.endDate(), status));
        return ProjectResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream().map(ProjectResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        return projectRepository.findById(id)
                .map(ProjectResponse::from)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @Transactional
    public ProjectResponse updateStatus(Long id, UpdateProjectStatusRequest req) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        project.setStatus(req.status());
        // Không xoá allocation hiện có khi chuyển COMPLETED (Story 3.3 AC).
        return ProjectResponse.from(project); // dirty-checking tự flush
    }
}
