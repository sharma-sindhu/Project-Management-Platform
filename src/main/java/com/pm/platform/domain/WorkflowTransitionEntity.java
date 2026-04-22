package com.pm.platform.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "workflow_transitions")
public class WorkflowTransitionEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_status_id", nullable = false)
    private WorkflowStatusEntity fromStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_status_id", nullable = false)
    private WorkflowStatusEntity toStatus;

    /** Optional: auto-assign reviewer user id when entering toStatus */
    @JoinColumn(name = "assign_reviewer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity assignReviewerOnTransition;

    protected WorkflowTransitionEntity() {}

    public WorkflowTransitionEntity(
            String id,
            ProjectEntity project,
            WorkflowStatusEntity fromStatus,
            WorkflowStatusEntity toStatus,
            UserEntity assignReviewerOnTransition) {
        this.id = id;
        this.project = project;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.assignReviewerOnTransition = assignReviewerOnTransition;
    }

    public String getId() {
        return id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public WorkflowStatusEntity getFromStatus() {
        return fromStatus;
    }

    public WorkflowStatusEntity getToStatus() {
        return toStatus;
    }

    public UserEntity getAssignReviewerOnTransition() {
        return assignReviewerOnTransition;
    }
}
