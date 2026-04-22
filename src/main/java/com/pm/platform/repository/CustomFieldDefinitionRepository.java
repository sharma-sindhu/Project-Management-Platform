package com.pm.platform.repository;

import com.pm.platform.domain.CustomFieldDefinitionEntity;
import com.pm.platform.domain.ProjectEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFieldDefinitionRepository extends JpaRepository<CustomFieldDefinitionEntity, String> {

    List<CustomFieldDefinitionEntity> findByProject(ProjectEntity project);
}
