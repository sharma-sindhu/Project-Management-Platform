package com.pm.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(@NotBlank String body, String parent_comment_id, @NotNull String author_user_id) {}
