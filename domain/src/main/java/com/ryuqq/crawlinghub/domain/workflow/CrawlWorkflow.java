package com.ryuqq.crawlinghub.domain.workflow;

import com.ryuqq.crawlinghub.domain.site.SiteId;

public class CrawlWorkflow {

    private final WorkflowId workflowId;
    private final SiteId siteId;
    private final String workflowName;
    private String workflowDescription;
    private boolean isActive;

    private CrawlWorkflow(WorkflowId workflowId, SiteId siteId, String workflowName, String workflowDescription, boolean isActive) {
        this.workflowId = workflowId;
        this.siteId = siteId;
        this.workflowName = workflowName;
        this.workflowDescription = workflowDescription;
        this.isActive = isActive;
    }

    public static CrawlWorkflow create(SiteId siteId, String workflowName, String workflowDescription) {
        validateCreate(siteId, workflowName);
        return new CrawlWorkflow(null, siteId, workflowName, workflowDescription, true);
    }

    public static CrawlWorkflow reconstitute(WorkflowId workflowId, SiteId siteId, String workflowName,
                                            String workflowDescription, boolean isActive) {
        return new CrawlWorkflow(workflowId, siteId, workflowName, workflowDescription, isActive);
    }

    private static void validateCreate(SiteId siteId, String workflowName) {
        if (siteId == null) {
            throw new IllegalArgumentException("Site ID cannot be null");
        }
        if (workflowName == null || workflowName.isBlank()) {
            throw new IllegalArgumentException("Workflow name cannot be null or blank");
        }
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

    public WorkflowId getWorkflowId() {
        return workflowId;
    }

    public SiteId getSiteId() {
        return siteId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getWorkflowDescription() {
        return workflowDescription;
    }

    public boolean isActive() {
        return isActive;
    }

}
