package com.ryuqq.crawlinghub.application.seller.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Seller Detail Response
 *
 * <p>셀러 상세 응답 데이터 (단건 조회 결과)
 *
 * <p>기본 셀러 정보와 함께 연관된 스케줄러, 최근 태스크, 통계 정보를 포함합니다.
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러 이름
 * @param sellerName 셀러 이름
 * @param active 활성화 여부
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param schedulers 연관 스케줄러 목록
 * @param recentTasks 최근 태스크 목록 (최대 5개)
 * @param statistics 셀러 상세 통계
 * @author development-team
 * @since 1.0.0
 */
public record SellerDetailResponse(
        Long sellerId,
        String mustItSellerName,
        String sellerName,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        List<SchedulerSummary> schedulers,
        List<TaskSummary> recentTasks,
        SellerDetailStatistics statistics) {}
