package com.ryuqq.crawlinghub.domain.seller.vo;

import java.time.LocalDateTime;

/**
 * Seller 조회 조건 Value Object
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Record 타입 사용 (불변성 보장)</li>
 *   <li>✅ Lombok 금지</li>
 *   <li>✅ of() 정적 팩토리 메서드만 제공</li>
 *   <li>✅ Null-safe 설계 (모든 필드 nullable)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Seller ID로 조회
 * SellerSearchCriteria.of("seller_12345", null, null, null, null);
 *
 * // 활성 Seller만 조회
 * SellerSearchCriteria.of(null, null, true, null, null);
 *
 * // 복합 조건 조회
 * SellerSearchCriteria.of("seller_12345", "무신사", true, startDate, endDate);
 * }</pre>
 *
 * @param sellerId Seller ID (String, 예: "seller_12345")
 * @param sellerName Seller 이름 (like 검색)
 * @param active 활성 상태 (true/false/null)
 * @param fromCreatedAt 생성일 범위 시작
 * @param toCreatedAt 생성일 범위 종료
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public record SellerSearchCriteria(
    String sellerId,
    String sellerName,
    Boolean active,
    LocalDateTime fromCreatedAt,
    LocalDateTime toCreatedAt
) {
    /**
     * 정적 팩토리 메서드
     *
     * @param sellerId Seller ID (nullable)
     * @param sellerName Seller 이름 (nullable)
     * @param active 활성 상태 (nullable)
     * @param fromCreatedAt 생성일 범위 시작 (nullable)
     * @param toCreatedAt 생성일 범위 종료 (nullable)
     * @return SellerSearchCriteria
     */
    public static SellerSearchCriteria of(
        String sellerId,
        String sellerName,
        Boolean active,
        LocalDateTime fromCreatedAt,
        LocalDateTime toCreatedAt
    ) {
        return new SellerSearchCriteria(sellerId, sellerName, active, fromCreatedAt, toCreatedAt);
    }
}
