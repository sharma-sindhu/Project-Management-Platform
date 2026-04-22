package com.pm.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Issue payload aligned with assignment sample")
public record IssueDto(
        /** Row primary key; use in URLs such as `/api/issues/{id}`. */
        @JsonProperty("id") @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") String id,
        /** Human-readable key, e.g. `DEMO-1`. */
        @JsonProperty("issue_id") @Schema(example = "PROJ-123") String issue_id,
        @JsonProperty("project_id") @Schema(example = "proj_abc") String project_id,
        String type,
        String title,
        String description,
        String status,
        String priority,
        UserRefDto assignee,
        UserRefDto reporter,
        SprintRefDto sprint,
        List<String> labels,
        @JsonProperty("story_points") Integer story_points,
        @JsonProperty("parent_id") String parent_id,
        List<String> watchers,
        @JsonProperty("created_at") Instant created_at,
        @JsonProperty("updated_at") Instant updated_at,
        @JsonProperty("version") Long version) {}
