package com.ryuqq.crawlinghub.application.seller.dto.command;

/**
 * Update Seller Command
 *
 * <p>셀러 수정 명령 데이터
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러 이름 (선택)
 * @param sellerName 셀러 이름 (선택)
 * @param active 활성화 여부 (선택)
 * @author development-team
 * @since 1.0.0
 */
public record UpdateSellerCommand(
        Long sellerId, String mustItSellerName, String sellerName, Boolean active) {}
