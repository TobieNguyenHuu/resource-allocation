package com.company.resourceallocation.repository;

import com.company.resourceallocation.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository cho {@link Project}. */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByProjectCode(String projectCode);
}
