package com.ryuqq.crawlinghub.application.schedule.dto.command;

/**
 * 크롤링 스케줄 생성 Command
 *
 * @param sellerId       셀러 ID (필수)
 * @param cronExpression Cron 표현식 (필수, 예: "0 0 * * *" - 매일 자정)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record CreateScheduleCommand(
    Long sellerId,
    String cronExpression
) {
    public CreateScheduleCommand {
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 필수입니다");
        }
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("Cron 표현식은 필수입니다");
        }
    }
}
