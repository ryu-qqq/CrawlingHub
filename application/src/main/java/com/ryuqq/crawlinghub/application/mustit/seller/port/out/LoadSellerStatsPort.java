package com.ryuqq.crawlinghub.application.mustit.seller.port.out;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

/**
 * 셀러 통계 조회 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 * GetSellerDetailUseCase에서 통계 정보 조회 시 사용됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface LoadSellerStatsPort {

    /**
     * 셀러 통계 조회
     *
     * @param sellerId 셀러 ID (null 불가)
     * @return 셀러 통계 정보
     * @throws IllegalArgumentException sellerId가 null인 경우
     */
    SellerStats getSellerStats(MustitSellerId sellerId);

    /**
     * 셀러 통계 정보 DTO
     *
     * @param totalSchedules  총 스케줄 수
     * @param activeSchedules 활성 스케줄 수
     * @param totalCrawlTasks 총 크롤링 태스크 수
     * @param successfulTasks 성공한 태스크 수
     * @param failedTasks     실패한 태스크 수
     */
    record SellerStats(
        Integer totalSchedules,
        Integer activeSchedules,
        Integer totalCrawlTasks,
        Integer successfulTasks,
        Integer failedTasks
    ) {
    }
}
