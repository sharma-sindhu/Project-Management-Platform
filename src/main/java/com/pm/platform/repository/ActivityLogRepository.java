package com.pm.platform.repository;

import com.pm.platform.domain.ActivityLogEntity;
import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, String> {

    @Query(
            """
            SELECT a FROM ActivityLogEntity a
            JOIN a.issue i
            WHERE i.project = :project
            ORDER BY a.createdAt DESC
            """)
    Page<ActivityLogEntity> findByProject(@Param("project") ProjectEntity project, Pageable pageable);

    Page<ActivityLogEntity> findByIssueOrderByCreatedAtDesc(IssueEntity issue, Pageable pageable);
}
