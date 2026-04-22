package com.pm.platform.web;

import com.pm.platform.dto.CompleteSprintRequest;
import com.pm.platform.dto.SprintDto;
import com.pm.platform.service.SprintService;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sprints")
public class SprintApiController {

    private final SprintService sprintService;

    public SprintApiController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping("/{sprintId}/start")
    public SprintDto start(@PathVariable String sprintId) {
        return sprintService.startSprint(sprintId);
    }

    @PostMapping("/{sprintId}/complete")
    public Map<String, Object> complete(
            @PathVariable String sprintId, @RequestBody(required = false) CompleteSprintRequest body) {
        CompleteSprintRequest req =
                body == null
                        ? new CompleteSprintRequest(null, null, null)
                        : body;
        return sprintService.completeSprint(sprintId, req);
    }
}
