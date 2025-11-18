package com.ryuqq.crawlinghub.application.seller.dto.command;

/**
 * Seller 등록 Command
 *
 * @param name Seller 이름
 */
public record RegisterSellerCommand(
        String name
) {
    public static RegisterSellerCommand of(String name) {
        return new RegisterSellerCommand(name);
    }
}
