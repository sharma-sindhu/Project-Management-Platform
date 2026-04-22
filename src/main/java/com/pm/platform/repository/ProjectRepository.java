package com.pm.platform.repository;

import com.pm.platform.domain.ProjectEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    Optional<ProjectEntity> findByKeyIgnoreCase(String key);
}
