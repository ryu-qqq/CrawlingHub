package com.ryuqq.crawlinghub.application.execution.dto.response;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.LocalDateTime;

/**
 * CrawlExecution 상세 조회 Response DTO
 *
 * <p>CrawlExecution 상세 응답 정보 (결과 데이터 포함)
 *
 * @param crawlExecutionId CrawlExecution ID
 * @param crawlTaskId CrawlTask ID
 * @param crawlSchedulerId CrawlScheduler ID
 * @param sellerId 셀러 ID
 * @param status 상태
 * @param httpStatusCode HTTP 상태 코드 (nullable)
 * @param responseBody 응답 본문 (nullable, 성공 시 크롤링 결과)
 * @param errorMessage 에러 메시지 (nullable)
 * @param durationMs 실행 시간 (밀리초, nullable)
 * @param startedAt 실행 시작 시각
 * @param completedAt 실행 완료 시각 (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlExecutionDetailResponse(
        Long crawlExecutionId,
        Long crawlTaskId,
        Long crawlSchedulerId,
        Long sellerId,
        CrawlExecutionStatus status,
        Integer httpStatusCode,
        String responseBody,
        String errorMessage,
        Long durationMs,
        LocalDateTime startedAt,
        LocalDateTime completedAt) {}
