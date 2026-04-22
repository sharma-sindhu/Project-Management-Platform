package com.pm.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User reference")
public record UserRefDto(
        @Schema(example = "jane") String user_id,
        @Schema(example = "Jane Smith") String display_name) {

    public static UserRefDto of(String id, String displayName) {
        return new UserRefDto(id, displayName);
    }
}
