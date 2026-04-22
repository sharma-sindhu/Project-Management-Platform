package com.pm.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "comments")
public class CommentEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private IssueEntity issue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @Column(nullable = false, length = 10000)
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private CommentEntity parent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected CommentEntity() {}

    public CommentEntity(String id, IssueEntity issue, UserEntity author, String body, CommentEntity parent) {
        this.id = id;
        this.issue = issue;
        this.author = author;
        this.body = body;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public IssueEntity getIssue() {
        return issue;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public CommentEntity getParent() {
        return parent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
