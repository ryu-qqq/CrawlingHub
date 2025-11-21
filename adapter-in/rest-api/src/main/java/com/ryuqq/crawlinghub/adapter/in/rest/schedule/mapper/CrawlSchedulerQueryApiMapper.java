package com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query.SearchCrawlSchedulersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerQueryApiMapper - CrawlScheduler Query REST API ↔ Application Layer 변환
 *
 * <p>CrawlScheduler Query 요청/응답에 대한 DTO 변환을 담당합니다.
 *
 * <p><strong>변환 방향:</strong>
 *
 * <ul>
 *   <li>API Query Request → Application Query (Controller → Application)
 *   <li>Application Response → API Response (Application → Controller)
 * </ul>
 *
 * <p><strong>CQRS 패턴 적용:</strong>
 *
 * <ul>
 *   <li>Query: SearchCrawlSchedulers 요청 변환
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 *   <li>Enum 변환 (String ↔ SchedulerStatus)
 *   <li>페이징 응답 변환 (PageResponse → PageApiResponse)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerQueryApiMapper {

    /**
     * SearchCrawlSchedulersApiRequest → SearchCrawlSchedulersQuery 변환
     *
     * @param request REST API 크롤 스케줄러 목록 조회 요청
     * @return Application Layer 크롤 스케줄러 목록 조회 쿼리
     */
    public SearchCrawlSchedulersQuery toQuery(SearchCrawlSchedulersApiRequest request) {
        SchedulerStatus status =
                (request.status() != null && !request.status().isBlank())
                        ? SchedulerStatus.valueOf(request.status())
                        : null;

        return new SearchCrawlSchedulersQuery(
                request.sellerId(), status, request.page(), request.size());
    }

    /**
     * CrawlSchedulerResponse → CrawlSchedulerApiResponse 변환
     *
     * @param appResponse Application Layer 크롤 스케줄러 응답
     * @return REST API 크롤 스케줄러 응답
     */
    public CrawlSchedulerApiResponse toApiResponse(CrawlSchedulerResponse appResponse) {
        return new CrawlSchedulerApiResponse(
                appResponse.crawlSchedulerId(),
                appResponse.sellerId(),
                appResponse.schedulerName(),
                appResponse.cronExpression(),
                appResponse.status().name(),
                appResponse.createdAt(),
                appResponse.updatedAt());
    }

    /**
     * CrawlSchedulerResponse → CrawlSchedulerSummaryApiResponse 변환
     *
     * @param appResponse Application Layer 크롤 스케줄러 응답
     * @return REST API 크롤 스케줄러 요약 응답
     */
    public CrawlSchedulerSummaryApiResponse toSummaryApiResponse(
            CrawlSchedulerResponse appResponse) {
        return new CrawlSchedulerSummaryApiResponse(
                appResponse.crawlSchedulerId(),
                appResponse.sellerId(),
                appResponse.schedulerName(),
                appResponse.cronExpression(),
                appResponse.status().name());
    }

    /**
     * PageResponse<CrawlSchedulerResponse> → PageApiResponse<CrawlSchedulerSummaryApiResponse> 변환
     *
     * <p>Application Layer의 페이지 응답을 REST API Layer의 페이지 응답으로 변환합니다.
     *
     * @param appPageResponse Application Layer 페이지 응답
     * @return REST API 페이지 응답
     */
    public PageApiResponse<CrawlSchedulerSummaryApiResponse> toPageApiResponse(
            PageResponse<CrawlSchedulerResponse> appPageResponse) {
        return PageApiResponse.from(appPageResponse, this::toSummaryApiResponse);
    }
}
