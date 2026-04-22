package com.pm.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProjectDto(
        @JsonProperty("project_id") String project_id,
        String key,
        String name,
        String description) {}
