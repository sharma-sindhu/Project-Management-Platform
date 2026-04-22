package com.pm.platform.dto;

import java.util.List;

public record SearchResultDto(String next_cursor, List<IssueDto> items) {}
