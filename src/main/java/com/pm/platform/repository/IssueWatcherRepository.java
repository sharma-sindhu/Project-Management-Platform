package com.pm.platform.repository;

import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.IssueWatcherEntity;
import com.pm.platform.domain.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueWatcherRepository extends JpaRepository<IssueWatcherEntity, IssueWatcherEntity.IssueWatcherId> {

    List<IssueWatcherEntity> findByIssue(IssueEntity issue);

    boolean existsByIssueAndUser(IssueEntity issue, UserEntity user);

    void deleteByIssueAndUser(IssueEntity issue, UserEntity user);
}
