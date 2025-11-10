package com.ryuqq.crawlinghub.application.schedule.port;

import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;

import java.util.List;
import java.util.Optional;

/**
 * Query Port for CrawlSchedule (CQRS Pattern)
 * Handles complex query operations
 */
public interface CrawlScheduleQueryPort {

    /**
     * Finds a schedule by ID with all related data
     *
     * @param scheduleId the schedule ID
     * @return Optional containing the schedule with input params
     */
    Optional<CrawlSchedule> findByIdWithInputParams(ScheduleId scheduleId);

    /**
     * Finds all schedules for a workflow
     *
     * @param workflowId the workflow ID
     * @return list of schedules
     */
    List<CrawlSchedule> findByWorkflowId(WorkflowId workflowId);

    /**
     * Finds schedules by enabled status
     *
     * @param isEnabled the enabled status
     * @return list of schedules
     */
    List<CrawlSchedule> findByIsEnabled(boolean isEnabled);

    /**
     * Finds schedules by workflow ID and enabled status
     *
     * @param workflowId the workflow ID
     * @param isEnabled the enabled status
     * @return list of schedules
     */
    List<CrawlSchedule> findByWorkflowIdAndIsEnabled(WorkflowId workflowId, boolean isEnabled);

    /**
     * Finds all schedules
     *
     * @return list of all schedules
     */
    List<CrawlSchedule> findAll();

    /**
     * Counts schedules for a workflow
     *
     * @param workflowId the workflow ID
     * @return count of schedules
     */
    long countByWorkflowId(WorkflowId workflowId);
}
