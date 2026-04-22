package com.pm.platform.web;

import com.pm.platform.service.RealtimeEventPublisher;
import java.util.Map;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class PresenceController {

    private final RealtimeEventPublisher realtimeEventPublisher;

    public PresenceController(RealtimeEventPublisher realtimeEventPublisher) {
        this.realtimeEventPublisher = realtimeEventPublisher;
    }

    /** Client sends to /app/projects/{projectId}/presence with body { userId, resourceType, resourceId, viewing: true } */
    @MessageMapping("/projects/{projectId}/presence")
    public void presence(
            @DestinationVariable String projectId, @Payload Map<String, Object> body) {
        String resourceType = String.valueOf(body.getOrDefault("resourceType", "board"));
        String resourceId = String.valueOf(body.getOrDefault("resourceId", "default"));
        realtimeEventPublisher.publishPresence(projectId, resourceType, resourceId, body);
    }
}
