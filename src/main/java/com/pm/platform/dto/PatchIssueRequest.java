package com.pm.platform.dto;

import com.pm.platform.domain.Priority;
import java.util.List;

public record PatchIssueRequest(
        String title,
        String description,
        Priority priority,
        String assignee_user_id,
        String sprint_id,
        String parent_issue_id,
        Integer story_points,
        List<String> labels,
        Long expected_version) {}
