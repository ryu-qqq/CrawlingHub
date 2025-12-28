package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import java.util.List;

/**
 * Seller Detail API Response
 *
 * <p>셀러 상세 정보 API 응답 DTO (스케줄러, 최근 태스크, 통계 포함)
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>sellerId: 셀러 ID
 *   <li>mustItSellerName: 머스트잇 셀러명
 *   <li>sellerName: 커머스 셀러명
 *   <li>status: 셀러 상태 (ACTIVE/INACTIVE)
 *   <li>createdAt: 생성 일시 (ISO-8601 형식)
 *   <li>updatedAt: 수정 일시 (ISO-8601 형식)
 *   <li>schedulers: 연관 스케줄러 목록
 *   <li>recentTasks: 최근 태스크 목록 (최대 5개)
 *   <li>statistics: 셀러 상세 통계
 * </ul>
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러명
 * @param sellerName 커머스 셀러명
 * @param status 셀러 상태
 * @param createdAt 생성 일시 (ISO-8601 형식)
 * @param updatedAt 수정 일시 (ISO-8601 형식)
 * @param schedulers 연관 스케줄러 목록
 * @param recentTasks 최근 태스크 목록
 * @param statistics 셀러 상세 통계
 * @author development-team
 * @since 1.0.0
 */
public record SellerDetailApiResponse(
        Long sellerId,
        String mustItSellerName,
        String sellerName,
        String status,
        String createdAt,
        String updatedAt,
        List<SchedulerSummaryApiResponse> schedulers,
        List<TaskSummaryApiResponse> recentTasks,
        SellerDetailStatisticsApiResponse statistics) {}
