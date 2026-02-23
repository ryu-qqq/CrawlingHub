package com.ryuqq.crawlinghub.application.seller.dto.composite;

import java.time.Instant;
import java.util.List;

/**
 * 셀러 상세 조회 Composite Result
 *
 * <p>Composite 패턴으로 Persistence Layer에서 조립된 결과를 Application Layer에서 사용합니다.
 *
 * @param seller 셀러 기본 정보
 * @param schedulers 연관 스케줄러 요약 목록
 * @param recentTasks 최근 태스크 목록 (최대 5개)
 * @param statistics 셀러 통계 정보
 * @author development-team
 * @since 1.0.0
 */
public record SellerDetailResult(
        SellerInfo seller,
        List<SchedulerSummary> schedulers,
        List<TaskSummary> recentTasks,
        SellerStatistics statistics) {

    /**
     * 셀러 기본 정보
     *
     * @param sellerId 셀러 ID
     * @param mustItSellerName 머스트잇 셀러명
     * @param sellerName 커머스 셀러명
     * @param status 셀러 상태
     * @param productCount 상품 수
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     */
    public record SellerInfo(
            Long sellerId,
            String mustItSellerName,
            String sellerName,
            String status,
            int productCount,
            Instant createdAt,
            Instant updatedAt) {}

    /**
     * 스케줄러 요약 정보
     *
     * @param schedulerId 스케줄러 ID
     * @param schedulerName 스케줄러 이름
     * @param status 스케줄러 상태
     * @param cronExpression 크론 표현식
     */
    public record SchedulerSummary(
            Long schedulerId, String schedulerName, String status, String cronExpression) {}

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

    /**
     * 셀러 통계 정보
     *
     * @param totalProducts 전체 크롤링 상품 수
     * @param syncedProducts 동기화 완료 상품 수
     * @param pendingSyncProducts 동기화 대기 상품 수
     * @param successRate 성공률 (0.0 ~ 1.0)
     */
    public record SellerStatistics(
            long totalProducts,
            long syncedProducts,
            long pendingSyncProducts,
            double successRate) {}
}
