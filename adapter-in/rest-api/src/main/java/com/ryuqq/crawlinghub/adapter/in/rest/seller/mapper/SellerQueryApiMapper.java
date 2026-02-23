package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query.SearchSellersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailStatisticsApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.TaskSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.query.CommonSearchParams;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerSearchParams;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerPageResult;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResult;
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
 *   <li>API Query Request → Application SearchParams (Controller → Application)
 *   <li>Application Result → API Response (Application → Controller)
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 *   <li>페이징 응답 변환 (SellerPageResult → PageApiResponse)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerQueryApiMapper {

    /**
     * SearchSellersApiRequest → SellerSearchParams 변환
     *
     * @param request REST API 셀러 목록 조회 요청
     * @return Application Layer 셀러 검색 파라미터
     */
    public SellerSearchParams toSearchParams(SearchSellersApiRequest request) {
        CommonSearchParams searchParams =
                CommonSearchParams.of(
                        null,
                        null,
                        null,
                        request.sortKey(),
                        request.sortDirection(),
                        request.page(),
                        request.size());

        return SellerSearchParams.of(
                isNotBlank(request.mustItSellerName()) ? request.mustItSellerName() : null,
                isNotBlank(request.sellerName()) ? request.sellerName() : null,
                request.statuses(),
                request.createdFrom(),
                request.createdTo(),
                searchParams);
    }

    /**
     * SellerResult → SellerSummaryApiResponse 변환
     *
     * @param result Application Layer 셀러 결과
     * @return REST API 셀러 요약 응답
     */
    public SellerSummaryApiResponse toSummaryApiResponse(SellerResult result) {
        return new SellerSummaryApiResponse(
                result.id(),
                result.mustItSellerName(),
                result.sellerName(),
                result.status(),
                format(result.createdAt()),
                format(result.updatedAt()));
    }

    /**
     * SellerPageResult → PageApiResponse<SellerSummaryApiResponse> 변환
     *
     * @param pageResult Application Layer 셀러 페이지 결과
     * @return REST API 페이지 응답
     */
    public PageApiResponse<SellerSummaryApiResponse> toPageResponse(SellerPageResult pageResult) {
        List<SellerSummaryApiResponse> content =
                pageResult.results().stream().map(this::toSummaryApiResponse).toList();
        return PageApiResponse.of(
                content,
                pageResult.pageMeta().page(),
                pageResult.pageMeta().size(),
                pageResult.pageMeta().totalElements());
    }

    /**
     * SellerDetailResult → SellerDetailApiResponse 변환
     *
     * @param result Application Layer 셀러 상세 Composite 결과
     * @return REST API 셀러 상세 응답
     */
    public SellerDetailApiResponse toDetailApiResponse(SellerDetailResult result) {
        SellerDetailResult.SellerInfo seller = result.seller();
        List<SchedulerSummaryApiResponse> schedulers =
                toSchedulerSummaryApiResponses(result.schedulers());
        List<TaskSummaryApiResponse> recentTasks = toTaskSummaryApiResponses(result.recentTasks());
        SellerDetailStatisticsApiResponse statistics = toStatisticsApiResponse(result.statistics());

        return new SellerDetailApiResponse(
                seller.sellerId(),
                seller.mustItSellerName(),
                seller.sellerName(),
                seller.status(),
                toIsoString(seller.createdAt()),
                toIsoString(seller.updatedAt()),
                schedulers,
                recentTasks,
                statistics);
    }

    private List<SchedulerSummaryApiResponse> toSchedulerSummaryApiResponses(
            List<SellerDetailResult.SchedulerSummary> schedulers) {
        return schedulers.stream().map(this::toSchedulerSummaryApiResponse).toList();
    }

    private SchedulerSummaryApiResponse toSchedulerSummaryApiResponse(
            SellerDetailResult.SchedulerSummary summary) {
        return new SchedulerSummaryApiResponse(
                summary.schedulerId(),
                summary.schedulerName(),
                summary.status(),
                summary.cronExpression(),
                null);
    }

    private List<TaskSummaryApiResponse> toTaskSummaryApiResponses(
            List<SellerDetailResult.TaskSummary> tasks) {
        return tasks.stream().map(this::toTaskSummaryApiResponse).toList();
    }

    private TaskSummaryApiResponse toTaskSummaryApiResponse(
            SellerDetailResult.TaskSummary summary) {
        return new TaskSummaryApiResponse(
                summary.taskId(),
                summary.status(),
                summary.taskType(),
                toIsoString(summary.createdAt()));
    }

    private SellerDetailStatisticsApiResponse toStatisticsApiResponse(
            SellerDetailResult.SellerStatistics statistics) {
        return new SellerDetailStatisticsApiResponse(
                statistics.totalProducts(),
                statistics.syncedProducts(),
                statistics.pendingSyncProducts(),
                statistics.successRate());
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String toIsoString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
