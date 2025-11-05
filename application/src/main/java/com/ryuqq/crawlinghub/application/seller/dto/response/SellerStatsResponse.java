package com.ryuqq.crawlinghub.application.seller.dto.response;

/**
 * 셀러 통계 정보 DTO
 *
 * @param totalSchedules  총 스케줄 수
 * @param activeSchedules 활성 스케줄 수
 * @param totalCrawlTasks 총 크롤링 태스크 수
 * @param successfulTasks 성공한 태스크 수
 * @param failedTasks     실패한 태스크 수
 */
public record SellerStatsResponse(
    Integer totalSchedules,
    Integer activeSchedules,
    Integer totalCrawlTasks,
    Integer successfulTasks,
    Integer failedTasks
) {
}
