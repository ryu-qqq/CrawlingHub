package com.ryuqq.crawlinghub.application.schedule.assembler;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
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
 *   <li>ClockHolder 의존 금지
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
     * SearchCrawlSchedulersQuery → CrawlSchedulerQueryCriteria (조회 조건 변환)
     *
     * <p>Application Layer Query DTO를 Domain VO로 변환
     *
     * @param query 스케줄러 검색 Query
     * @return Domain 조회 조건 객체
     */
    public CrawlSchedulerQueryCriteria toCriteria(SearchCrawlSchedulersQuery query) {
        SellerId sellerId = query.sellerId() != null ? SellerId.of(query.sellerId()) : null;

        return new CrawlSchedulerQueryCriteria(
                sellerId, query.status(), query.page(), query.size());
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
