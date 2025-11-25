package com.ryuqq.crawlinghub.application.crawl.task.dto.response;

import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskType;

import java.time.LocalDateTime;

/**
 * CrawlTask Response DTO
 *
 * <p>CrawlTask 기본 응답 정보
 *
 * @param crawlTaskId CrawlTask ID
 * @param crawlSchedulerId 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param requestUrl 요청 URL
 * @param status 상태
 * @param taskType 태스크 유형
 * @param retryCount 재시도 횟수
 * @param createdAt 생성 시각
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskResponse(
        Long crawlTaskId,
        Long crawlSchedulerId,
        Long sellerId,
        String requestUrl,
        CrawlTaskStatus status,
        CrawlTaskType taskType,
        int retryCount,
        LocalDateTime createdAt
) {}
