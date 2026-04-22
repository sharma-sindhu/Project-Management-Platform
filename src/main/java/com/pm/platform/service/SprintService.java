package com.pm.platform.service;

import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.ProjectEntity;
import com.pm.platform.domain.SprintEntity;
import com.pm.platform.domain.SprintState;
import com.pm.platform.domain.WorkflowStatusEntity;
import com.pm.platform.dto.CompleteSprintRequest;
import com.pm.platform.dto.CreateSprintRequest;
import com.pm.platform.dto.SprintDto;
import com.pm.platform.repository.IssueRepository;
import com.pm.platform.repository.SprintRepository;
import com.pm.platform.repository.WorkflowStatusRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;
    private final IssueService issueService;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final ActivityLogger activityLogger;
    private final RealtimeEventPublisher realtimeEventPublisher;

    public SprintService(
            SprintRepository sprintRepository,
            IssueRepository issueRepository,
            IssueService issueService,
            WorkflowStatusRepository workflowStatusRepository,
            ActivityLogger activityLogger,
            RealtimeEventPublisher realtimeEventPublisher) {
        this.sprintRepository = sprintRepository;
        this.issueRepository = issueRepository;
        this.issueService = issueService;
        this.workflowStatusRepository = workflowStatusRepository;
        this.activityLogger = activityLogger;
        this.realtimeEventPublisher = realtimeEventPublisher;
    }

    @Transactional(readOnly = true)
    public List<SprintDto> listSprints(String projectId) {
        ProjectEntity project = issueService.requireProject(projectId);
        return sprintRepository.findByProjectOrderByStartDateDesc(project).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public SprintDto createSprint(String projectId, CreateSprintRequest req) {
        ProjectEntity project = issueService.requireProject(projectId);
        if (req.end_date().isBefore(req.start_date())) {
            throw new IllegalArgumentException("end_date must be on or after start_date");
        }
        SprintState state = req.state() != null ? req.state() : SprintState.PLANNED;
        String id =
                req.sprint_id() != null && !req.sprint_id().isBlank()
                        ? req.sprint_id().trim()
                        : UUID.randomUUID().toString();
        if (sprintRepository.existsById(id)) {
            throw new IllegalArgumentException("Sprint id already exists: " + id);
        }
        SprintEntity sprint = new SprintEntity(id, project, req.name(), req.start_date(), req.end_date(), state);
        sprint = sprintRepository.save(sprint);
        Map<String, Object> payload = new HashMap<>();
        payload.put("sprint_id", sprint.getId());
        payload.put("state", sprint.getState().name());
        payload.put("name", sprint.getName());
        realtimeEventPublisher.publishSprintUpdated(project.getId(), payload);
        return toDto(sprint);
    }

    private SprintDto toDto(SprintEntity s) {
        Integer velocity = computeVelocityForCompleted(s);
        return new SprintDto(
                s.getId(),
                s.getName(),
                s.getStartDate(),
                s.getEndDate(),
                s.getState(),
                velocity);
    }

    /** Sum story points for issues in sprint whose status name equals "Done" (case-insensitive). */
    private Integer computeVelocityForCompleted(SprintEntity sprint) {
        if (sprint.getState() != SprintState.COMPLETED) {
            return null;
        }
        List<IssueEntity> inSprint = issueRepository.findByProjectAndSprint(sprint.getProject(), sprint);
        int sum = 0;
        for (IssueEntity i : inSprint) {
            if (i.getStatus().getName().equalsIgnoreCase("done") && i.getStoryPoints() != null) {
                sum += i.getStoryPoints();
            }
        }
        return sum;
    }

    @Transactional
    public SprintDto startSprint(String sprintId) {
        SprintEntity sprint = sprintRepository
                .findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
        ProjectEntity project = sprint.getProject();
        final String thisSprintId = sprint.getId();
        sprintRepository.findByProjectAndState(project, SprintState.ACTIVE).ifPresent(other -> {
            if (!other.getId().equals(thisSprintId)) {
                other.setState(SprintState.PLANNED);
                sprintRepository.save(other);
            }
        });
        sprint.setState(SprintState.ACTIVE);
        sprint = sprintRepository.save(sprint);
        Map<String, Object> payload = new HashMap<>();
        payload.put("sprint_id", sprint.getId().toString());
        payload.put("state", sprint.getState().name());
        realtimeEventPublisher.publishSprintUpdated(project.getId(), payload);
        return toDto(sprint);
    }

    @Transactional
    public Map<String, Object> completeSprint(String sprintId, CompleteSprintRequest req) {
        SprintEntity sprint = sprintRepository
                .findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
        if (sprint.getState() != SprintState.ACTIVE) {
            throw new IllegalStateException("Only an active sprint can be completed");
        }
        ProjectEntity project = sprint.getProject();
        List<IssueEntity> issues = issueRepository.findByProjectAndSprint(project, sprint);
        Optional<WorkflowStatusEntity> doneStatus =
                workflowStatusRepository.findByProjectAndNameIgnoreCase(project, "Done");

        int velocity = 0;
        List<String> incompleteKeys = new java.util.ArrayList<>();
        for (IssueEntity issue : issues) {
            boolean isDone =
                    doneStatus.map(ds -> ds.getId().equals(issue.getStatus().getId())).orElse(false);
            if (isDone && issue.getStoryPoints() != null) {
                velocity += issue.getStoryPoints();
            }
            if (!isDone) {
                incompleteKeys.add(
                        issue.getProject().getKey() + "-" + issue.getIssueNumber());
            }
        }

        if (req.carry_over_issue_ids() != null) {
            for (String id : req.carry_over_issue_ids()) {
                IssueEntity issue = issueRepository.findById(id).orElseThrow();
                if (!issue.getSprint().getId().equals(sprint.getId())) {
                    continue;
                }
                if (req.target_sprint_id() == null) {
                    throw new IllegalArgumentException("target_sprint_id required for carry-over");
                }
                SprintEntity target =
                        sprintRepository
                                .findById(req.target_sprint_id())
                                .orElseThrow(() -> new IllegalArgumentException("Target sprint not found"));
                if (!target.getProject().getId().equals(project.getId())) {
                    throw new IllegalArgumentException("Target sprint wrong project");
                }
                issue.setSprint(target);
                issue.touch();
                issueRepository.save(issue);
                activityLogger.log(
                        issue,
                        issue.getReporter(),
                        "sprint_carry_over",
                        "{\"from\":\"" + sprint.getId() + "\",\"to\":\"" + target.getId() + "\"}");
            }
        }
        if (req.move_to_backlog_issue_ids() != null) {
            for (String id : req.move_to_backlog_issue_ids()) {
                IssueEntity issue = issueRepository.findById(id).orElseThrow();
                if (issue.getSprint() == null || !issue.getSprint().getId().equals(sprint.getId())) {
                    continue;
                }
                issue.setSprint(null);
                issue.touch();
                issueRepository.save(issue);
                activityLogger.log(issue, issue.getReporter(), "moved_to_backlog", "{}");
            }
        }

        sprint.setState(SprintState.COMPLETED);
        sprintRepository.save(sprint);

        Map<String, Object> result = new HashMap<>();
        result.put("sprint_id", sprint.getId());
        result.put("velocity_story_points", velocity);
        result.put("incomplete_issue_keys", incompleteKeys);
        result.put("state", SprintState.COMPLETED.name());

        Map<String, Object> wsPayload = new HashMap<>(result);
        realtimeEventPublisher.publishSprintUpdated(project.getId(), wsPayload);

        return result;
    }
}
