package com.ryuqq.crawlinghub.application.seller.dto.response;

import java.time.Instant;

/**
 * Scheduler Summary
 *
 * <p>셀러 상세 조회 시 포함되는 스케줄러 요약 정보
 *
 * @param schedulerId 스케줄러 ID
 * @param schedulerName 스케줄러 이름
 * @param status 스케줄러 상태
 * @param cronExpression 크론 표현식
 * @param nextExecutionTime 다음 실행 예정 시각
 * @author development-team
 * @since 1.0.0
 */
public record SchedulerSummary(
        Long schedulerId,
        String schedulerName,
        String status,
        String cronExpression,
        Instant nextExecutionTime) {}
