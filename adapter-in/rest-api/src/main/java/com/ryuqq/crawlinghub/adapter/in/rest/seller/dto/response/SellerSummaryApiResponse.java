package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

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
 * @author development-team
 * @since 1.0.0
 */
public record SellerSummaryApiResponse(
        Long sellerId, String mustItSellerName, String sellerName, String status) {}
