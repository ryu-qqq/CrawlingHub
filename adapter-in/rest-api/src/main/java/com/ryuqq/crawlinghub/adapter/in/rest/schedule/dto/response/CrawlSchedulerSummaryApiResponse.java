package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * CrawlScheduler Summary API Response
 *
 * <p>크롤 스케줄러 요약 정보 API 응답 DTO (목록 조회용)
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>crawlSchedulerId: 크롤 스케줄러 ID
 *   <li>sellerId: 셀러 ID
 *   <li>schedulerName: 스케줄러 이름
 *   <li>cronExpression: 크론 표현식
 *   <li>status: 스케줄러 상태 (ACTIVE/INACTIVE)
 *   <li>createdAt: 생성 시각
 *   <li>updatedAt: 수정 시각
 * </ul>
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 * @param createdAt 생성 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param updatedAt 수정 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "크롤 스케줄러 요약 정보")
public record CrawlSchedulerSummaryApiResponse(
        @Schema(description = "크롤 스케줄러 ID", example = "1") Long crawlSchedulerId,
        @Schema(description = "셀러 ID", example = "100") Long sellerId,
        @Schema(description = "스케줄러 이름", example = "DAILY_CRAWL") String schedulerName,
        @Schema(description = "크론 표현식", example = "0 0 2 * * ?") String cronExpression,
        @Schema(description = "스케줄러 상태", example = "ACTIVE") String status,
        @Schema(description = "생성 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String createdAt,
        @Schema(description = "수정 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String updatedAt) {}
