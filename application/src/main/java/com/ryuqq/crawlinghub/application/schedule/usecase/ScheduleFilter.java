package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;

import java.util.Optional;

/**
 * Filter criteria for querying schedules
 * Encapsulates optional filter parameters to simplify query logic
 *
 * @author Sangwon Ryu (ryuqq@company.com)
 * @since 2025-10-14
 */
public record ScheduleFilter(
        Long workflowId,
        Boolean isEnabled
) {
    /**
     * Creates an empty filter (no criteria)
     *
     * @return empty filter
     */
    public static ScheduleFilter empty() {
        return new ScheduleFilter(null, null);
    }

    /**
     * Creates a filter for workflow ID only
     *
     * @param workflowId the workflow ID
     * @return workflow filter
     */
    public static ScheduleFilter byWorkflowId(Long workflowId) {
        return new ScheduleFilter(workflowId, null);
    }

    /**
     * Creates a filter for enabled status only
     *
     * @param isEnabled the enabled status
     * @return enabled status filter
     */
    public static ScheduleFilter byIsEnabled(Boolean isEnabled) {
        return new ScheduleFilter(null, isEnabled);
    }

    /**
     * Checks if workflow ID filter is present
     *
     * @return true if workflow ID is set
     */
    public boolean hasWorkflowId() {
        return workflowId != null;
    }

    /**
     * Checks if enabled status filter is present
     *
     * @return true if enabled status is set
     */
    public boolean hasIsEnabled() {
        return isEnabled != null;
    }

    /**
     * Checks if any filter criteria is present
     *
     * @return true if at least one filter is set
     */
    public boolean hasAnyFilter() {
        return hasWorkflowId() || hasIsEnabled();
    }

    /**
     * Checks if both filters are present
     *
     * @return true if both workflow ID and enabled status are set
     */
    public boolean hasBothFilters() {
        return hasWorkflowId() && hasIsEnabled();
    }

    /**
     * Gets workflow ID as Optional
     *
     * @return Optional workflow ID
     */
    public Optional<WorkflowId> getWorkflowId() {
        return Optional.ofNullable(workflowId).map(WorkflowId::of);
    }

    /**
     * Gets enabled status as Optional
     *
     * @return Optional enabled status
     */
    public Optional<Boolean> getIsEnabled() {
        return Optional.ofNullable(isEnabled);
    }
}
