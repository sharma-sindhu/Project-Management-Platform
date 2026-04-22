package com.pm.platform.repository;

import com.pm.platform.domain.ProjectEntity;
import com.pm.platform.domain.WorkflowStatusEntity;
import com.pm.platform.domain.WorkflowTransitionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransitionEntity, String> {

    List<WorkflowTransitionEntity> findByProjectAndFromStatus(ProjectEntity project, WorkflowStatusEntity fromStatus);

    Optional<WorkflowTransitionEntity> findByProjectAndFromStatusAndToStatus(
            ProjectEntity project, WorkflowStatusEntity from, WorkflowStatusEntity to);
}
