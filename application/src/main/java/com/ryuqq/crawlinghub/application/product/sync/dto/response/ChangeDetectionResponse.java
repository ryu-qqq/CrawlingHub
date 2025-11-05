package com.ryuqq.crawlinghub.application.product.sync.dto.response;

/**
 * 변경 감지 결과 Response
 *
 * @param productId 상품 ID
 * @param hasChanged 변경 여부
 * @param previousHash 이전 해시값
 * @param currentHash 현재 해시값
 * @param previousVersion 이전 버전
 * @param currentVersion 현재 버전
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record ChangeDetectionResponse(
    Long productId,
    boolean hasChanged,
    String previousHash,
    String currentHash,
    Integer previousVersion,
    Integer currentVersion
) {
}
