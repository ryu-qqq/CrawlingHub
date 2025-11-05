package com.ryuqq.crawlinghub.application.seller.dto.response;

import java.time.LocalDateTime;

/**
 * ScheduleInfoResponse - 크롤링 스케줄 정보 응답 DTO
 *
 * @param scheduleId 스케줄 ID
 * @param cronExpression Cron 표현식
 * @param status 스케줄 상태
 * @param nextExecutionTime 다음 실행 시간
 * @param createdAt 생성 일시
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ScheduleInfoResponse(
    Long scheduleId,
    String cronExpression,
    String status,
    LocalDateTime nextExecutionTime,
    LocalDateTime createdAt
) {}

