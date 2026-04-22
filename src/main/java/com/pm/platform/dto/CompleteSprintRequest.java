package com.pm.platform.dto;

import java.util.List;

public record CompleteSprintRequest(
        /** Incomplete issues to move to target sprint (carry-over) */
        List<String> carry_over_issue_ids,
        String target_sprint_id,
        /** Issues to move back to backlog instead */
        List<String> move_to_backlog_issue_ids) {}
