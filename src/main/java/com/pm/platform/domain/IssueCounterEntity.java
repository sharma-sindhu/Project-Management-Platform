package com.pm.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "issue_counters")
public class IssueCounterEntity {

    @Id
    @Column(length = 255)
    private String projectId;

    private long nextNumber = 1;

    @Version
    private long version;

    protected IssueCounterEntity() {}

    public IssueCounterEntity(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public long getNextNumber() {
        return nextNumber;
    }

    public void setNextNumber(long nextNumber) {
        this.nextNumber = nextNumber;
    }

    /** Returns the allocated issue number and advances the counter. */
    public int allocateNextIssueNumber() {
        int n = (int) nextNumber;
        nextNumber = n + 1L;
        return n;
    }
}
