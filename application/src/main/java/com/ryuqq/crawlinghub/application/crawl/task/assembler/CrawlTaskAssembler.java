package com.ryuqq.crawlinghub.application.crawl.task.assembler;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.crawl.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.crawl.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.domain.crawl.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlEndpoint;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CrawlTask Aggregate ↔ Response DTO 변환기
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskAssembler {

    /**
     * CrawlTask Aggregate → CrawlTaskResponse 변환
     *
     * @param crawlTask CrawlTask Aggregate
     * @return CrawlTask 응답 DTO
     */
    public CrawlTaskResponse toResponse(CrawlTask crawlTask) {
        return new CrawlTaskResponse(
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value(),
                crawlTask.getSellerId().value(),
                crawlTask.getEndpoint().toFullUrl(),
                crawlTask.getStatus(),
                crawlTask.getTaskType(),
                crawlTask.getRetryCount().value(),
                crawlTask.getCreatedAt()
        );
    }

    /**
     * CrawlTask Aggregate → CrawlTaskDetailResponse 변환
     *
     * @param crawlTask CrawlTask Aggregate
     * @return CrawlTask 상세 응답 DTO
     */
    public CrawlTaskDetailResponse toDetailResponse(CrawlTask crawlTask) {
        CrawlEndpoint endpoint = crawlTask.getEndpoint();
        return new CrawlTaskDetailResponse(
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value(),
                crawlTask.getSellerId().value(),
                crawlTask.getStatus(),
                crawlTask.getTaskType(),
                crawlTask.getRetryCount().value(),
                endpoint.baseUrl(),
                endpoint.path(),
                endpoint.queryParams(),
                endpoint.toFullUrl(),
                crawlTask.getCreatedAt(),
                crawlTask.getUpdatedAt()
        );
    }

    /**
     * CrawlTask 목록 → CrawlTaskResponse 목록
     *
     * @param crawlTasks CrawlTask Aggregate 목록
     * @return 응답 목록
     */
    public List<CrawlTaskResponse> toResponses(List<CrawlTask> crawlTasks) {
        return crawlTasks.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * CrawlTask 목록 + 총 개수 → PageResponse 변환
     *
     * @param crawlTasks CrawlTask 목록
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return 페이징된 응답
     */
    public PageResponse<CrawlTaskResponse> toPageResponse(
            List<CrawlTask> crawlTasks,
            int page,
            int size,
            long totalElements
    ) {
        List<CrawlTaskResponse> content = toResponses(crawlTasks);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = (page == 0);
        boolean last = (page >= totalPages - 1) || totalElements == 0;

        return PageResponse.of(content, page, size, totalElements, totalPages, first, last);
    }
}
