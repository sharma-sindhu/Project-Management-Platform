package com.pm.platform.service;

import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.UserEntity;
import com.pm.platform.dto.IssueDto;
import com.pm.platform.dto.SprintRefDto;
import com.pm.platform.dto.UserRefDto;
import com.pm.platform.repository.IssueWatcherRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class IssueMapper {

    private final IssueWatcherRepository watcherRepository;

    public IssueMapper(IssueWatcherRepository watcherRepository) {
        this.watcherRepository = watcherRepository;
    }

    public String issueKey(IssueEntity issue) {
        return issue.getProject().getKey() + "-" + issue.getIssueNumber();
    }

    public String projectExternalId(IssueEntity issue) {
        return issue.getProject().getId();
    }

    public IssueDto toDto(IssueEntity issue) {
        UserRefDto assignee =
                issue.getAssignee() == null
                        ? null
                        : UserRefDto.of(issue.getAssignee().getId(), issue.getAssignee().getDisplayName());
        UserRefDto reporter = UserRefDto.of(issue.getReporter().getId(), issue.getReporter().getDisplayName());
        SprintRefDto sprint =
                issue.getSprint() == null
                        ? null
                        : SprintRefDto.from(
                                issue.getSprint().getId(),
                                issue.getSprint().getName(),
                                issue.getSprint().getStartDate(),
                                issue.getSprint().getEndDate());
        String parentKey = issue.getParent() == null ? null : issueKey(issue.getParent());
        List<String> watchers =
                watcherRepository.findByIssue(issue).stream()
                        .map(w -> w.getUser().getId())
                        .collect(Collectors.toList());
        return new IssueDto(
                issue.getId(),
                issueKey(issue),
                projectExternalId(issue),
                issue.getType().name().toLowerCase(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus().getName(),
                issue.getPriority().name().toLowerCase(),
                assignee,
                reporter,
                sprint,
                List.copyOf(issue.getLabels()),
                issue.getStoryPoints(),
                parentKey,
                watchers,
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getVersion());
    }
}
