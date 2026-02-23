package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * CrawlTask API Response
 *
 * <p>크롤 태스크 조회 API 응답 DTO (기본 + 상세 필드 통합)
 *
 * @param crawlTaskId 크롤 태스크 ID
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param requestUrl 전체 요청 URL
 * @param baseUrl 기본 URL
 * @param path 경로
 * @param queryParams 쿼리 파라미터
 * @param status 상태
 * @param taskType 태스크 유형
 * @param retryCount 재시도 횟수
 * @param createdAt 생성 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param updatedAt 수정 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "크롤 태스크 조회 API 응답")
public record CrawlTaskApiResponse(
        @Schema(description = "크롤 태스크 ID", example = "1") Long crawlTaskId,
        @Schema(description = "크롤 스케줄러 ID", example = "1") Long crawlSchedulerId,
        @Schema(description = "셀러 ID", example = "100") Long sellerId,
        @Schema(description = "전체 요청 URL", example = "https://api.example.com/products?page=1")
                String requestUrl,
        @Schema(description = "기본 URL", example = "https://api.example.com") String baseUrl,
        @Schema(description = "경로", example = "/products") String path,
        @Schema(description = "쿼리 파라미터") Map<String, String> queryParams,
        @Schema(
                        description = "상태 (WAITING/PUBLISHED/RUNNING/SUCCESS/FAILED/RETRY/TIMEOUT)",
                        example = "WAITING")
                String status,
        @Schema(description = "태스크 유형 (META/MINI_SHOP/DETAIL/OPTION)", example = "META")
                String taskType,
        @Schema(description = "재시도 횟수", example = "0") int retryCount,
        @Schema(description = "생성 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String createdAt,
        @Schema(description = "수정 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String updatedAt) {}
