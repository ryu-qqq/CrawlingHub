package com.ryuqq.crawlinghub.adapter.in.rest.task.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskDetailApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.GetCrawlTaskQuery;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskQueryApiMapper - CrawlTask Query REST API ↔ Application Layer 변환
 *
 * <p>CrawlTask Query 요청/응답에 대한 DTO 변환을 담당합니다.
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
 *   <li>Query: ListCrawlTasks, GetCrawlTask 요청 변환
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 *   <li>Enum 변환 (String ↔ CrawlTaskStatus)
 *   <li>페이징 응답 변환 (PageResponse → PageApiResponse)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskQueryApiMapper {

    /**
     * SearchCrawlTasksApiRequest → ListCrawlTasksQuery 변환
     *
     * @param request REST API 크롤 태스크 목록 조회 요청
     * @return Application Layer 크롤 태스크 목록 조회 쿼리
     */
    public ListCrawlTasksQuery toQuery(SearchCrawlTasksApiRequest request) {
        CrawlTaskStatus status =
                (request.status() != null && !request.status().isBlank())
                        ? CrawlTaskStatus.valueOf(request.status())
                        : null;

        return new ListCrawlTasksQuery(
                request.crawlSchedulerId(), status, request.page(), request.size());
    }

    /**
     * Long → GetCrawlTaskQuery 변환
     *
     * @param crawlTaskId 크롤 태스크 ID
     * @return Application Layer 크롤 태스크 단건 조회 쿼리
     */
    public GetCrawlTaskQuery toGetQuery(Long crawlTaskId) {
        return new GetCrawlTaskQuery(crawlTaskId);
    }

    /**
     * CrawlTaskResponse → CrawlTaskApiResponse 변환
     *
     * @param appResponse Application Layer 크롤 태스크 응답
     * @return REST API 크롤 태스크 응답
     */
    public CrawlTaskApiResponse toApiResponse(CrawlTaskResponse appResponse) {
        return new CrawlTaskApiResponse(
                appResponse.crawlTaskId(),
                appResponse.crawlSchedulerId(),
                appResponse.sellerId(),
                appResponse.requestUrl(),
                appResponse.status().name(),
                appResponse.taskType().name(),
                appResponse.retryCount(),
                appResponse.createdAt());
    }

    /**
     * CrawlTaskDetailResponse → CrawlTaskDetailApiResponse 변환
     *
     * @param appResponse Application Layer 크롤 태스크 상세 응답
     * @return REST API 크롤 태스크 상세 응답
     */
    public CrawlTaskDetailApiResponse toDetailApiResponse(CrawlTaskDetailResponse appResponse) {
        return new CrawlTaskDetailApiResponse(
                appResponse.crawlTaskId(),
                appResponse.crawlSchedulerId(),
                appResponse.sellerId(),
                appResponse.status().name(),
                appResponse.taskType().name(),
                appResponse.retryCount(),
                appResponse.baseUrl(),
                appResponse.path(),
                appResponse.queryParams(),
                appResponse.fullUrl(),
                appResponse.createdAt(),
                appResponse.updatedAt());
    }

    /**
     * PageResponse → PageApiResponse 변환
     *
     * <p>Application Layer의 페이지 응답을 REST API Layer의 페이지 응답으로 변환합니다.
     *
     * @param appPageResponse Application Layer 페이지 응답
     * @return REST API 페이지 응답
     */
    public PageApiResponse<CrawlTaskApiResponse> toPageApiResponse(
            PageResponse<CrawlTaskResponse> appPageResponse) {
        return PageApiResponse.from(appPageResponse, this::toApiResponse);
    }
}
