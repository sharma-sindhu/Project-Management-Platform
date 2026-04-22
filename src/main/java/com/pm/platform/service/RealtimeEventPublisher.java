package com.pm.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pm.platform.dto.IssueDto;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimeEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, Deque<ObjectNode>> projectQueues = new ConcurrentHashMap<>();

    public RealtimeEventPublisher(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishIssueCreated(String projectId, IssueDto issue) {
        publish(projectId, "issue_created", issue);
    }

    public void publishIssueUpdated(String projectId, IssueDto issue) {
        publish(projectId, "issue_updated", issue);
    }

    public void publishIssueMoved(String projectId, IssueDto issue) {
        publish(projectId, "issue_moved", issue);
    }

    public void publishCommentAdded(String projectId, Object payload) {
        publish(projectId, "comment_added", payload);
    }

    public void publishSprintUpdated(String projectId, Object payload) {
        publish(projectId, "sprint_updated", payload);
    }

    public void publishPresence(String projectId, String resourceType, String resourceId, Object presencePayload) {
        ObjectNode n = objectMapper.createObjectNode();
        n.put("type", "presence");
        n.put("resource_type", resourceType);
        n.put("resource_id", resourceId);
        n.set("payload", objectMapper.valueToTree(presencePayload));
        n.put("ts", Instant.now().toString());
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/presence", n);
    }

    private void publish(String projectId, String type, Object payload) {
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("type", type);
        envelope.set("payload", objectMapper.valueToTree(payload));
        envelope.put("ts", Instant.now().toString());
        Deque<ObjectNode> q = projectQueues.computeIfAbsent(projectId, k -> new ArrayDeque<>());
        synchronized (q) {
            q.addLast(envelope);
            while (q.size() > 500) {
                q.removeFirst();
            }
        }
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/events", envelope);
    }

    public ArrayNode replayRecent(String projectId) {
        Deque<ObjectNode> q = projectQueues.get(projectId);
        ArrayNode arr = objectMapper.createArrayNode();
        if (q == null) {
            return arr;
        }
        synchronized (q) {
            for (ObjectNode e : q) {
                arr.add(e);
            }
        }
        return arr;
    }
}
