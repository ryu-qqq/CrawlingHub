package com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response;

import java.time.LocalDateTime;

/**
 * CrawlExecution 목록 조회 API Response DTO
 *
 * <p>CrawlExecution 기본 정보 응답
 *
 * @param crawlExecutionId CrawlExecution ID
 * @param crawlTaskId CrawlTask ID
 * @param crawlSchedulerId CrawlScheduler ID
 * @param sellerId 셀러 ID
 * @param status 상태 (RUNNING, SUCCESS, FAILED, TIMEOUT)
 * @param httpStatusCode HTTP 상태 코드 (nullable)
 * @param durationMs 실행 시간 (밀리초, nullable)
 * @param startedAt 실행 시작 시각
 * @param completedAt 실행 완료 시각 (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlExecutionApiResponse(
        Long crawlExecutionId,
        Long crawlTaskId,
        Long crawlSchedulerId,
        Long sellerId,
        String status,
        Integer httpStatusCode,
        Long durationMs,
        LocalDateTime startedAt,
        LocalDateTime completedAt) {}
