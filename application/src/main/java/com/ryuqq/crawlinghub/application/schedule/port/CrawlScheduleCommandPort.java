package com.ryuqq.crawlinghub.application.schedule.port;

import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;

import java.util.List;
import java.util.Optional;

/**
 * Command Port for CrawlSchedule (CQRS Pattern)
 * Handles CUD operations and simple reads
 */
public interface CrawlScheduleCommandPort {

    /**
     * Saves a CrawlSchedule (create or update)
     *
     * @param schedule the schedule to save
     * @return the saved schedule with generated ID
     */
    CrawlSchedule save(CrawlSchedule schedule);

    /**
     * Finds a schedule by ID
     *
     * @param scheduleId the schedule ID
     * @return Optional containing the schedule if found
     */
    Optional<CrawlSchedule> findById(ScheduleId scheduleId);

    /**
     * Checks if a schedule exists
     *
     * @param scheduleId the schedule ID
     * @return true if exists, false otherwise
     */
    boolean existsById(ScheduleId scheduleId);

    /**
     * Deletes a schedule by ID
     *
     * @param scheduleId the schedule ID
     */
    void deleteById(ScheduleId scheduleId);

    /**
     * Saves schedule input parameters
     *
     * @param params the parameters to save
     * @return the saved parameters
     */
    List<ScheduleInputParam> saveInputParams(List<ScheduleInputParam> params);

    /**
     * Deletes all input parameters for a schedule
     *
     * @param scheduleId the schedule ID
     */
    void deleteInputParamsByScheduleId(Long scheduleId);

    /**
     * Finds input parameters for a schedule
     *
     * @param scheduleId the schedule ID
     * @return list of input parameters
     */
    List<ScheduleInputParam> findInputParamsByScheduleId(Long scheduleId);
}
