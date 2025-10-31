package com.ryuqq.crawlinghub.application.mustit.seller.assembler;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerStatsPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;

/**
 * 셀러 Assembler
 *
 * <p>Domain 객체와 DTO 간 변환을 담당합니다.
 * Law of Demeter를 준수하여 직접적인 getter 체이닝을 피합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public class SellerAssembler {

    /**
     * Domain → Response DTO 변환
     *
     * @param seller 도메인 셀러 객체 (null 불가)
     * @return SellerResponse
     * @throws IllegalArgumentException seller가 null인 경우
     */
    public static SellerResponse toResponse(MustitSeller seller) {
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null");
        }

        return new SellerResponse(
            seller.getIdValue(),
            seller.getSellerCode(),
            seller.getSellerName(),
            seller.getStatus(),
            seller.getTotalProductCount(),
            seller.getLastCrawledAt(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );
    }

    /**
     * Domain + Stats → DetailResponse DTO 변환
     *
     * @param seller 도메인 셀러 객체 (null 불가)
     * @param stats  셀러 통계 정보 (null 불가)
     * @return SellerDetailResponse
     * @throws IllegalArgumentException seller 또는 stats가 null인 경우
     */
    public static SellerDetailResponse toDetailResponse(
        MustitSeller seller,
        LoadSellerStatsPort.SellerStats stats
    ) {
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null");
        }
        if (stats == null) {
            throw new IllegalArgumentException("stats must not be null");
        }

        return new SellerDetailResponse(
            toResponse(seller),
            stats.totalSchedules(),
            stats.activeSchedules(),
            stats.totalCrawlTasks(),
            stats.successfulTasks(),
            stats.failedTasks()
        );
    }
}
