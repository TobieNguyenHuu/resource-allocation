package com.company.resourceallocation.service;

import com.company.resourceallocation.dto.request.ProjectRequest;
import com.company.resourceallocation.dto.request.UpdateProjectStatusRequest;
import com.company.resourceallocation.dto.response.ProjectResponse;
import com.company.resourceallocation.entity.Project;
import com.company.resourceallocation.entity.ProjectStatus;
import com.company.resourceallocation.exception.ProjectCodeAlreadyExistsException;
import com.company.resourceallocation.exception.ProjectNotFoundException;
import com.company.resourceallocation.repository.ProjectRepository;
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

/** Unit test cho {@link ProjectService}. */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("Create: status null -> mặc định PLANNING")
    void create_defaultStatusPlanning() {
        when(projectRepository.existsByProjectCode("NCG")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 1L);
            return p;
        });

        ProjectResponse res = projectService.create(
                new ProjectRequest("NCG", "NCG Platform", "NCG Corp", null, null, null));

        assertThat(res.status()).isEqualTo(ProjectStatus.PLANNING);
    }

    @Test
    @DisplayName("Create: trùng projectCode ném ProjectCodeAlreadyExistsException")
    void create_duplicateCode_throws() {
        when(projectRepository.existsByProjectCode("NCG")).thenReturn(true);

        assertThatThrownBy(() -> projectService.create(
                new ProjectRequest("NCG", "Dup", null, null, null, null)))
                .isInstanceOf(ProjectCodeAlreadyExistsException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("UpdateStatus: chuyển ACTIVE -> COMPLETED thành công")
    void updateStatus_success() {
        Project project = new Project("NCG", "NCG Platform", null, null, null, ProjectStatus.ACTIVE);
        ReflectionTestUtils.setField(project, "id", 1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectResponse res = projectService.updateStatus(1L,
                new UpdateProjectStatusRequest(ProjectStatus.COMPLETED));

        assertThat(res.status()).isEqualTo(ProjectStatus.COMPLETED);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    @DisplayName("UpdateStatus: id không tồn tại ném ProjectNotFoundException")
    void updateStatus_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateStatus(99L,
                new UpdateProjectStatusRequest(ProjectStatus.ACTIVE)))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    @DisplayName("FindById: id không tồn tại ném ProjectNotFoundException")
    void findById_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(99L))
                .isInstanceOf(ProjectNotFoundException.class);
    }
}
