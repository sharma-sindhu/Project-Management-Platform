package com.pm.platform.web;

import com.pm.platform.dto.CommentDto;
import com.pm.platform.dto.CreateCommentRequest;
import com.pm.platform.dto.IssueDto;
import com.pm.platform.dto.PatchIssueRequest;
import com.pm.platform.dto.TransitionRequest;
import com.pm.platform.service.CommentService;
import com.pm.platform.service.IssueService;
import com.pm.platform.service.WatcherService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
public class IssueApiController {

    private final IssueService issueService;
    private final CommentService commentService;
    private final WatcherService watcherService;

    public IssueApiController(
            IssueService issueService, CommentService commentService, WatcherService watcherService) {
        this.issueService = issueService;
        this.commentService = commentService;
        this.watcherService = watcherService;
    }

    @GetMapping("/{issueId}")
    public IssueDto get(@PathVariable String issueId) {
        return issueService.getDto(issueId);
    }

    @PatchMapping("/{issueId}")
    public IssueDto patch(@PathVariable String issueId, @Valid @RequestBody PatchIssueRequest body) {
        return issueService.patchIssue(issueId, body);
    }

    @PostMapping("/{issueId}/transitions")
    public IssueDto transition(@PathVariable String issueId, @Valid @RequestBody TransitionRequest body) {
        return issueService.transition(issueId, body);
    }

    @GetMapping("/{issueId}/comments")
    public List<CommentDto> comments(@PathVariable String issueId) {
        return commentService.list(issueId);
    }

    @PostMapping("/{issueId}/comments")
    public CommentDto addComment(@PathVariable String issueId, @Valid @RequestBody CreateCommentRequest body) {
        return commentService.add(issueId, body);
    }

    @PostMapping("/{issueId}/watch")
    public void watch(@PathVariable String issueId, @RequestParam("user_id") String userId) {
        watcherService.watch(issueId, userId);
    }

    @DeleteMapping("/{issueId}/watch")
    public void unwatch(@PathVariable String issueId, @RequestParam("user_id") String userId) {
        watcherService.unwatch(issueId, userId);
    }
}
