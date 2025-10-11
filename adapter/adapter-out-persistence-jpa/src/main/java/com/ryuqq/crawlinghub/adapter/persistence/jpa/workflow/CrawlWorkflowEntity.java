package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "crawl_workflow")
public class CrawlWorkflowEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workflow_id")
    private Long workflowId;

    @Column(name = "site_id", nullable = false)
    private Long siteId;

    @Column(name = "workflow_name", nullable = false, length = 200)
    private String workflowName;

    @Column(name = "workflow_description", columnDefinition = "TEXT")
    private String workflowDescription;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    protected CrawlWorkflowEntity() {
    }

    private CrawlWorkflowEntity(Long workflowId, Long siteId, String workflowName, String workflowDescription, Boolean isActive) {
        this.workflowId = workflowId;
        this.siteId = siteId;
        this.workflowName = workflowName;
        this.workflowDescription = workflowDescription;
        this.isActive = isActive;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getWorkflowDescription() {
        return workflowDescription;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateDescription(String newDescription) {
        this.workflowDescription = newDescription;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long workflowId;
        private Long siteId;
        private String workflowName;
        private String workflowDescription;
        private Boolean isActive;

        public Builder workflowId(Long workflowId) {
            this.workflowId = workflowId;
            return this;
        }

        public Builder siteId(Long siteId) {
            this.siteId = siteId;
            return this;
        }

        public Builder workflowName(String workflowName) {
            this.workflowName = workflowName;
            return this;
        }

        public Builder workflowDescription(String workflowDescription) {
            this.workflowDescription = workflowDescription;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public CrawlWorkflowEntity build() {
            return new CrawlWorkflowEntity(workflowId, siteId, workflowName, workflowDescription, isActive);
        }
    }

}
