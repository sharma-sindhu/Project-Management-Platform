package com.pm.platform.service;

import com.pm.platform.domain.IssueCounterEntity;
import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.IssueWatcherEntity;
import com.pm.platform.domain.ProjectEntity;
import com.pm.platform.domain.SprintEntity;
import com.pm.platform.domain.UserEntity;
import com.pm.platform.domain.WorkflowStatusEntity;
import com.pm.platform.domain.WorkflowTransitionEntity;
import com.pm.platform.dto.CreateIssueRequest;
import com.pm.platform.dto.IssueDto;
import com.pm.platform.dto.PatchIssueRequest;
import com.pm.platform.dto.TransitionRequest;
import com.pm.platform.repository.IssueCounterRepository;
import com.pm.platform.repository.IssueRepository;
import com.pm.platform.repository.IssueWatcherRepository;
import com.pm.platform.repository.ProjectRepository;
import com.pm.platform.repository.SprintRepository;
import com.pm.platform.repository.UserRepository;
import com.pm.platform.repository.WorkflowStatusRepository;
import com.pm.platform.repository.WorkflowTransitionRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final IssueCounterRepository issueCounterRepository;
    private final IssueWatcherRepository issueWatcherRepository;
    private final ActivityLogger activityLogger;
    private final NotificationService notificationService;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final IssueMapper issueMapper;

    public IssueService(
            ProjectRepository projectRepository,
            IssueRepository issueRepository,
            UserRepository userRepository,
            SprintRepository sprintRepository,
            WorkflowStatusRepository workflowStatusRepository,
            WorkflowTransitionRepository workflowTransitionRepository,
            IssueCounterRepository issueCounterRepository,
            IssueWatcherRepository issueWatcherRepository,
            ActivityLogger activityLogger,
            NotificationService notificationService,
            RealtimeEventPublisher realtimeEventPublisher,
            IssueMapper issueMapper) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.sprintRepository = sprintRepository;
        this.workflowStatusRepository = workflowStatusRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.issueCounterRepository = issueCounterRepository;
        this.issueWatcherRepository = issueWatcherRepository;
        this.activityLogger = activityLogger;
        this.notificationService = notificationService;
        this.realtimeEventPublisher = realtimeEventPublisher;
        this.issueMapper = issueMapper;
    }

    public ProjectEntity requireProject(String projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));
    }

    public IssueEntity requireIssue(String issueId) {
        return issueRepository
                .findById(issueId)
                .orElseThrow(() -> new IssueNotFoundException("Issue not found: " + issueId));
    }

    @Transactional
    public IssueDto createIssue(String projectId, CreateIssueRequest req) {
        ProjectEntity project = requireProject(projectId);
        UserEntity reporter = userRepository
                .findById(req.reporter_user_id())
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));
        UserEntity assignee =
                req.assignee_user_id() == null
                        ? null
                        : userRepository
                                .findById(req.assignee_user_id())
                                .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
        SprintEntity sprint =
                req.sprint_id() == null
                        ? null
                        : sprintRepository
                                .findById(req.sprint_id())
                                .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
        if (sprint != null && !sprint.getProject().getId().equals(project.getId())) {
            throw new IllegalArgumentException("Sprint does not belong to project");
        }
        IssueEntity parent =
                req.parent_issue_id() == null
                        ? null
                        : issueRepository
                                .findById(req.parent_issue_id())
                                .orElseThrow(() -> new IllegalArgumentException("Parent issue not found"));
        if (parent != null && !parent.getProject().getId().equals(project.getId())) {
            throw new IllegalArgumentException("Parent issue must belong to same project");
        }

        WorkflowStatusEntity initial =
                req.initial_status_name() != null
                        ? workflowStatusRepository
                                .findByProjectAndNameIgnoreCase(project, req.initial_status_name())
                                .orElseThrow(() -> new IllegalArgumentException("Unknown initial status"))
                        : workflowStatusRepository
                                .findByProjectOrderBySortOrderAsc(project)
                                .stream()
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("Project has no workflow statuses"));

        IssueCounterEntity counter =
                issueCounterRepository
                        .findForUpdate(project.getId())
                        .orElseThrow(() -> new IllegalStateException("Issue counter not initialized for project"));
        int num = counter.allocateNextIssueNumber();
        issueCounterRepository.save(counter);

        IssueEntity issue =
                new IssueEntity(
                        UUID.randomUUID().toString(),
                        project,
                        num,
                        req.type(),
                        req.title(),
                        req.description(),
                        initial,
                        req.priority() != null ? req.priority() : com.pm.platform.domain.Priority.MEDIUM,
                        assignee,
                        reporter,
                        sprint,
                        parent);
        if (req.story_points() != null) {
            issue.setStoryPoints(req.story_points());
        }
        if (req.labels() != null) {
            issue.getLabels().addAll(req.labels());
        }
        issue = issueRepository.save(issue);

        activityLogger.log(issue, reporter, "issue_created", "{\"title\":\"" + escape(issue.getTitle()) + "\"}");
        if (assignee != null) {
            notificationService.notify(
                    assignee,
                    issue,
                    com.pm.platform.domain.NotificationType.ASSIGNMENT,
                    "You were assigned to " + issueMapper.issueKey(issue));
        }
        IssueDto dto = issueMapper.toDto(issue);
        realtimeEventPublisher.publishIssueCreated(project.getId(), dto);
        return dto;
    }

    @Transactional
    public IssueDto patchIssue(String issueId, PatchIssueRequest req) {
        IssueEntity issue = requireIssue(issueId);
        if (req.expected_version() != null && req.expected_version() != issue.getVersion()) {
            throw new OptimisticLockConflictException("Issue was modified by another user; refresh and retry.");
        }
        try {
            if (req.title() != null) {
                issue.setTitle(req.title());
            }
            if (req.description() != null) {
                issue.setDescription(req.description());
            }
            if (req.priority() != null) {
                issue.setPriority(req.priority());
            }
            if (req.assignee_user_id() != null) {
                UserEntity assignee = userRepository
                        .findById(req.assignee_user_id())
                        .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
                issue.setAssignee(assignee);
                notificationService.notify(
                        assignee,
                        issue,
                        com.pm.platform.domain.NotificationType.ASSIGNMENT,
                        "You were assigned to " + issueMapper.issueKey(issue));
            }
            if (req.sprint_id() != null) {
                SprintEntity sp = sprintRepository
                        .findById(req.sprint_id())
                        .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
                if (!sp.getProject().getId().equals(issue.getProject().getId())) {
                    throw new IllegalArgumentException("Sprint does not belong to project");
                }
                issue.setSprint(sp);
            }
            if (req.parent_issue_id() != null) {
                IssueEntity p = requireIssue(req.parent_issue_id());
                if (!p.getProject().getId().equals(issue.getProject().getId())) {
                    throw new IllegalArgumentException("Parent must be same project");
                }
                issue.setParent(p);
            }
            if (req.story_points() != null) {
                issue.setStoryPoints(req.story_points());
            }
            if (req.labels() != null) {
                issue.getLabels().clear();
                issue.getLabels().addAll(req.labels());
            }
            issue.touch();
            issue = issueRepository.saveAndFlush(issue);
            IssueDto dto = issueMapper.toDto(issue);
            realtimeEventPublisher.publishIssueUpdated(issue.getProject().getId(), dto);
            return dto;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockConflictException("Issue was modified concurrently; refresh and retry.");
        }
    }

    @Transactional
    public IssueDto transition(String issueId, TransitionRequest req) {
        IssueEntity issue = requireIssue(issueId);
        UserEntity actor =
                req.actor_user_id() == null
                        ? issue.getReporter()
                        : userRepository
                                .findById(req.actor_user_id())
                                .orElseThrow(() -> new IllegalArgumentException("Actor not found"));
        ProjectEntity project = issue.getProject();
        WorkflowStatusEntity current = issue.getStatus();
        WorkflowStatusEntity target =
                workflowStatusRepository
                        .findByProjectAndNameIgnoreCase(project, req.to_status_name())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown target status: " + req.to_status_name()));

        if (target.getId().equals(current.getId())) {
            return issueMapper.toDto(issue);
        }

        WorkflowTransitionEntity transition =
                workflowTransitionRepository
                        .findByProjectAndFromStatusAndToStatus(project, current, target)
                        .orElseThrow(
                                () -> {
                                    List<String> allowed =
                                            workflowTransitionRepository
                                                    .findByProjectAndFromStatus(project, current).stream()
                                                    .map(t -> t.getToStatus().getName())
                                                    .collect(Collectors.toList());
                                    return new WorkflowViolationException(
                                            "Transition from '" + current.getName() + "' to '" + target.getName()
                                                    + "' is not allowed.",
                                            allowed);
                                });

        validateTransitionHooks(issue, target);

        issue.setStatus(target);
        if (transition.getAssignReviewerOnTransition() != null) {
            issue.setAssignee(transition.getAssignReviewerOnTransition());
        } else if (req.override_reviewer_id() != null) {
            UserEntity rev = userRepository
                    .findById(req.override_reviewer_id())
                    .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));
            issue.setAssignee(rev);
        }
        issue.touch();
        issue = issueRepository.save(issue);

        activityLogger.log(
                issue,
                actor,
                "status_changed",
                "{\"from\":\"" + escape(current.getName()) + "\",\"to\":\"" + escape(target.getName()) + "\"}");

        for (IssueWatcherEntity w : issueWatcherRepository.findByIssue(issue)) {
            notificationService.notify(
                    w.getUser(),
                    issue,
                    com.pm.platform.domain.NotificationType.STATUS_CHANGE,
                    issueMapper.issueKey(issue) + " moved to " + target.getName());
        }

        IssueDto dto = issueMapper.toDto(issue);
        realtimeEventPublisher.publishIssueMoved(issue.getProject().getId(), dto);
        return dto;
    }

    /** Example validation hook: moving to In Review requires story points for stories. */
    private void validateTransitionHooks(IssueEntity issue, WorkflowStatusEntity target) {
        if (target.getName().toLowerCase().contains("review")
                && issue.getType() == com.pm.platform.domain.IssueType.STORY
                && issue.getStoryPoints() == null) {
            throw new IllegalArgumentException("Story points required before moving to review.");
        }
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    @Transactional(readOnly = true)
    public IssueDto getDto(String issueId) {
        return issueMapper.toDto(requireIssue(issueId));
    }
}
