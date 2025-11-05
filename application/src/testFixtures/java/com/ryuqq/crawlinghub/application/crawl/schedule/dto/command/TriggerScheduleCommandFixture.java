package com.ryuqq.crawlinghub.application.crawl.schedule.dto.command;

/**
 * TriggerScheduleCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class TriggerScheduleCommandFixture {

    private static final Long DEFAULT_SCHEDULE_ID = 1L;

    /**
     * 기본 TriggerScheduleCommand 생성
     *
     * @return TriggerScheduleCommand
     */
    public static TriggerScheduleCommand create() {
        return new TriggerScheduleCommand(DEFAULT_SCHEDULE_ID);
    }

    /**
     * 특정 스케줄 ID로 TriggerScheduleCommand 생성
     *
     * @param scheduleId 스케줄 ID
     * @return TriggerScheduleCommand
     */
    public static TriggerScheduleCommand createWithScheduleId(Long scheduleId) {
        return new TriggerScheduleCommand(scheduleId);
    }
}
