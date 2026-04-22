package com.pm.platform.dto;

import java.util.List;

public record BoardColumnDto(String status_id, String status_name, List<IssueDto> issues) {}
