package com.pm.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record TransitionRequest(
        @NotBlank String to_status_name,
        String actor_user_id,
        /** Optional reviewer assignment when workflow defines it */
        String override_reviewer_id) {}
