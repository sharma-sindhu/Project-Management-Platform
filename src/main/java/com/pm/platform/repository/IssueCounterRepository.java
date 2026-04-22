package com.pm.platform.repository;

import com.pm.platform.domain.IssueCounterEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssueCounterRepository extends JpaRepository<IssueCounterEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM IssueCounterEntity c WHERE c.projectId = :projectId")
    Optional<IssueCounterEntity> findForUpdate(@Param("projectId") String projectId);
}
