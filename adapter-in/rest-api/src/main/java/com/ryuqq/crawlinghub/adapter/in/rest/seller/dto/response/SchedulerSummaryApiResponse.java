package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

/**
 * Scheduler Summary API Response
 *
 * <p>셀러 상세 조회 시 포함되는 스케줄러 요약 정보
 *
 * @param schedulerId 스케줄러 ID
 * @param schedulerName 스케줄러 이름
 * @param status 스케줄러 상태 (ACTIVE/INACTIVE)
 * @param cronExpression Cron 표현식
 * @param nextExecutionTime 다음 실행 예정 시각 (ISO-8601 형식)
 * @author development-team
 * @since 1.0.0
 */
public record SchedulerSummaryApiResponse(
        Long schedulerId,
        String schedulerName,
        String status,
        String cronExpression,
        String nextExecutionTime) {}
