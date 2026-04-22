package com.pm.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "activity_logs")
public class ActivityLogEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private IssueEntity issue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private UserEntity actor;

    @Column(nullable = false, length = 100)
    private String action;

    /** JSON payload describing the change */
    @Column(name = "payload", length = 4000)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected ActivityLogEntity() {}

    public ActivityLogEntity(String id, IssueEntity issue, UserEntity actor, String action, String payload) {
        this.id = id;
        this.issue = issue;
        this.actor = actor;
        this.action = action;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public IssueEntity getIssue() {
        return issue;
    }

    public UserEntity getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
