package com.ryuqq.crawlinghub.application.dashboard.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Dashboard 통계 Response DTO
 *
 * <p>관리자 대시보드에서 표시할 전반적인 통계 정보를 제공합니다.
 *
 * @param todayTaskStats 오늘 태스크 통계
 * @param weeklySuccessRates 최근 7일 성공률
 * @param scheduleStats 스케줄 통계
 * @param outboxStats Outbox 통계
 * @param recentFailedTasks 최근 실패 태스크 목록
 * @author development-team
 * @since 1.0.0
 */
public record DashboardStatsResponse(
        TodayTaskStats todayTaskStats,
        List<DailySuccessRate> weeklySuccessRates,
        ScheduleStats scheduleStats,
        OutboxStats outboxStats,
        List<FailedTaskSummary> recentFailedTasks) {

    /**
     * 오늘 태스크 통계
     *
     * @param total 전체 태스크 수
     * @param success 성공 수
     * @param failed 실패 수
     * @param inProgress 진행 중 수
     * @param waiting 대기 중 수
     * @param successRate 성공률 (0.0 ~ 1.0)
     */
    public record TodayTaskStats(
            long total,
            long success,
            long failed,
            long inProgress,
            long waiting,
            double successRate) {

        public static TodayTaskStats of(
                long total, long success, long failed, long inProgress, long waiting) {
            double successRate = total > 0 ? (double) success / total : 0.0;
            return new TodayTaskStats(total, success, failed, inProgress, waiting, successRate);
        }
    }

    /**
     * 일별 성공률
     *
     * @param date 날짜 (YYYY-MM-DD 형식)
     * @param total 전체 태스크 수
     * @param success 성공 수
     * @param successRate 성공률 (0.0 ~ 1.0)
     */
    public record DailySuccessRate(String date, long total, long success, double successRate) {

        public static DailySuccessRate of(String date, long total, long success) {
            double successRate = total > 0 ? (double) success / total : 0.0;
            return new DailySuccessRate(date, total, success, successRate);
        }
    }

    /**
     * 스케줄 통계
     *
     * @param total 전체 스케줄 수
     * @param active 활성 스케줄 수
     * @param inactive 비활성 스케줄 수
     */
    public record ScheduleStats(long total, long active, long inactive) {}

    /**
     * Outbox 통계
     *
     * @param pending 대기 중 수 (PENDING)
     * @param sent 발행 완료 수 (SENT)
     * @param failed 실패 수 (FAILED)
     */
    public record OutboxStats(long pending, long sent, long failed) {}

    /**
     * 최근 실패 태스크 요약
     *
     * @param taskId 태스크 ID
     * @param schedulerId 스케줄러 ID
     * @param taskType 태스크 유형
     * @param status 현재 상태
     * @param failedAt 실패 시각
     */
    public record FailedTaskSummary(
            long taskId, long schedulerId, String taskType, String status, Instant failedAt) {}
}
