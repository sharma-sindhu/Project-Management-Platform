package com.pm.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "custom_field_definitions")
public class CustomFieldDefinitionEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private CustomFieldType fieldType;

    /** JSON array of options for DROPDOWN */
    @Column(name = "options_json", length = 2000)
    private String optionsJson;

    protected CustomFieldDefinitionEntity() {}

    public CustomFieldDefinitionEntity(
            String id, ProjectEntity project, String name, CustomFieldType fieldType, String optionsJson) {
        this.id = id;
        this.project = project;
        this.name = name;
        this.fieldType = fieldType;
        this.optionsJson = optionsJson;
    }

    public String getId() {
        return id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public String getName() {
        return name;
    }

    public CustomFieldType getFieldType() {
        return fieldType;
    }

    public String getOptionsJson() {
        return optionsJson;
    }
}
