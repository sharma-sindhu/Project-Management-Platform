package com.pm.platform.dto;

import java.util.List;

public record ApiErrorDto(String error, String message, List<String> allowed_transitions) {}
