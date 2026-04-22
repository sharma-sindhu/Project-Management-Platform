package com.pm.platform.repository;

import com.pm.platform.domain.ProjectEntity;
import com.pm.platform.domain.WorkflowStatusEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStatusRepository extends JpaRepository<WorkflowStatusEntity, String> {

    List<WorkflowStatusEntity> findByProjectOrderBySortOrderAsc(ProjectEntity project);

    Optional<WorkflowStatusEntity> findByProjectAndNameIgnoreCase(ProjectEntity project, String name);
}
