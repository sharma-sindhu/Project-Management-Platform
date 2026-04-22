package com.pm.platform.dto;

import com.pm.platform.domain.IssueType;
import com.pm.platform.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateIssueRequest(
        @NotNull IssueType type,
        @NotBlank String title,
        String description,
        Priority priority,
        String assignee_user_id,
        @NotNull String reporter_user_id,
        String sprint_id,
        String parent_issue_id,
        Integer story_points,
        List<String> labels,
        String initial_status_name) {}
