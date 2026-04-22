package com.pm.platform.service;

import com.pm.platform.dto.ActivityEntryDto;
import com.pm.platform.dto.UserRefDto;
import com.pm.platform.repository.ActivityLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityFeedService {

    private final ActivityLogRepository activityLogRepository;
    private final IssueService issueService;

    public ActivityFeedService(ActivityLogRepository activityLogRepository, IssueService issueService) {
        this.activityLogRepository = activityLogRepository;
        this.issueService = issueService;
    }

    @Transactional(readOnly = true)
    public Page<ActivityEntryDto> projectFeed(String projectId, int page, int size) {
        var project = issueService.requireProject(projectId);
        return activityLogRepository
                .findByProject(project, PageRequest.of(page, size))
                .map(
                        a ->
                                new ActivityEntryDto(
                                        a.getId(),
                                        a.getIssue().getProject().getKey() + "-" + a.getIssue().getIssueNumber(),
                                        UserRefDto.of(a.getActor().getId(), a.getActor().getDisplayName()),
                                        a.getAction(),
                                        a.getPayload(),
                                        a.getCreatedAt()));
    }
}
