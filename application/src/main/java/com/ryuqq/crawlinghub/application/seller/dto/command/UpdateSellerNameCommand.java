package com.ryuqq.crawlinghub.application.seller.dto.command;

/**
 * Seller 이름 변경 Command
 *
 * @param sellerId Seller ID
 * @param newName 새로운 Seller 이름
 */
public record UpdateSellerNameCommand(
        Long sellerId,
        String newName
) {
    public static UpdateSellerNameCommand of(Long sellerId, String newName) {
        return new UpdateSellerNameCommand(sellerId, newName);
    }
}
