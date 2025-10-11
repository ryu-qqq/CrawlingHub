package com.ryuqq.crawlinghub.domain.workflow;

import com.ryuqq.crawlinghub.domain.site.SiteId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrawlWorkflow {

    private final WorkflowId workflowId;
    private final SiteId siteId;
    private final String workflowName;
    private String workflowDescription;
    private boolean isActive;
    private final List<WorkflowStep> steps;

    private CrawlWorkflow(WorkflowId workflowId, SiteId siteId, String workflowName,
                         String workflowDescription, boolean isActive, List<WorkflowStep> steps) {
        this.workflowId = workflowId;
        this.siteId = siteId;
        this.workflowName = workflowName;
        this.workflowDescription = workflowDescription;
        this.isActive = isActive;
        this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
    }

    public static CrawlWorkflow create(SiteId siteId, String workflowName, String workflowDescription) {
        validateCreate(siteId, workflowName);
        return new CrawlWorkflow(null, siteId, workflowName, workflowDescription, true, new ArrayList<>());
    }

    public static CrawlWorkflow reconstitute(WorkflowId workflowId, SiteId siteId, String workflowName,
                                            String workflowDescription, boolean isActive) {
        return new CrawlWorkflow(workflowId, siteId, workflowName, workflowDescription, isActive, new ArrayList<>());
    }

    public static CrawlWorkflow reconstituteWithSteps(WorkflowId workflowId, SiteId siteId, String workflowName,
                                                     String workflowDescription, boolean isActive, List<WorkflowStep> steps) {
        return new CrawlWorkflow(workflowId, siteId, workflowName, workflowDescription, isActive, steps);
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

    public void addStep(WorkflowStep step) {
        if (step == null) {
            throw new IllegalArgumentException("Step cannot be null");
        }
        this.steps.add(step);
    }

    public void replaceSteps(List<WorkflowStep> newSteps) {
        this.steps.clear();
        if (newSteps != null) {
            this.steps.addAll(newSteps);
        }
    }

    public void clearSteps() {
        this.steps.clear();
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

    public List<WorkflowStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public int getStepsCount() {
        return steps.size();
    }

}
