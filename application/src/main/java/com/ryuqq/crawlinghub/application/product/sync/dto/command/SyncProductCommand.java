package com.ryuqq.crawlinghub.application.product.sync.dto.command;

/**
 * 상품 동기화 Command
 *
 * <p>크롤링된 상품 데이터를 내부 시스템으로 동기화합니다.
 *
 * @param productId 상품 ID
 * @param forceSync 강제 동기화 여부 (변경 없어도 동기화)
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record SyncProductCommand(
    Long productId,
    boolean forceSync
) {
    public SyncProductCommand {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("상품 ID는 필수입니다");
        }
    }

    /**
     * 기본값(forceSync=false)으로 생성
     */
    public static SyncProductCommand of(Long productId) {
        return new SyncProductCommand(productId, false);
    }

    /**
     * 강제 동기화로 생성
     */
    public static SyncProductCommand forceSync(Long productId) {
        return new SyncProductCommand(productId, true);
    }
}
