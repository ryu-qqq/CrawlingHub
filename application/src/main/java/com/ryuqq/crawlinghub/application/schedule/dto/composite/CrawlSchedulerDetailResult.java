package com.ryuqq.crawlinghub.application.schedule.dto.composite;

import java.time.Instant;
import java.util.List;

/**
 * 크롤 스케줄러 상세 조회 Composite Result
 *
 * <p>Composite 패턴으로 Persistence Layer에서 조립된 결과를 Application Layer에서 사용합니다.
 *
 * @param scheduler 스케줄러 기본 정보
 * @param seller 셀러 요약 정보 (nullable)
 * @param execution 실행 정보
 * @param statistics 통계 정보
 * @param recentTasks 최근 태스크 목록 (최대 10개)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerDetailResult(
        SchedulerInfo scheduler,
        SellerSummary seller,
        ExecutionInfo execution,
        SchedulerStatistics statistics,
        List<TaskSummary> recentTasks) {

    /**
     * 스케줄러 기본 정보
     *
     * @param id 스케줄러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 상태
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     */
    public record SchedulerInfo(
            Long id,
            String schedulerName,
            String cronExpression,
            String status,
            Instant createdAt,
            Instant updatedAt) {}

    /**
     * 셀러 요약 정보
     *
     * @param sellerId 셀러 ID
     * @param sellerName 셀러명
     * @param mustItSellerName 머스트잇 셀러명
     */
    public record SellerSummary(Long sellerId, String sellerName, String mustItSellerName) {}

    /**
     * 실행 정보
     *
     * @param lastExecutionTime 마지막 실행 시각
     * @param lastExecutionStatus 마지막 실행 상태
     */
    public record ExecutionInfo(Instant lastExecutionTime, String lastExecutionStatus) {}

    /**
     * 통계 정보
     *
     * @param totalTasks 전체 태스크 수
     * @param successTasks 성공 태스크 수
     * @param failedTasks 실패 태스크 수
     * @param successRate 성공률 (0.0 ~ 1.0)
     */
    public record SchedulerStatistics(
            long totalTasks, long successTasks, long failedTasks, double successRate) {}

    /**
     * 태스크 요약 정보
     *
     * @param taskId 태스크 ID
     * @param status 태스크 상태
     * @param taskType 태스크 유형
     * @param createdAt 생성 시각
     * @param completedAt 완료 시각 (null 가능)
     */
    public record TaskSummary(
            Long taskId, String status, String taskType, Instant createdAt, Instant completedAt) {}
}
