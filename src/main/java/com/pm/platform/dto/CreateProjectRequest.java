package com.pm.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        /** Short code for issue keys, e.g. `DEMO` → `DEMO-1`. Unique (case-insensitive). */
        @NotBlank @Size(max = 32) String key,
        @NotBlank String name,
        String description,
        /** Optional primary key; when omitted the server assigns one. */
        @JsonProperty("project_id") String project_id) {}
