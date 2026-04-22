package com.pm.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @JsonProperty("display_name") String display_name,
        /** Optional; when omitted the server assigns a unique string id. */
        @JsonProperty("user_id") String user_id) {}
