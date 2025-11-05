package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.PageApiResponse;

/**
 * SellerDetailApiResponse - 셀러 상세 API 응답 DTO (확장)
 *
 * <p><strong>🆕 확장된 필드 (v2) ⭐</strong></p>
 * <ul>
 *   <li>productCountHistories (PageApiResponse)</li>
 *   <li>scheduleInfo (스케줄 정보)</li>
 *   <li>scheduleHistories (PageApiResponse)</li>
 * </ul>
 *
 * @param sellerId 셀러 ID
 * @param sellerCode 셀러 코드
 * @param sellerName 셀러명
 * @param status 상태
 * @param totalProductCount 총 상품 수
 * @param productCountHistories 상품 수 변경 이력 (PageApiResponse) ⭐
 * @param scheduleInfo 스케줄 정보 ⭐
 * @param scheduleHistories 스케줄 실행 이력 (PageApiResponse) ⭐
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record SellerDetailApiResponse(
    Long sellerId,
    String sellerCode,
    String sellerName,
    String status,
    Integer totalProductCount,
    PageApiResponse<ProductCountHistoryApiResponse> productCountHistories, // ⭐
    ScheduleInfoApiResponse scheduleInfo, // ⭐
    PageApiResponse<ScheduleHistoryApiResponse> scheduleHistories // ⭐
) {}

