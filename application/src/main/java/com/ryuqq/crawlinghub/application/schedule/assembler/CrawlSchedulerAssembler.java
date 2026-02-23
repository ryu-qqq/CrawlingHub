package com.ryuqq.crawlinghub.application.schedule.assembler;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerPageResult;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResult;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler Assembler
 *
 * <p><strong>책임</strong>: Domain → Response 변환 (단방향)
 *
 * <p><strong>금지</strong>:
 *
 * <ul>
 *   <li>Command → Domain 변환 금지 (Factory 책임)
 *   <li>TimeProvider 의존 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerAssembler {

    /**
     * CrawlScheduler Aggregate → CrawlSchedulerResponse 변환.
     *
     * @param crawlScheduler 크롤 스케줄러 Aggregate
     * @return 크롤 스케줄러 응답 DTO
     */
    public CrawlSchedulerResponse toResponse(CrawlScheduler crawlScheduler) {
        return new CrawlSchedulerResponse(
                crawlScheduler.getCrawlSchedulerIdValue(),
                crawlScheduler.getSellerIdValue(),
                crawlScheduler.getSchedulerNameValue(),
                crawlScheduler.getCronExpressionValue(),
                crawlScheduler.getStatus(),
                crawlScheduler.getCreatedAt(),
                crawlScheduler.getUpdatedAt());
    }

    /**
     * CrawlScheduler Aggregate → CrawlSchedulerResult 변환
     *
     * @param scheduler 크롤 스케줄러 Aggregate
     * @return CrawlSchedulerResult
     */
    public CrawlSchedulerResult toResult(CrawlScheduler scheduler) {
        return CrawlSchedulerResult.from(scheduler);
    }

    /**
     * CrawlScheduler 목록 → CrawlSchedulerResult 목록
     *
     * @param schedulers CrawlScheduler Aggregate 목록
     * @return CrawlSchedulerResult 목록
     */
    public List<CrawlSchedulerResult> toResults(List<CrawlScheduler> schedulers) {
        return schedulers.stream().map(this::toResult).toList();
    }

    /**
     * CrawlScheduler 목록 + 페이징 정보 → CrawlSchedulerPageResult 변환
     *
     * @param schedulers 스케줄러 목록
     * @param page 현재 페이지 번호
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return CrawlSchedulerPageResult
     */
    public CrawlSchedulerPageResult toPageResult(
            List<CrawlScheduler> schedulers, int page, int size, long totalElements) {
        List<CrawlSchedulerResult> results = toResults(schedulers);
        PageMeta pageMeta = PageMeta.of(page, size, totalElements);
        return CrawlSchedulerPageResult.of(results, pageMeta);
    }

    /**
     * CrawlScheduler 목록 → CrawlSchedulerResponse 목록
     *
     * @param schedulers CrawlScheduler Aggregate 목록
     * @return 응답 목록
     */
    public List<CrawlSchedulerResponse> toResponses(List<CrawlScheduler> schedulers) {
        return schedulers.stream().map(this::toResponse).toList();
    }

    /**
     * CrawlScheduler 목록 + 총 개수 → PageResponse<CrawlSchedulerResponse>
     *
     * <p>페이징 메타데이터를 계산하여 PageResponse로 변환
     *
     * @param schedulers CrawlScheduler 목록
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return 페이징된 응답
     */
    public PageResponse<CrawlSchedulerResponse> toPageResponse(
            List<CrawlScheduler> schedulers, int page, int size, long totalElements) {
        List<CrawlSchedulerResponse> content = toResponses(schedulers);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = (page == 0);
        boolean last = (page >= totalPages - 1);

        return PageResponse.of(content, page, size, totalElements, totalPages, first, last);
    }
}
