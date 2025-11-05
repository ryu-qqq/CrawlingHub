package com.ryuqq.crawlinghub.application.seller.dto.query;

import com.ryuqq.crawlinghub.domain.seller.SellerStatus;

import java.time.LocalDateTime;

/**
 * Seller Query DTO - Query 전용 DTO
 *
 * <p><strong>CQRS 패턴 적용 - Query 결과 전용 ⭐</strong></p>
 * <ul>
 *   <li>✅ QueryDSL DTO Projection으로 직접 생성</li>
 *   <li>✅ Domain Model을 거치지 않음 (성능 향상)</li>
 *   <li>✅ N+1 문제 방지</li>
 * </ul>
 *
 * <p><strong>사용처:</strong></p>
 * <ul>
 *   <li>LoadSellerPort의 findById, findByCode 메서드 반환 타입</li>
 *   <li>QueryAdapter에서 QueryDSL Projection으로 직접 생성</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record SellerQueryDto(
    Long id,
    String sellerCode,
    String sellerName,
    SellerStatus status,
    Integer totalProductCount,
    LocalDateTime lastCrawledAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

