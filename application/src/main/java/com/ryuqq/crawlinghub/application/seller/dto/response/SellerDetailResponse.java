package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;

/**
 * SellerDetailResponse - 셀러 상세 조회 응답 DTO (확장)
 *
 * <p><strong>확장된 필드 (v2) ⭐</strong></p>
 * <ul>
 *   <li>🆕 productCountHistories (PageResponse)</li>
 *   <li>🆕 scheduleInfo (크롤링 스케줄)</li>
 *   <li>🆕 scheduleHistories (PageResponse)</li>
 * </ul>
 *
 * @param sellerId 셀러 ID
 * @param sellerCode 셀러 코드
 * @param sellerName 셀러명
 * @param status 상태
 * @param totalProductCount 총 상품 수
 * @param productCountHistories 상품 수 변경 이력 (PageResponse) ⭐
 * @param scheduleInfo 크롤링 스케줄 정보 ⭐
 * @param scheduleHistories 크롤링 실행 이력 (PageResponse) ⭐
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record SellerDetailResponse(
    Long sellerId,
    String sellerCode,
    String sellerName,
    String status,
    Integer totalProductCount,
    PageResponse<ProductCountHistoryResponse> productCountHistories, // ⭐
    ScheduleInfoResponse scheduleInfo, // ⭐
    PageResponse<ScheduleHistoryResponse> scheduleHistories // ⭐
) {}
