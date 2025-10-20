package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for retrieving schedule information
 * Provides various query methods for schedules
 */
@Service
@Transactional(readOnly = true)
public class GetScheduleUseCase {

    private final CrawlScheduleCommandPort scheduleCommandPort;
    private final CrawlScheduleQueryPort scheduleQueryPort;

    public GetScheduleUseCase(
            CrawlScheduleCommandPort scheduleCommandPort,
            CrawlScheduleQueryPort scheduleQueryPort) {
        this.scheduleCommandPort = scheduleCommandPort;
        this.scheduleQueryPort = scheduleQueryPort;
    }

    /**
     * Gets a schedule by ID
     *
     * @param scheduleId the schedule ID
     * @return the schedule
     * @throws ScheduleNotFoundException if schedule not found
     */
    public CrawlSchedule getById(Long scheduleId) {
        return scheduleCommandPort.findById(ScheduleId.of(scheduleId))
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
    }

    /**
     * Gets a schedule by ID with input parameters
     *
     * @param scheduleId the schedule ID
     * @return the schedule with input params
     * @throws ScheduleNotFoundException if schedule not found
     */
    public CrawlSchedule getByIdWithInputParams(Long scheduleId) {
        return scheduleQueryPort.findByIdWithInputParams(ScheduleId.of(scheduleId))
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
    }

    /**
     * Gets input parameters for a schedule
     *
     * @param scheduleId the schedule ID
     * @return list of input parameters
     */
    public List<ScheduleInputParam> getInputParams(Long scheduleId) {
        return scheduleCommandPort.findInputParamsByScheduleId(scheduleId);
    }

    /**
     * Gets all schedules for a workflow
     *
     * @param workflowId the workflow ID
     * @return list of schedules
     */
    public List<CrawlSchedule> getByWorkflowId(Long workflowId) {
        return scheduleQueryPort.findByWorkflowId(WorkflowId.of(workflowId));
    }

    /**
     * Gets schedules by enabled status
     *
     * @param isEnabled the enabled status
     * @return list of schedules
     */
    public List<CrawlSchedule> getByIsEnabled(boolean isEnabled) {
        return scheduleQueryPort.findByIsEnabled(isEnabled);
    }

    /**
     * Gets schedules by workflow ID and enabled status
     *
     * @param workflowId the workflow ID
     * @param isEnabled the enabled status
     * @return list of schedules
     */
    public List<CrawlSchedule> getByWorkflowIdAndIsEnabled(Long workflowId, boolean isEnabled) {
        return scheduleQueryPort.findByWorkflowIdAndIsEnabled(
                WorkflowId.of(workflowId),
                isEnabled
        );
    }

    /**
     * Gets all schedules
     *
     * @return list of all schedules
     */
    public List<CrawlSchedule> getAll() {
        return scheduleQueryPort.findAll();
    }

    /**
     * Gets schedules by filter criteria
     * Replaces multiple conditional query methods with a single unified method
     *
     * @param filter the filter criteria
     * @return list of schedules matching the filter
     */
    public List<CrawlSchedule> getByFilter(ScheduleFilter filter) {
        if (filter.hasBothFilters()) {
            return scheduleQueryPort.findByWorkflowIdAndIsEnabled(
                    filter.getWorkflowId().orElseThrow(),
                    filter.getIsEnabled().orElseThrow()
            );
        } else if (filter.hasWorkflowId()) {
            return scheduleQueryPort.findByWorkflowId(
                    filter.getWorkflowId().orElseThrow()
            );
        } else if (filter.hasIsEnabled()) {
            return scheduleQueryPort.findByIsEnabled(
                    filter.getIsEnabled().orElseThrow()
            );
        } else {
            return scheduleQueryPort.findAll();
        }
    }
}
