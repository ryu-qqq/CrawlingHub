package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response;

import java.util.List;

/**
 * CrawlScheduler Detail API Response
 *
 * <p>크롤 스케줄러 상세 정보 API 응답 DTO (단건 조회용)
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 * @param createdAt 생성 시각 (ISO-8601 형식)
 * @param updatedAt 수정 시각 (ISO-8601 형식)
 * @param seller 셀러 요약 정보
 * @param execution 실행 정보
 * @param statistics 통계 정보
 * @param recentTasks 최근 태스크 목록 (최대 10개)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerDetailApiResponse(
        Long crawlSchedulerId,
        String schedulerName,
        String cronExpression,
        String status,
        String createdAt,
        String updatedAt,
        SellerSummaryApiResponse seller,
        ExecutionInfoApiResponse execution,
        SchedulerStatisticsApiResponse statistics,
        List<TaskSummaryApiResponse> recentTasks) {

    /**
     * 셀러 요약 정보 API 응답
     *
     * @param sellerId 셀러 ID
     * @param sellerName 셀러 이름
     * @param mustItSellerName 머스트잇 셀러 이름
     */
    public record SellerSummaryApiResponse(
            Long sellerId, String sellerName, String mustItSellerName) {}

    /**
     * 실행 정보 API 응답
     *
     * @param nextExecutionTime 다음 실행 예정 시각 (ISO-8601 형식)
     * @param lastExecutionTime 마지막 실행 시각 (ISO-8601 형식)
     * @param lastExecutionStatus 마지막 실행 상태
     */
    public record ExecutionInfoApiResponse(
            String nextExecutionTime, String lastExecutionTime, String lastExecutionStatus) {}

    /**
     * 스케줄러 통계 정보 API 응답
     *
     * @param totalTasks 전체 태스크 수
     * @param successTasks 성공 태스크 수
     * @param failedTasks 실패 태스크 수
     * @param successRate 성공률 (0.0 ~ 1.0)
     * @param avgDurationMs 평균 실행 시간 (밀리초)
     */
    public record SchedulerStatisticsApiResponse(
            long totalTasks,
            long successTasks,
            long failedTasks,
            double successRate,
            long avgDurationMs) {}

    /**
     * 태스크 요약 정보 API 응답
     *
     * @param taskId 태스크 ID
     * @param status 태스크 상태
     * @param taskType 태스크 유형
     * @param createdAt 생성 시각 (ISO-8601 형식)
     * @param completedAt 완료 시각 (ISO-8601 형식, null 가능)
     */
    public record TaskSummaryApiResponse(
            Long taskId, String status, String taskType, String createdAt, String completedAt) {}
}
