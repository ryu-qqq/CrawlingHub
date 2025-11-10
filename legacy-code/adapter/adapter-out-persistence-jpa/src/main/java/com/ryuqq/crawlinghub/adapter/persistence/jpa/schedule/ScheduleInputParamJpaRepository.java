package com.ryuqq.crawlinghub.adapter.persistence.jpa.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * JPA Repository for ScheduleInputParam
 * Provides CRUD operations and query methods for schedule input parameters
 */
public interface ScheduleInputParamJpaRepository extends JpaRepository<ScheduleInputParamEntity, Long> {

    /**
     * Finds all input parameters for a schedule
     * @param scheduleId the schedule ID
     * @return list of input parameters ordered by param_key
     */
    @Query("SELECT p FROM ScheduleInputParamEntity p " +
           "WHERE p.scheduleId = :scheduleId " +
           "ORDER BY p.paramKey")
    List<ScheduleInputParamEntity> findByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * Finds input parameters for multiple schedules (for batch loading)
     * @param scheduleIds list of schedule IDs
     * @return list of input parameters ordered by schedule_id, param_key
     */
    @Query("SELECT p FROM ScheduleInputParamEntity p " +
           "WHERE p.scheduleId IN :scheduleIds " +
           "ORDER BY p.scheduleId, p.paramKey")
    List<ScheduleInputParamEntity> findByScheduleIdIn(@Param("scheduleIds") List<Long> scheduleIds);

    /**
     * Deletes all input parameters for a schedule
     * @param scheduleId the schedule ID
     */
    @Modifying
    @Query("DELETE FROM ScheduleInputParamEntity p WHERE p.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") Long scheduleId);
}
