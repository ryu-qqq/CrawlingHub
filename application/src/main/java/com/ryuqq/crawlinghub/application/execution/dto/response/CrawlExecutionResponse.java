package com.ryuqq.crawlinghub.application.execution.dto.response;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.Instant;

/**
 * CrawlExecution 목록 조회 Response DTO
 *
 * <p>CrawlExecution 기본 응답 정보
 *
 * @param crawlExecutionId CrawlExecution ID
 * @param crawlTaskId CrawlTask ID
 * @param crawlSchedulerId CrawlScheduler ID
 * @param sellerId 셀러 ID
 * @param status 상태
 * @param httpStatusCode HTTP 상태 코드 (nullable)
 * @param durationMs 실행 시간 (밀리초, nullable)
 * @param startedAt 실행 시작 시각 (UTC Instant)
 * @param completedAt 실행 완료 시각 (UTC Instant, nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlExecutionResponse(
        Long crawlExecutionId,
        Long crawlTaskId,
        Long crawlSchedulerId,
        Long sellerId,
        CrawlExecutionStatus status,
        Integer httpStatusCode,
        Long durationMs,
        Instant startedAt,
        Instant completedAt) {}
