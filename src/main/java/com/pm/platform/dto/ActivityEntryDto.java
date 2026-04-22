package com.pm.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record ActivityEntryDto(
        String id,
        @JsonProperty("issue_id") String issue_id,
        UserRefDto actor,
        String action,
        String payload,
        @JsonProperty("created_at") Instant created_at) {}
