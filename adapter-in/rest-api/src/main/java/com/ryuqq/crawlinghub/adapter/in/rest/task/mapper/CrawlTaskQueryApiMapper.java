package com.ryuqq.crawlinghub.adapter.in.rest.task.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.CrawlTaskSearchParams;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskQueryApiMapper - CrawlTask Query REST API ↔ Application Layer 변환
 *
 * <p>CrawlTask Query 요청/응답에 대한 DTO 변환을 담당합니다.
 *
 * <p><strong>변환 방향:</strong>
 *
 * <ul>
 *   <li>API Query Request → Application SearchParams (Controller → Application)
 *   <li>Application Result → API Response (Application → Controller)
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 *   <li>페이징 응답 변환 (CrawlTaskPageResult → PageApiResponse)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskQueryApiMapper {

    /**
     * SearchCrawlTasksApiRequest → CrawlTaskSearchParams 변환
     *
     * @param request REST API 크롤 태스크 목록 조회 요청
     * @return Application Layer 크롤 태스크 검색 파라미터
     */
    public CrawlTaskSearchParams toSearchParams(SearchCrawlTasksApiRequest request) {
        return new CrawlTaskSearchParams(
                request.crawlSchedulerIds(),
                request.sellerIds(),
                request.statuses(),
                request.taskTypes(),
                request.createdFrom(),
                request.createdTo(),
                request.page(),
                request.size());
    }

    /**
     * CrawlTaskResult → CrawlTaskApiResponse 변환
     *
     * @param result Application Layer 크롤 태스크 결과
     * @return REST API 크롤 태스크 응답
     */
    public CrawlTaskApiResponse toApiResponse(CrawlTaskResult result) {
        return new CrawlTaskApiResponse(
                result.crawlTaskId(),
                result.crawlSchedulerId(),
                result.sellerId(),
                result.requestUrl(),
                result.baseUrl(),
                result.path(),
                result.queryParams(),
                result.status(),
                result.taskType(),
                result.retryCount(),
                format(result.createdAt()),
                format(result.updatedAt()));
    }

    /**
     * CrawlTaskPageResult → PageApiResponse 변환
     *
     * @param pageResult Application Layer 페이지 결과
     * @return REST API 페이지 응답
     */
    public PageApiResponse<CrawlTaskApiResponse> toPageApiResponse(CrawlTaskPageResult pageResult) {
        List<CrawlTaskApiResponse> content =
                pageResult.results().stream().map(this::toApiResponse).toList();
        PageMeta meta = pageResult.pageMeta();
        return PageApiResponse.of(content, meta.page(), meta.size(), meta.totalElements());
    }
}
