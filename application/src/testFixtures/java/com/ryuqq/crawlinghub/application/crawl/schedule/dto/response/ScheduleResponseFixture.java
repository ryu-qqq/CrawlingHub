package com.ryuqq.crawlinghub.application.crawl.schedule.dto.response;

import com.ryuqq.crawlinghub.domain.crawl.schedule.ScheduleStatus;

import java.time.LocalDateTime;

/**
 * ScheduleResponse Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class ScheduleResponseFixture {

    private static final Long DEFAULT_SCHEDULE_ID = 1L;
    private static final Long DEFAULT_SELLER_ID = 1L;
    private static final String DEFAULT_CRON_EXPRESSION = "0 0 * * *";
    private static final ScheduleStatus DEFAULT_STATUS = ScheduleStatus.ACTIVE;
    private static final LocalDateTime DEFAULT_NEXT_EXECUTION_TIME = LocalDateTime.of(2025, 11, 1, 0, 0);
    private static final LocalDateTime DEFAULT_LAST_EXECUTED_AT = LocalDateTime.of(2025, 10, 31, 0, 0);
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2025, 10, 30, 10, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2025, 10, 30, 10, 0);

    /**
     * 기본 ScheduleResponse 생성
     *
     * @return ScheduleResponse
     */
    public static ScheduleResponse create() {
        return new ScheduleResponse(
            DEFAULT_SCHEDULE_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_CRON_EXPRESSION,
            DEFAULT_STATUS,
            DEFAULT_NEXT_EXECUTION_TIME,
            DEFAULT_LAST_EXECUTED_AT,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 특정 스케줄 ID로 ScheduleResponse 생성
     *
     * @param scheduleId 스케줄 ID
     * @return ScheduleResponse
     */
    public static ScheduleResponse createWithScheduleId(Long scheduleId) {
        return new ScheduleResponse(
            scheduleId,
            DEFAULT_SELLER_ID,
            DEFAULT_CRON_EXPRESSION,
            DEFAULT_STATUS,
            DEFAULT_NEXT_EXECUTION_TIME,
            DEFAULT_LAST_EXECUTED_AT,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 특정 셀러 ID로 ScheduleResponse 생성
     *
     * @param sellerId 셀러 ID
     * @return ScheduleResponse
     */
    public static ScheduleResponse createWithSellerId(Long sellerId) {
        return new ScheduleResponse(
            DEFAULT_SCHEDULE_ID,
            sellerId,
            DEFAULT_CRON_EXPRESSION,
            DEFAULT_STATUS,
            DEFAULT_NEXT_EXECUTION_TIME,
            DEFAULT_LAST_EXECUTED_AT,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 특정 상태로 ScheduleResponse 생성
     *
     * @param status 스케줄 상태
     * @return ScheduleResponse
     */
    public static ScheduleResponse createWithStatus(ScheduleStatus status) {
        return new ScheduleResponse(
            DEFAULT_SCHEDULE_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_CRON_EXPRESSION,
            status,
            DEFAULT_NEXT_EXECUTION_TIME,
            DEFAULT_LAST_EXECUTED_AT,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 마지막 실행 시간 없이 ScheduleResponse 생성
     *
     * @return ScheduleResponse
     */
    public static ScheduleResponse createWithoutLastExecutedAt() {
        return new ScheduleResponse(
            DEFAULT_SCHEDULE_ID,
            DEFAULT_SELLER_ID,
            DEFAULT_CRON_EXPRESSION,
            DEFAULT_STATUS,
            DEFAULT_NEXT_EXECUTION_TIME,
            null,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 완전한 커스텀 ScheduleResponse 생성
     *
     * @param scheduleId        스케줄 ID
     * @param sellerId          셀러 ID
     * @param cronExpression    Cron 표현식
     * @param status            스케줄 상태
     * @param nextExecutionTime 다음 실행 시간
     * @param lastExecutedAt    마지막 실행 시간
     * @param createdAt         생성 시간
     * @param updatedAt         수정 시간
     * @return ScheduleResponse
     */
    public static ScheduleResponse createCustom(
        Long scheduleId,
        Long sellerId,
        String cronExpression,
        ScheduleStatus status,
        LocalDateTime nextExecutionTime,
        LocalDateTime lastExecutedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ScheduleResponse(
            scheduleId,
            sellerId,
            cronExpression,
            status,
            nextExecutionTime,
            lastExecutedAt,
            createdAt,
            updatedAt
        );
    }
}
