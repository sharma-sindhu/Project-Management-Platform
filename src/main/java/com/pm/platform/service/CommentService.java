package com.pm.platform.service;

import com.pm.platform.domain.CommentEntity;
import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.NotificationType;
import com.pm.platform.domain.UserEntity;
import com.pm.platform.dto.CommentDto;
import com.pm.platform.dto.CreateCommentRequest;
import com.pm.platform.dto.UserRefDto;
import com.pm.platform.repository.CommentRepository;
import com.pm.platform.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    /** Matches {@code @user_<id>} where {@code id} is a non-empty token (letters, digits, {@code _}, {@code -}, {@code .}). */
    private static final Pattern MENTION = Pattern.compile("@user_([\\w.-]+)");

    private final CommentRepository commentRepository;
    private final IssueService issueService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityLogger activityLogger;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final IssueMapper issueMapper;

    public CommentService(
            CommentRepository commentRepository,
            IssueService issueService,
            UserRepository userRepository,
            NotificationService notificationService,
            ActivityLogger activityLogger,
            RealtimeEventPublisher realtimeEventPublisher,
            IssueMapper issueMapper) {
        this.commentRepository = commentRepository;
        this.issueService = issueService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityLogger = activityLogger;
        this.realtimeEventPublisher = realtimeEventPublisher;
        this.issueMapper = issueMapper;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> list(String issueId) {
        IssueEntity issue = issueService.requireIssue(issueId);
        return commentRepository.findByIssueOrderByCreatedAtAsc(issue).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CommentDto add(String issueId, CreateCommentRequest req) {
        IssueEntity issue = issueService.requireIssue(issueId);
        UserEntity author =
                userRepository
                        .findById(req.author_user_id())
                        .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        CommentEntity parent = null;
        if (req.parent_comment_id() != null) {
            parent = commentRepository
                    .findById(req.parent_comment_id())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            if (!parent.getIssue().getId().equals(issue.getId())) {
                throw new IllegalArgumentException("Parent comment wrong issue");
            }
        }
        CommentEntity c = new CommentEntity(UUID.randomUUID().toString(), issue, author, req.body(), parent);
        c = commentRepository.save(c);

        List<String> mentions = extractMentions(req.body());
        for (String mentionedUserId : mentions) {
            userRepository
                    .findById(mentionedUserId)
                    .ifPresent(
                            mentioned -> notificationService.notify(
                                    mentioned,
                                    issue,
                                    NotificationType.MENTION,
                                    author.getDisplayName()
                                            + " mentioned you on "
                                            + issueMapper.issueKey(issue)));
        }

        activityLogger.log(issue, author, "comment_added", "{\"comment_id\":\"" + c.getId() + "\"}");

        CommentDto dto = toDto(c);
        realtimeEventPublisher.publishCommentAdded(
                issue.getProject().getId(),
                java.util.Map.of(
                        "issue_id",
                        issueMapper.issueKey(issue),
                        "comment",
                        dto));
        return dto;
    }

    private List<String> extractMentions(String body) {
        List<String> out = new ArrayList<>();
        Matcher m = MENTION.matcher(body);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    private CommentDto toDto(CommentEntity c) {
        List<String> mentions = extractMentions(c.getBody());
        String parentId = c.getParent() == null ? null : c.getParent().getId();
        return new CommentDto(
                c.getId(),
                issueMapper.issueKey(c.getIssue()),
                UserRefDto.of(c.getAuthor().getId(), c.getAuthor().getDisplayName()),
                c.getBody(),
                parentId,
                c.getCreatedAt(),
                mentions);
    }
}
