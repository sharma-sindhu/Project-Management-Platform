package com.pm.platform.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.pm.platform.service.RealtimeEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class ReplayController {

    private final RealtimeEventPublisher realtimeEventPublisher;

    public ReplayController(RealtimeEventPublisher realtimeEventPublisher) {
        this.realtimeEventPublisher = realtimeEventPublisher;
    }

    @GetMapping("/events/replay")
    public ResponseEntity<JsonNode> replay(@PathVariable String projectId) {
        return ResponseEntity.ok(realtimeEventPublisher.replayRecent(projectId));
    }
}
