package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

import java.time.LocalDateTime;

/**
 * Seller 조회 응답 DTO
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Plain Java Record 사용</li>
 *   <li>✅ Response DTO는 Domain Aggregate로부터 생성</li>
 *   <li>✅ 불변 객체 (Java 21 Record)</li>
 * </ul>
 *
 * @param sellerId Seller ID (String)
 * @param name Seller 이름
 * @param status Seller 상태
 * @param crawlingIntervalDays 크롤링 주기 (일 단위)
 * @param totalProductCount 총 상품 수
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public record SellerResponse(
        String sellerId,
        String name,
        SellerStatus status,
        Integer crawlingIntervalDays,
        Integer totalProductCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
