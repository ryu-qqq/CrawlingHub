package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

/**
 * 셀러 상세 정보 응답 DTO (통계 포함)
 *
 * @param seller             셀러 기본 정보
 * @param totalSchedules     총 스케줄 수
 * @param activeSchedules    활성 스케줄 수
 * @param totalCrawlTasks    총 크롤링 태스크 수
 * @param successfulTasks    성공한 태스크 수
 * @param failedTasks        실패한 태스크 수
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record SellerDetailResponse(
    SellerResponse seller,
    Integer totalSchedules,
    Integer activeSchedules,
    Integer totalCrawlTasks,
    Integer successfulTasks,
    Integer failedTasks
) {
    /**
     * 성공률 계산
     *
     * @return 성공률 (0.0 ~ 100.0)
     */
    public double getSuccessRate() {
        if (totalCrawlTasks == null || totalCrawlTasks == 0) {
            return 0.0;
        }
        return (successfulTasks * 100.0) / totalCrawlTasks;
    }
}
