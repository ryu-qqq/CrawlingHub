package com.ryuqq.crawlinghub.application.crawl.schedule.dto.response;

import com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus;

import java.time.LocalDateTime;

/**
 * 크롤링 스케줄 응답 DTO
 *
 * @param scheduleId        스케줄 ID
 * @param sellerId          셀러 ID
 * @param cronExpression    Cron 표현식
 * @param status            스케줄 상태
 * @param nextExecutionTime 다음 실행 시간
 * @param lastExecutedAt    마지막 실행 시간
 * @param createdAt         생성 시간
 * @param updatedAt         수정 시간
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record ScheduleResponse(
    Long scheduleId,
    Long sellerId,
    String cronExpression,
    ScheduleStatus status,
    LocalDateTime nextExecutionTime,
    LocalDateTime lastExecutedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
