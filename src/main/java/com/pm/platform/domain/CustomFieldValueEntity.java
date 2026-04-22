package com.pm.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "custom_field_values")
public class CustomFieldValueEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private IssueEntity issue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id", nullable = false)
    private CustomFieldDefinitionEntity definition;

    @Column(name = "field_value", length = 2000)
    private String fieldValue;

    protected CustomFieldValueEntity() {}

    public CustomFieldValueEntity(
            String id, IssueEntity issue, CustomFieldDefinitionEntity definition, String fieldValue) {
        this.id = id;
        this.issue = issue;
        this.definition = definition;
        this.fieldValue = fieldValue;
    }

    public String getId() {
        return id;
    }

    public IssueEntity getIssue() {
        return issue;
    }

    public CustomFieldDefinitionEntity getDefinition() {
        return definition;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}
