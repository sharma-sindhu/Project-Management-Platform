package com.pm.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDto(
        @JsonProperty("user_id") String user_id,
        String email,
        @JsonProperty("display_name") String display_name) {}
