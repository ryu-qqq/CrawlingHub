package com.ryuqq.crawlinghub.application.task.dto.response;

import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import java.time.Instant;
import java.util.Map;

/**
 * CrawlTask 조회 결과 DTO (static from() 팩토리)
 *
 * <p>기본 정보 + 상세 엔드포인트 정보를 통합한 결과 DTO
 *
 * @param crawlTaskId CrawlTask ID
 * @param crawlSchedulerId 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param requestUrl 전체 요청 URL
 * @param baseUrl 기본 URL
 * @param path 경로
 * @param queryParams 쿼리 파라미터
 * @param status 상태
 * @param taskType 태스크 유형
 * @param retryCount 재시도 횟수
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskResult(
        Long crawlTaskId,
        Long crawlSchedulerId,
        Long sellerId,
        String requestUrl,
        String baseUrl,
        String path,
        Map<String, String> queryParams,
        String status,
        String taskType,
        int retryCount,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * CrawlTask Aggregate → CrawlTaskResult 변환
     *
     * @param crawlTask CrawlTask Aggregate
     * @return CrawlTaskResult
     */
    public static CrawlTaskResult from(CrawlTask crawlTask) {
        CrawlEndpoint endpoint = crawlTask.getEndpoint();
        return new CrawlTaskResult(
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue(),
                crawlTask.getSellerIdValue(),
                endpoint.toFullUrl(),
                endpoint.baseUrl(),
                endpoint.path(),
                endpoint.queryParams(),
                crawlTask.getStatus().name(),
                crawlTask.getTaskType().name(),
                crawlTask.getRetryCountValue(),
                crawlTask.getCreatedAt(),
                crawlTask.getUpdatedAt());
    }
}
