package com.ryuqq.crawlinghub.application.execution.assembler;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionResult;
import com.ryuqq.crawlinghub.domain.execution.vo.ExecutionDuration;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawlExecution Aggregate ↔ Response DTO 변환기
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlExecutionAssembler {

    /**
     * ListCrawlExecutionsQuery → CrawlExecutionCriteria 변환
     *
     * @param query 목록 조회 쿼리
     * @return Domain 조회 조건 객체
     */
    public CrawlExecutionCriteria toCriteria(ListCrawlExecutionsQuery query) {
        return new CrawlExecutionCriteria(
                query.crawlTaskId() != null ? CrawlTaskId.of(query.crawlTaskId()) : null,
                query.crawlSchedulerId() != null
                        ? CrawlSchedulerId.of(query.crawlSchedulerId())
                        : null,
                query.sellerId() != null ? SellerId.of(query.sellerId()) : null,
                query.statuses(),
                toInstant(query.from()),
                toInstant(query.to()),
                query.page(),
                query.size());
    }

    /**
     * CrawlExecution Aggregate → CrawlExecutionResponse 변환
     *
     * @param execution CrawlExecution Aggregate
     * @return CrawlExecution 응답 DTO
     */
    public CrawlExecutionResponse toResponse(CrawlExecution execution) {
        ExecutionDuration duration = execution.getDuration();
        CrawlExecutionResult result = execution.getResult();

        return new CrawlExecutionResponse(
                execution.getId().value(),
                execution.getCrawlTaskId().value(),
                execution.getCrawlSchedulerId().value(),
                execution.getSellerId().value(),
                execution.getStatus(),
                result != null ? result.httpStatusCode() : null,
                duration != null ? duration.durationMs() : null,
                duration != null ? duration.startedAt() : null,
                duration != null ? duration.completedAt() : null,
                execution.getCreatedAt(),
                null); // updatedAt: domain 모델에 미존재, 향후 추가 예정
    }

    /**
     * CrawlExecution Aggregate → CrawlExecutionDetailResponse 변환
     *
     * @param execution CrawlExecution Aggregate
     * @return CrawlExecution 상세 응답 DTO
     */
    public CrawlExecutionDetailResponse toDetailResponse(CrawlExecution execution) {
        ExecutionDuration duration = execution.getDuration();
        CrawlExecutionResult result = execution.getResult();

        return new CrawlExecutionDetailResponse(
                execution.getId().value(),
                execution.getCrawlTaskId().value(),
                execution.getCrawlSchedulerId().value(),
                execution.getSellerId().value(),
                execution.getStatus(),
                result != null ? result.httpStatusCode() : null,
                result != null ? result.responseBody() : null,
                result != null ? result.errorMessage() : null,
                duration != null ? duration.durationMs() : null,
                duration != null ? duration.startedAt() : null,
                duration != null ? duration.completedAt() : null,
                execution.getCreatedAt(),
                null); // updatedAt: domain 모델에 미존재, 향후 추가 예정
    }

    /**
     * CrawlExecution 목록 → CrawlExecutionResponse 목록
     *
     * @param executions CrawlExecution Aggregate 목록
     * @return 응답 목록
     */
    public List<CrawlExecutionResponse> toResponses(List<CrawlExecution> executions) {
        return executions.stream().map(this::toResponse).toList();
    }

    /**
     * CrawlExecution 목록 + 총 개수 → PageResponse 변환
     *
     * @param executions CrawlExecution 목록
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return 페이징된 응답
     */
    public PageResponse<CrawlExecutionResponse> toPageResponse(
            List<CrawlExecution> executions, int page, int size, long totalElements) {
        List<CrawlExecutionResponse> content = toResponses(executions);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = (page == 0);
        boolean last = (page >= totalPages - 1) || totalElements == 0;

        return PageResponse.of(content, page, size, totalElements, totalPages, first, last);
    }

    // === Private Helper Methods ===

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
