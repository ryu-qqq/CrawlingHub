package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.LocalDateTime;

/**
 * 셀러 상세 응답 DTO.
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerId 머스트잇 셀러 식별자
 * @param sellerName 셀러명
 * @param status 상태
 * @param activeSchedulerCount 활성 스케줄 수
 * @param totalSchedulerCount 총 스케줄 수
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 */
public record SellerDetailResponse(
        Long sellerId,
        String mustItSellerId,
        String sellerName,
        SellerStatus status,
        Integer activeSchedulerCount,
        Integer totalSchedulerCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
