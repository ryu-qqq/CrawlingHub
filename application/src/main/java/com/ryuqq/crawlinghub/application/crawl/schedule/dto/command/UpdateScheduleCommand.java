package com.ryuqq.crawlinghub.application.crawl.schedule.dto.command;

/**
 * 크롤링 스케줄 수정 Command
 *
 * @param scheduleId     스케줄 ID (필수)
 * @param cronExpression 새로운 Cron 표현식 (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record UpdateScheduleCommand(
    Long scheduleId,
    String cronExpression
) {
    public UpdateScheduleCommand {
        if (scheduleId == null) {
            throw new IllegalArgumentException("스케줄 ID는 필수입니다");
        }
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("Cron 표현식은 필수입니다");
        }
    }
}
