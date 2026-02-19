package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query.SearchSellersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailStatisticsApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.TaskSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SchedulerSummary;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailStatistics;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.TaskSummary;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * SellerQueryApiMapper - Seller Query REST API ↔ Application Layer 변환
 *
 * <p>Seller Query 요청/응답에 대한 DTO 변환을 담당합니다.
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
 *   <li>Query: GetSeller, ListSellers 요청 변환
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 *   <li>Enum 변환 (String ↔ SellerStatus)
 *   <li>페이징 응답 변환 (PageResponse → PageApiResponse)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerQueryApiMapper {

    /**
     * ID → GetSellerQuery 변환
     *
     * <p>PathVariable의 ID를 GetSellerQuery로 변환합니다.
     *
     * @param sellerId 셀러 ID
     * @return Application Layer 셀러 조회 쿼리
     */
    public GetSellerQuery toQuery(Long sellerId) {
        return new GetSellerQuery(sellerId);
    }

    /**
     * ListSellersApiRequest → ListSellersQuery 변환
     *
     * @param request REST API 셀러 목록 조회 요청
     * @return Application Layer 셀러 목록 조회 쿼리
     */
    public SearchSellersQuery toQuery(SearchSellersApiRequest request) {
        List<SellerStatus> statuses = parseStatuses(request.statuses());

        String sellerName = isNotBlank(request.sellerName()) ? request.sellerName() : null;
        String mustItSellerName =
                isNotBlank(request.mustItSellerName()) ? request.mustItSellerName() : null;

        return new SearchSellersQuery(
                mustItSellerName,
                sellerName,
                statuses,
                request.createdFrom(),
                request.createdTo(),
                request.page(),
                request.size());
    }

    /**
     * 상태 문자열 목록 → SellerStatus Enum 목록 변환
     *
     * @param statusStrings 상태 문자열 목록
     * @return SellerStatus Enum 목록 (null이거나 빈 리스트면 null)
     * @throws IllegalArgumentException 유효하지 않은 상태값이 포함된 경우
     */
    private List<SellerStatus> parseStatuses(List<String> statusStrings) {
        if (statusStrings == null || statusStrings.isEmpty()) {
            return null;
        }
        return statusStrings.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(this::parseStatus)
                .toList();
    }

    private SellerStatus parseStatus(String status) {
        try {
            return SellerStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * SellerResponse → SellerApiResponse 변환
     *
     * @param appResponse Application Layer 셀러 응답
     * @return REST API 셀러 응답
     */
    public SellerApiResponse toApiResponse(SellerResponse appResponse) {
        String statusName = appResponse.active() ? "ACTIVE" : "INACTIVE";
        return new SellerApiResponse(
                appResponse.sellerId(),
                appResponse.mustItSellerName(),
                appResponse.sellerName(),
                statusName,
                toIsoString(appResponse.createdAt()),
                toIsoString(appResponse.updatedAt()));
    }

    private String toIsoString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    /**
     * SellerSummaryResponse → SellerSummaryApiResponse 변환
     *
     * @param appResponse Application Layer 셀러 요약 응답
     * @return REST API 셀러 요약 응답
     */
    public SellerSummaryApiResponse toSummaryApiResponse(SellerSummaryResponse appResponse) {
        String statusName = appResponse.active() ? "ACTIVE" : "INACTIVE";
        return new SellerSummaryApiResponse(
                appResponse.sellerId(),
                appResponse.mustItSellerName(),
                appResponse.sellerName(),
                statusName,
                format(appResponse.createdAt()),
                format(appResponse.updatedAt()));
    }

    /**
     * PageResponse<SellerSummaryResponse> → PageApiResponse<SellerSummaryApiResponse> 변환
     *
     * <p>Application Layer의 페이지 응답을 REST API Layer의 페이지 응답으로 변환합니다.
     *
     * @param appPageResponse Application Layer 페이지 응답
     * @return REST API 페이지 응답
     */
    public PageApiResponse<SellerSummaryApiResponse> toPageApiResponse(
            PageResponse<SellerSummaryResponse> appPageResponse) {
        List<SellerSummaryApiResponse> content =
                appPageResponse.content().stream().map(this::toSummaryApiResponse).toList();
        return PageApiResponse.of(
                content,
                appPageResponse.page(),
                appPageResponse.size(),
                appPageResponse.totalElements());
    }

    /**
     * SellerDetailResponse → SellerDetailApiResponse 변환
     *
     * @param appResponse Application Layer 셀러 상세 응답
     * @return REST API 셀러 상세 응답
     */
    public SellerDetailApiResponse toDetailApiResponse(SellerDetailResponse appResponse) {
        String statusName = appResponse.active() ? "ACTIVE" : "INACTIVE";
        List<SchedulerSummaryApiResponse> schedulers =
                toSchedulerSummaryApiResponses(appResponse.schedulers());
        List<TaskSummaryApiResponse> recentTasks =
                toTaskSummaryApiResponses(appResponse.recentTasks());
        SellerDetailStatisticsApiResponse statistics =
                toStatisticsApiResponse(appResponse.statistics());

        return new SellerDetailApiResponse(
                appResponse.sellerId(),
                appResponse.mustItSellerName(),
                appResponse.sellerName(),
                statusName,
                toIsoString(appResponse.createdAt()),
                toIsoString(appResponse.updatedAt()),
                schedulers,
                recentTasks,
                statistics);
    }

    private List<SchedulerSummaryApiResponse> toSchedulerSummaryApiResponses(
            List<SchedulerSummary> schedulers) {
        return schedulers.stream().map(this::toSchedulerSummaryApiResponse).toList();
    }

    private SchedulerSummaryApiResponse toSchedulerSummaryApiResponse(SchedulerSummary summary) {
        return new SchedulerSummaryApiResponse(
                summary.schedulerId(),
                summary.schedulerName(),
                summary.status(),
                summary.cronExpression(),
                toIsoString(summary.nextExecutionTime()));
    }

    private List<TaskSummaryApiResponse> toTaskSummaryApiResponses(List<TaskSummary> tasks) {
        return tasks.stream().map(this::toTaskSummaryApiResponse).toList();
    }

    private TaskSummaryApiResponse toTaskSummaryApiResponse(TaskSummary summary) {
        return new TaskSummaryApiResponse(
                summary.taskId(),
                summary.status(),
                summary.taskType(),
                toIsoString(summary.createdAt()));
    }

    private SellerDetailStatisticsApiResponse toStatisticsApiResponse(
            SellerDetailStatistics statistics) {
        return new SellerDetailStatisticsApiResponse(
                statistics.totalProducts(),
                statistics.syncedProducts(),
                statistics.pendingSyncProducts(),
                statistics.successRate());
    }
}
