package com.pm.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record CommentDto(
        String id,
        @JsonProperty("issue_id") String issue_id,
        UserRefDto author,
        String body,
        @JsonProperty("parent_id") String parent_id,
        @JsonProperty("created_at") Instant created_at,
        List<String> mentioned_user_ids) {}
