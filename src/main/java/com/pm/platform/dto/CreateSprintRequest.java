package com.pm.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pm.platform.domain.SprintState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateSprintRequest(
        @NotBlank String name,
        @NotNull @JsonProperty("start_date") LocalDate start_date,
        @NotNull @JsonProperty("end_date") LocalDate end_date,
        /** Defaults to `PLANNED` when omitted. */
        SprintState state,
        /** Optional; when omitted the server assigns a unique string id. */
        @JsonProperty("sprint_id") String sprint_id) {}
