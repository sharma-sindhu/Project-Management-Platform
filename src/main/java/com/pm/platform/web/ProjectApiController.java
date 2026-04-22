package com.pm.platform.web;

import com.pm.platform.dto.BoardDto;
import com.pm.platform.dto.CreateIssueRequest;
import com.pm.platform.dto.CreateSprintRequest;
import com.pm.platform.dto.IssueDto;
import com.pm.platform.dto.SprintDto;
import com.pm.platform.service.ActivityFeedService;
import com.pm.platform.service.BoardService;
import com.pm.platform.service.IssueService;
import com.pm.platform.service.SprintService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class ProjectApiController {

    private final IssueService issueService;
    private final BoardService boardService;
    private final SprintService sprintService;
    private final ActivityFeedService activityFeedService;

    public ProjectApiController(
            IssueService issueService,
            BoardService boardService,
            SprintService sprintService,
            ActivityFeedService activityFeedService) {
        this.issueService = issueService;
        this.boardService = boardService;
        this.sprintService = sprintService;
        this.activityFeedService = activityFeedService;
    }

    @PostMapping("/issues")
    public IssueDto createIssue(@PathVariable String projectId, @Valid @RequestBody CreateIssueRequest body) {
        return issueService.createIssue(projectId, body);
    }

    @GetMapping("/board")
    public BoardDto board(@PathVariable String projectId) {
        return boardService.getBoard(projectId);
    }

    @GetMapping("/sprints")
    public List<SprintDto> sprints(@PathVariable String projectId) {
        return sprintService.listSprints(projectId);
    }

    @PostMapping("/sprints")
    public SprintDto createSprint(
            @PathVariable String projectId, @Valid @RequestBody CreateSprintRequest body) {
        return sprintService.createSprint(projectId, body);
    }

    @GetMapping("/activity")
    public Page<?> activity(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return activityFeedService.projectFeed(projectId, page, Math.min(size, 200));
    }
}
