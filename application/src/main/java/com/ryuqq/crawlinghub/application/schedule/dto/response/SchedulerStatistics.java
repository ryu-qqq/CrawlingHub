package com.ryuqq.crawlinghub.application.schedule.dto.response;

/**
 * 스케줄러 통계 정보
 *
 * @param totalTasks 전체 태스크 수
 * @param successTasks 성공 태스크 수
 * @param failedTasks 실패 태스크 수
 * @param successRate 성공률 (0.0 ~ 1.0)
 * @param avgDurationMs 평균 실행 시간 (밀리초)
 * @author development-team
 * @since 1.0.0
 */
public record SchedulerStatistics(
        long totalTasks,
        long successTasks,
        long failedTasks,
        double successRate,
        long avgDurationMs) {}
