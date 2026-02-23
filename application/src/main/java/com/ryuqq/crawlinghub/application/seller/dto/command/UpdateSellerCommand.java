package com.ryuqq.crawlinghub.application.seller.dto.command;

/**
 * Update Seller Command
 *
 * <p>셀러 수정 명령 데이터 (모든 필드 non-null)
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러 이름
 * @param sellerName 셀러 이름
 * @param active 활성화 여부
 * @author development-team
 * @since 1.0.0
 */
public record UpdateSellerCommand(
        long sellerId, String mustItSellerName, String sellerName, boolean active) {}
