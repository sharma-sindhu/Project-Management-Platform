package com.pm.platform.repository;

import com.pm.platform.domain.CustomFieldDefinitionEntity;
import com.pm.platform.domain.CustomFieldValueEntity;
import com.pm.platform.domain.IssueEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValueEntity, String> {

    List<CustomFieldValueEntity> findByIssue(IssueEntity issue);

    Optional<CustomFieldValueEntity> findByIssueAndDefinition(IssueEntity issue, CustomFieldDefinitionEntity def);
}
