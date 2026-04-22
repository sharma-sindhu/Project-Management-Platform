package com.pm.platform.web;

import com.pm.platform.dto.CreateProjectRequest;
import com.pm.platform.dto.ProjectDto;
import com.pm.platform.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectRootController {

    private final ProjectService projectService;

    public ProjectRootController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** Creates a project, default workflow (To Do → In Progress → In Review → Done), and issue counter. */
    @PostMapping
    public ProjectDto createProject(@Valid @RequestBody CreateProjectRequest body) {
        return projectService.createProject(body);
    }
}
