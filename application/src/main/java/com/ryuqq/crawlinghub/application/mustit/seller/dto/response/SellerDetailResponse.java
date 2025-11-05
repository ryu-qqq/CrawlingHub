package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;

/**
 * SellerDetailResponse - 셀러 상세 조회 응답 DTO (확장)
 *
 * <p><strong>확장된 필드 (v2) ⭐</strong></p>
 * <ul>
 *   <li>🆕 productCountHistories (PageResponse) - 상품 수 변경 이력</li>
 *   <li>🆕 scheduleInfo (크롤링 스케줄) - 활성 스케줄 정보</li>
 *   <li>🆕 scheduleHistories (PageResponse) - 크롤링 실행 이력</li>
 * </ul>
 *
 * <p>기존 필드 (호환성 유지):
 * <ul>
 *   <li>seller - 셀러 기본 정보</li>
 *   <li>totalSchedules - 총 스케줄 수</li>
 *   <li>activeSchedules - 활성 스케줄 수</li>
 *   <li>totalCrawlTasks - 총 크롤링 태스크 수</li>
 *   <li>successfulTasks - 성공한 태스크 수</li>
 *   <li>failedTasks - 실패한 태스크 수</li>
 * </ul>
 *
 * @param seller 셀러 기본 정보
 * @param totalSchedules 총 스케줄 수
 * @param activeSchedules 활성 스케줄 수
 * @param totalCrawlTasks 총 크롤링 태스크 수
 * @param successfulTasks 성공한 태스크 수
 * @param failedTasks 실패한 태스크 수
 * @param productCountHistories 상품 수 변경 이력 (PageResponse) ⭐
 * @param scheduleInfo 크롤링 스케줄 정보 ⭐
 * @param scheduleHistories 크롤링 실행 이력 (PageResponse) ⭐
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record SellerDetailResponse(
    SellerResponse seller,
    Integer totalSchedules,
    Integer activeSchedules,
    Integer totalCrawlTasks,
    Integer successfulTasks,
    Integer failedTasks,
    PageResponse<ProductCountHistoryResponse> productCountHistories,
    ScheduleInfoResponse scheduleInfo,
    PageResponse<ScheduleHistoryResponse> scheduleHistories
) {
    /**
     * 기존 호환성을 위한 생성자 (v1)
     *
     * @param seller 셀러 기본 정보
     * @param totalSchedules 총 스케줄 수
     * @param activeSchedules 활성 스케줄 수
     * @param totalCrawlTasks 총 크롤링 태스크 수
     * @param successfulTasks 성공한 태스크 수
     * @param failedTasks 실패한 태스크 수
     */
    public SellerDetailResponse(
        SellerResponse seller,
        Integer totalSchedules,
        Integer activeSchedules,
        Integer totalCrawlTasks,
        Integer successfulTasks,
        Integer failedTasks
    ) {
        this(
            seller,
            totalSchedules,
            activeSchedules,
            totalCrawlTasks,
            successfulTasks,
            failedTasks,
            PageResponse.empty(0, 10),
            null,
            PageResponse.empty(0, 10)
        );
    }

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
