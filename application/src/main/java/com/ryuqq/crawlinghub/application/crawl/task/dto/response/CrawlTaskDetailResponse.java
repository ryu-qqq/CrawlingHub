package com.ryuqq.crawlinghub.application.crawl.task.dto.response;

import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * CrawlTask Detail Response DTO
 *
 * <p>CrawlTask 상세 응답 정보 (엔드포인트 상세 포함)
 *
 * @param crawlTaskId CrawlTask ID
 * @param crawlSchedulerId 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param status 상태
 * @param taskType 태스크 유형
 * @param retryCount 재시도 횟수
 * @param baseUrl 기본 URL
 * @param path 경로
 * @param queryParams 쿼리 파라미터
 * @param fullUrl 전체 URL
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskDetailResponse(
        Long crawlTaskId,
        Long crawlSchedulerId,
        Long sellerId,
        CrawlTaskStatus status,
        CrawlTaskType taskType,
        int retryCount,
        String baseUrl,
        String path,
        Map<String, String> queryParams,
        String fullUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
