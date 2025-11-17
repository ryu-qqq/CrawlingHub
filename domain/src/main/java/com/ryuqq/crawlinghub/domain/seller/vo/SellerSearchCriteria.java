package com.ryuqq.crawlinghub.domain.seller.vo;

import java.time.LocalDateTime;

/**
 * Seller 조회 조건 Value Object
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Record 타입 사용 (불변성 보장)</li>
 *   <li>✅ Lombok 금지</li>
 *   <li>✅ 비즈니스 검증 로직 포함 가능</li>
 *   <li>✅ Null-safe 설계</li>
 * </ul>
 *
 * @param sellerId Seller ID (String, 예: "seller_12345")
 * @param sellerName Seller 이름 (like 검색)
 * @param active 활성 상태 (true/false/null)
 * @param createdAtFrom 생성일 범위 시작
 * @param createdAtTo 생성일 범위 종료
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public record SellerSearchCriteria(
    String sellerId,
    String sellerName,
    Boolean active,
    LocalDateTime createdAtFrom,
    LocalDateTime createdAtTo
) {
    /**
     * 모든 Seller 조회 (조건 없음)
     */
    public static SellerSearchCriteria all() {
        return new SellerSearchCriteria(null, null, null, null, null);
    }

    /**
     * Seller ID로 조회
     */
    public static SellerSearchCriteria bySellerId(String sellerId) {
        return new SellerSearchCriteria(sellerId, null, null, null, null);
    }

    /**
     * 활성 Seller만 조회
     */
    public static SellerSearchCriteria onlyActive() {
        return new SellerSearchCriteria(null, null, true, null, null);
    }
}
