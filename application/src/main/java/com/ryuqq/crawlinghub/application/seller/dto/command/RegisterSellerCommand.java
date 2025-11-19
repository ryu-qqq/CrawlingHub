package com.ryuqq.crawlinghub.application.seller.dto.command;

/**
 * 셀러 등록 UseCase 입력 DTO.
 *
 * @param mustItSellerId 머스트잇 셀러 식별자
 * @param sellerName 셀러명
 */
public record RegisterSellerCommand(
    Long mustItSellerId,
    String sellerName
) {

    public RegisterSellerCommand {
        if (mustItSellerId == null) {
            throw new IllegalArgumentException("mustItSellerId must not be null");
        }
        if (sellerName == null || sellerName.isBlank()) {
            throw new IllegalArgumentException("sellerName must not be blank");
        }
    }
}

