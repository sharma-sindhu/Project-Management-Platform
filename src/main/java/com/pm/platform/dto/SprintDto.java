package com.pm.platform.dto;

import com.pm.platform.domain.SprintState;
import java.time.LocalDate;

public record SprintDto(
        String sprint_id,
        String name,
        LocalDate start_date,
        LocalDate end_date,
        SprintState state,
        Integer velocity_story_points) {}
