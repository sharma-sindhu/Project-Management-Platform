package com.pm.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Sprint summary on an issue")
public record SprintRefDto(
        @Schema(example = "sprint-10") String sprint_id,
        String name,
        LocalDate start_date,
        LocalDate end_date) {

    public static SprintRefDto from(String id, String name, LocalDate start, LocalDate end) {
        return new SprintRefDto(id, name, start, end);
    }
}
