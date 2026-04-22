package com.pm.platform.service;

import com.pm.platform.domain.IssueCounterEntity;
import com.pm.platform.domain.ProjectEntity;
import com.pm.platform.domain.WorkflowStatusEntity;
import com.pm.platform.domain.WorkflowTransitionEntity;
import com.pm.platform.dto.CreateProjectRequest;
import com.pm.platform.dto.ProjectDto;
import com.pm.platform.repository.IssueCounterRepository;
import com.pm.platform.repository.ProjectRepository;
import com.pm.platform.repository.WorkflowStatusRepository;
import com.pm.platform.repository.WorkflowTransitionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final IssueCounterRepository issueCounterRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            IssueCounterRepository issueCounterRepository,
            WorkflowStatusRepository workflowStatusRepository,
            WorkflowTransitionRepository workflowTransitionRepository) {
        this.projectRepository = projectRepository;
        this.issueCounterRepository = issueCounterRepository;
        this.workflowStatusRepository = workflowStatusRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
    }

    @Transactional
    public ProjectDto createProject(CreateProjectRequest req) {
        if (projectRepository.findByKeyIgnoreCase(req.key().trim()).isPresent()) {
            throw new IllegalArgumentException("Project key already in use: " + req.key());
        }
        String id =
                req.project_id() != null && !req.project_id().isBlank()
                        ? req.project_id().trim()
                        : UUID.randomUUID().toString();
        if (projectRepository.existsById(id)) {
            throw new IllegalArgumentException("project_id already exists: " + id);
        }
        String description = req.description() == null ? "" : req.description();
        ProjectEntity project = new ProjectEntity(id, req.key().trim(), req.name().trim(), description);
        projectRepository.save(project);

        issueCounterRepository.save(new IssueCounterEntity(id));

        String p = id;
        WorkflowStatusEntity todo = new WorkflowStatusEntity(p + ":st-todo", project, "To Do", 0);
        WorkflowStatusEntity inProg = new WorkflowStatusEntity(p + ":st-inprogress", project, "In Progress", 1);
        WorkflowStatusEntity inReview = new WorkflowStatusEntity(p + ":st-inreview", project, "In Review", 2);
        WorkflowStatusEntity done = new WorkflowStatusEntity(p + ":st-done", project, "Done", 3);
        workflowStatusRepository.save(todo);
        workflowStatusRepository.save(inProg);
        workflowStatusRepository.save(inReview);
        workflowStatusRepository.save(done);

        workflowTransitionRepository.save(
                new WorkflowTransitionEntity(UUID.randomUUID().toString(), project, todo, inProg, null));
        workflowTransitionRepository.save(
                new WorkflowTransitionEntity(UUID.randomUUID().toString(), project, inProg, inReview, null));
        workflowTransitionRepository.save(
                new WorkflowTransitionEntity(UUID.randomUUID().toString(), project, inReview, done, null));

        return new ProjectDto(project.getId(), project.getKey(), project.getName(), project.getDescription());
    }
}
