package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response;

import java.time.LocalDateTime;

/**
 * CrawlTask API Response
 *
 * <p>크롤 태스크 목록 조회 API 응답 DTO
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>crawlTaskId: 크롤 태스크 ID
 *   <li>crawlSchedulerId: 크롤 스케줄러 ID
 *   <li>sellerId: 셀러 ID
 *   <li>requestUrl: 요청 URL
 *   <li>status: 상태 (PENDING, RUNNING, SUCCESS, FAILED, CANCELLED)
 *   <li>taskType: 태스크 유형 (META, MINI_SHOP, DETAIL, OPTION)
 *   <li>retryCount: 재시도 횟수
 *   <li>createdAt: 생성 시각
 * </ul>
 *
 * @param crawlTaskId 크롤 태스크 ID
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param requestUrl 요청 URL
 * @param status 상태
 * @param taskType 태스크 유형
 * @param retryCount 재시도 횟수
 * @param createdAt 생성 시각
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskApiResponse(
        Long crawlTaskId,
        Long crawlSchedulerId,
        Long sellerId,
        String requestUrl,
        String status,
        String taskType,
        int retryCount,
        LocalDateTime createdAt) {}
