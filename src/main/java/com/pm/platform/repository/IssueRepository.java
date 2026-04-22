package com.pm.platform.repository;

import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.ProjectEntity;
import com.pm.platform.domain.SprintEntity;
import com.pm.platform.domain.WorkflowStatusEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IssueRepository extends JpaRepository<IssueEntity, String>, JpaSpecificationExecutor<IssueEntity> {

    List<IssueEntity> findByProjectAndStatusOrderByIssueNumberAsc(ProjectEntity project, WorkflowStatusEntity status);

    List<IssueEntity> findByProjectAndSprint(ProjectEntity project, SprintEntity sprint);

    List<IssueEntity> findByProjectAndSprintIsNull(ProjectEntity project);

    Optional<IssueEntity> findByProjectAndIssueNumber(ProjectEntity project, int issueNumber);
}
