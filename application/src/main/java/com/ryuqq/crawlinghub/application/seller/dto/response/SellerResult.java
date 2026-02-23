package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import java.time.Instant;

/**
 * 셀러 조회 결과 DTO (static from() 팩토리)
 *
 * @param id 셀러 ID
 * @param mustItSellerName 머스트잇 셀러명
 * @param sellerName 커머스 셀러명
 * @param status 셀러 상태
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author development-team
 * @since 1.0.0
 */
public record SellerResult(
        Long id,
        String mustItSellerName,
        String sellerName,
        String status,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * Seller Aggregate → SellerResult 변환
     *
     * @param seller 셀러 Aggregate
     * @return SellerResult
     */
    public static SellerResult from(Seller seller) {
        return new SellerResult(
                seller.getSellerIdValue(),
                seller.getMustItSellerNameValue(),
                seller.getSellerNameValue(),
                seller.getStatus().name(),
                seller.getCreatedAt(),
                seller.getUpdatedAt());
    }
}
