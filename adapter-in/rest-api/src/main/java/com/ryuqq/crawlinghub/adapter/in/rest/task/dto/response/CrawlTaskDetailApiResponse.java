package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response;

import java.util.Map;

/**
 * CrawlTask Detail API Response
 *
 * <p>크롤 태스크 상세 조회 API 응답 DTO
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>crawlTaskId: 크롤 태스크 ID
 *   <li>crawlSchedulerId: 크롤 스케줄러 ID
 *   <li>sellerId: 셀러 ID
 *   <li>status: 상태 (PENDING, RUNNING, SUCCESS, FAILED, CANCELLED)
 *   <li>taskType: 태스크 유형 (META, MINI_SHOP, DETAIL, OPTION)
 *   <li>retryCount: 재시도 횟수
 *   <li>baseUrl: 기본 URL
 *   <li>path: 경로
 *   <li>queryParams: 쿼리 파라미터
 *   <li>fullUrl: 전체 URL
 *   <li>createdAt: 생성 시각 (ISO-8601 형식)
 *   <li>updatedAt: 수정 시각 (ISO-8601 형식)
 * </ul>
 *
 * @param crawlTaskId 크롤 태스크 ID
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param status 상태
 * @param taskType 태스크 유형
 * @param retryCount 재시도 횟수
 * @param baseUrl 기본 URL
 * @param path 경로
 * @param queryParams 쿼리 파라미터
 * @param fullUrl 전체 URL
 * @param createdAt 생성 시각 (ISO-8601 형식)
 * @param updatedAt 수정 시각 (ISO-8601 형식)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskDetailApiResponse(
        Long crawlTaskId,
        Long crawlSchedulerId,
        Long sellerId,
        String status,
        String taskType,
        int retryCount,
        String baseUrl,
        String path,
        Map<String, String> queryParams,
        String fullUrl,
        String createdAt,
        String updatedAt) {}
