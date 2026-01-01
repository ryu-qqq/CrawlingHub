package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Seller Summary API Response
 *
 * <p>셀러 요약 정보 API 응답 DTO
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>sellerId: 셀러 ID
 *   <li>mustItSellerName: 머스트잇 셀러명
 *   <li>sellerName: 커머스 셀러명
 *   <li>status: 셀러 상태 (ACTIVE/INACTIVE)
 *   <li>createdAt: 생성 시각
 *   <li>updatedAt: 수정 시각
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <ul>
 *   <li>셀러 목록 조회 (ListSellers)
 *   <li>셀러 검색 결과
 *   <li>드롭다운/셀렉트 박스 데이터
 * </ul>
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러명
 * @param sellerName 커머스 셀러명
 * @param status 셀러 상태
 * @param createdAt 생성 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param updatedAt 수정 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "셀러 요약 정보")
public record SellerSummaryApiResponse(
        @Schema(description = "셀러 ID", example = "1") Long sellerId,
        @Schema(description = "머스트잇 셀러명", example = "MUSTIT_SELLER") String mustItSellerName,
        @Schema(description = "커머스 셀러명", example = "상점명") String sellerName,
        @Schema(description = "셀러 상태", example = "ACTIVE") String status,
        @Schema(description = "생성 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String createdAt,
        @Schema(description = "수정 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String updatedAt) {}
