package com.pm.platform.repository;

import com.pm.platform.domain.CommentEntity;
import com.pm.platform.domain.IssueEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, String> {

    List<CommentEntity> findByIssueOrderByCreatedAtAsc(IssueEntity issue);
}
