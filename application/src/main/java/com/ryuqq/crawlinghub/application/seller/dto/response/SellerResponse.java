package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

/**
 * Seller Response DTO
 *
 * @param sellerId Seller ID
 * @param name Seller 이름
 * @param status Seller 상태
 */
public record SellerResponse(
        Long sellerId,
        String name,
        SellerStatus status
) {
    public static SellerResponse of(Long sellerId, String name, SellerStatus status) {
        return new SellerResponse(sellerId, name, status);
    }
}
