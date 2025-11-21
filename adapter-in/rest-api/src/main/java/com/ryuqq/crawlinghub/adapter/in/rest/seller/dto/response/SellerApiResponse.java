package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import java.time.LocalDateTime;

/**
 * Seller API Response
 *
 * <p>셀러 상세 정보 API 응답 DTO
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>sellerId: 셀러 ID
 *   <li>mustItSellerName: 머스트잇 셀러명
 *   <li>sellerName: 커머스 셀러명
 *   <li>status: 셀러 상태 (ACTIVE/INACTIVE)
 *   <li>createdAt: 생성 일시
 *   <li>updatedAt: 수정 일시
 * </ul>
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러명
 * @param sellerName 커머스 셀러명
 * @param status 셀러 상태
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 * @author development-team
 * @since 1.0.0
 */
public record SellerApiResponse(
        Long sellerId,
        String mustItSellerName,
        String sellerName,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
