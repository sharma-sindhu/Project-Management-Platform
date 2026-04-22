package com.pm.platform.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "issue_watchers")
@IdClass(IssueWatcherEntity.IssueWatcherId.class)
public class IssueWatcherEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private IssueEntity issue;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    protected IssueWatcherEntity() {}

    public IssueWatcherEntity(IssueEntity issue, UserEntity user) {
        this.issue = issue;
        this.user = user;
    }

    public IssueEntity getIssue() {
        return issue;
    }

    public UserEntity getUser() {
        return user;
    }

    public static class IssueWatcherId implements Serializable {
        private String issue;
        private String user;

        public IssueWatcherId() {}

        public IssueWatcherId(String issue, String user) {
            this.issue = issue;
            this.user = user;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IssueWatcherId that = (IssueWatcherId) o;
            return Objects.equals(issue, that.issue) && Objects.equals(user, that.user);
        }

        @Override
        public int hashCode() {
            return Objects.hash(issue, user);
        }
    }
}
