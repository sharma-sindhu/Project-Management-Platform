package com.pm.platform.repository;

import com.pm.platform.domain.ProjectEntity;
import com.pm.platform.domain.SprintEntity;
import com.pm.platform.domain.SprintState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepository extends JpaRepository<SprintEntity, String> {

    List<SprintEntity> findByProjectOrderByStartDateDesc(ProjectEntity project);

    Optional<SprintEntity> findByProjectAndState(ProjectEntity project, SprintState state);
}
