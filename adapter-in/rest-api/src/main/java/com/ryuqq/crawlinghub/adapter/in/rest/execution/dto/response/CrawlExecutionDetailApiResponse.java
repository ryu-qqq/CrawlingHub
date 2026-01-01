package com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * CrawlExecution 상세 조회 API Response DTO
 *
 * <p>CrawlExecution 상세 정보 응답 (결과 데이터 포함)
 *
 * @param crawlExecutionId CrawlExecution ID
 * @param crawlTaskId CrawlTask ID
 * @param crawlSchedulerId CrawlScheduler ID
 * @param sellerId 셀러 ID
 * @param status 상태 (RUNNING, SUCCESS, FAILED, TIMEOUT)
 * @param httpStatusCode HTTP 상태 코드 (nullable)
 * @param responseBody 응답 본문 (nullable, 성공 시 크롤링 결과)
 * @param errorMessage 에러 메시지 (nullable)
 * @param durationMs 실행 시간 (밀리초, nullable)
 * @param startedAt 실행 시작 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param completedAt 실행 완료 시각 (yyyy-MM-dd HH:mm:ss 형식, nullable)
 * @param createdAt 생성 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param updatedAt 수정 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "CrawlExecution 상세 조회 응답")
public record CrawlExecutionDetailApiResponse(
        @Schema(description = "CrawlExecution ID", example = "1") Long crawlExecutionId,
        @Schema(description = "CrawlTask ID", example = "1") Long crawlTaskId,
        @Schema(description = "CrawlScheduler ID", example = "1") Long crawlSchedulerId,
        @Schema(description = "셀러 ID", example = "100") Long sellerId,
        @Schema(description = "상태", example = "SUCCESS") String status,
        @Schema(description = "HTTP 상태 코드", example = "200") Integer httpStatusCode,
        @Schema(description = "응답 본문") String responseBody,
        @Schema(description = "에러 메시지") String errorMessage,
        @Schema(description = "실행 시간 (밀리초)", example = "1500") Long durationMs,
        @Schema(description = "실행 시작 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String startedAt,
        @Schema(description = "실행 완료 시각 (Asia/Seoul)", example = "2025-01-15 09:30:01")
                String completedAt,
        @Schema(description = "생성 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String createdAt,
        @Schema(description = "수정 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String updatedAt) {}
