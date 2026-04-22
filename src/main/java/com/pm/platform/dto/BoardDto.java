package com.pm.platform.dto;

import java.util.List;

public record BoardDto(String project_id, List<BoardColumnDto> columns) {}
