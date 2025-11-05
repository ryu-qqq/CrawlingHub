package com.ryuqq.crawlinghub.application.product.sync.dto.command;

/**
 * 상품 변경 감지 Command
 *
 * <p>크롤링된 상품 데이터가 변경되었는지 감지합니다.
 *
 * @param productId 상품 ID
 * @param currentHash 현재 데이터 해시값
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record DetectChangeCommand(
    Long productId,
    String currentHash
) {
    public DetectChangeCommand {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("상품 ID는 필수입니다");
        }
        if (currentHash == null || currentHash.isBlank()) {
            throw new IllegalArgumentException("현재 해시값은 필수입니다");
        }
    }
}
