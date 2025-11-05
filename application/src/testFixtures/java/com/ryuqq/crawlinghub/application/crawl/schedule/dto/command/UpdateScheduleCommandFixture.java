package com.ryuqq.crawlinghub.application.crawl.schedule.dto.command;

import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateScheduleCommand;

/**
 * UpdateScheduleCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class UpdateScheduleCommandFixture {

    private static final Long DEFAULT_SCHEDULE_ID = 1L;
    private static final String DEFAULT_CRON_EXPRESSION = "0 0 * * *";

    /**
     * 기본 UpdateScheduleCommand 생성
     *
     * @return UpdateScheduleCommand
     */
    public static UpdateScheduleCommand create() {
        return new UpdateScheduleCommand(
            DEFAULT_SCHEDULE_ID,
            DEFAULT_CRON_EXPRESSION
        );
    }

    /**
     * 특정 스케줄 ID로 UpdateScheduleCommand 생성
     *
     * @param scheduleId 스케줄 ID
     * @return UpdateScheduleCommand
     */
    public static UpdateScheduleCommand createWithScheduleId(Long scheduleId) {
        return new UpdateScheduleCommand(
            scheduleId,
            DEFAULT_CRON_EXPRESSION
        );
    }

    /**
     * 특정 Cron 표현식으로 UpdateScheduleCommand 생성
     *
     * @param cronExpression Cron 표현식
     * @return UpdateScheduleCommand
     */
    public static UpdateScheduleCommand createWithCronExpression(String cronExpression) {
        return new UpdateScheduleCommand(
            DEFAULT_SCHEDULE_ID,
            cronExpression
        );
    }

    /**
     * 완전한 커스텀 UpdateScheduleCommand 생성
     *
     * @param scheduleId     스케줄 ID
     * @param cronExpression Cron 표현식
     * @return UpdateScheduleCommand
     */
    public static UpdateScheduleCommand createCustom(
        Long scheduleId,
        String cronExpression
    ) {
        return new UpdateScheduleCommand(scheduleId, cronExpression);
    }
}
