package com.ryuqq.crawlinghub.adapter.in.rest.execution.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.query.SearchCrawlExecutionsApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response.CrawlExecutionApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response.CrawlExecutionDetailApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.dto.query.GetCrawlExecutionQuery;
import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawlExecution Query API Mapper
 *
 * <p>API DTO ↔ Application DTO 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlExecutionQueryApiMapper {

    /**
     * SearchCrawlExecutionsApiRequest → ListCrawlExecutionsQuery 변환
     *
     * @param request API 요청 DTO
     * @return Application 쿼리 DTO
     */
    public ListCrawlExecutionsQuery toQuery(SearchCrawlExecutionsApiRequest request) {
        List<CrawlExecutionStatus> statuses = parseStatuses(request.statuses());

        return new ListCrawlExecutionsQuery(
                request.crawlTaskId(),
                request.crawlSchedulerId(),
                request.sellerId(),
                statuses,
                request.from(),
                request.to(),
                request.page(),
                request.size());
    }

    /**
     * 상태 문자열 목록 → CrawlExecutionStatus Enum 목록 변환
     *
     * @param statusStrings 상태 문자열 목록
     * @return CrawlExecutionStatus Enum 목록 (null이거나 빈 리스트면 null)
     * @throws IllegalArgumentException 유효하지 않은 상태값이 포함된 경우
     */
    private List<CrawlExecutionStatus> parseStatuses(List<String> statusStrings) {
        if (statusStrings == null || statusStrings.isEmpty()) {
            return null;
        }
        return statusStrings.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(this::parseStatus)
                .toList();
    }

    private CrawlExecutionStatus parseStatus(String status) {
        try {
            return CrawlExecutionStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    /**
     * crawlExecutionId → GetCrawlExecutionQuery 변환
     *
     * @param crawlExecutionId CrawlExecution ID
     * @return Application 쿼리 DTO
     */
    public GetCrawlExecutionQuery toGetQuery(Long crawlExecutionId) {
        return new GetCrawlExecutionQuery(crawlExecutionId);
    }

    /**
     * CrawlExecutionResponse → CrawlExecutionApiResponse 변환
     *
     * @param appResponse Application 응답 DTO
     * @return API 응답 DTO
     */
    public CrawlExecutionApiResponse toApiResponse(CrawlExecutionResponse appResponse) {
        return new CrawlExecutionApiResponse(
                appResponse.crawlExecutionId(),
                appResponse.crawlTaskId(),
                appResponse.crawlSchedulerId(),
                appResponse.sellerId(),
                appResponse.status() != null ? appResponse.status().name() : null,
                appResponse.httpStatusCode(),
                appResponse.durationMs(),
                toIsoString(appResponse.startedAt()),
                toIsoString(appResponse.completedAt()));
    }

    /**
     * CrawlExecutionDetailResponse → CrawlExecutionDetailApiResponse 변환
     *
     * @param appResponse Application 상세 응답 DTO
     * @return API 상세 응답 DTO
     */
    public CrawlExecutionDetailApiResponse toDetailApiResponse(
            CrawlExecutionDetailResponse appResponse) {
        return new CrawlExecutionDetailApiResponse(
                appResponse.crawlExecutionId(),
                appResponse.crawlTaskId(),
                appResponse.crawlSchedulerId(),
                appResponse.sellerId(),
                appResponse.status() != null ? appResponse.status().name() : null,
                appResponse.httpStatusCode(),
                appResponse.responseBody(),
                appResponse.errorMessage(),
                appResponse.durationMs(),
                toIsoString(appResponse.startedAt()),
                toIsoString(appResponse.completedAt()));
    }

    /**
     * Application PageResponse → API PageApiResponse 변환
     *
     * @param pageResponse Application 페이지 응답
     * @return API 페이지 응답
     */
    public PageApiResponse<CrawlExecutionApiResponse> toPageApiResponse(
            PageResponse<CrawlExecutionResponse> pageResponse) {
        List<CrawlExecutionApiResponse> content =
                pageResponse.content().stream().map(this::toApiResponse).toList();

        return new PageApiResponse<>(
                content,
                pageResponse.page(),
                pageResponse.size(),
                pageResponse.totalElements(),
                pageResponse.totalPages(),
                pageResponse.first(),
                pageResponse.last());
    }

    /**
     * Instant → ISO-8601 String 변환
     *
     * @param instant Instant 시각
     * @return ISO-8601 형식 문자열 (null 안전)
     */
    private String toIsoString(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.toString();
    }
}
