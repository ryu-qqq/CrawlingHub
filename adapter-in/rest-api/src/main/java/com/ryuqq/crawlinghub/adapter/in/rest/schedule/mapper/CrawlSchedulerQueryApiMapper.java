package com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query.SearchCrawlSchedulersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.ExecutionInfoApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.SchedulerStatisticsApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.TaskSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.query.CommonSearchParams;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.ExecutionInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SchedulerStatistics;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SellerSummary;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.TaskSummary;
import com.ryuqq.crawlinghub.application.schedule.dto.query.CrawlSchedulerSearchParams;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerPageResult;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResult;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerQueryApiMapper - CrawlScheduler Query REST API ↔ Application Layer 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerQueryApiMapper {

    /**
     * SearchCrawlSchedulersApiRequest → CrawlSchedulerSearchParams 변환
     *
     * @param request REST API 요청
     * @return Application Layer 검색 파라미터
     */
    public CrawlSchedulerSearchParams toSearchParams(SearchCrawlSchedulersApiRequest request) {
        CommonSearchParams searchParams =
                CommonSearchParams.of(
                        null,
                        null,
                        null,
                        request.sortKey(),
                        request.sortDirection(),
                        request.page(),
                        request.size());

        return CrawlSchedulerSearchParams.of(
                request.sellerId(),
                request.statuses(),
                request.searchField(),
                request.searchWord(),
                searchParams);
    }

    /**
     * CrawlSchedulerPageResult → PageApiResponse 변환
     *
     * @param pageResult Application Layer 페이지 결과
     * @return REST API 페이지 응답
     */
    public PageApiResponse<CrawlSchedulerSummaryApiResponse> toPageResponse(
            CrawlSchedulerPageResult pageResult) {
        List<CrawlSchedulerSummaryApiResponse> content =
                pageResult.results().stream().map(this::toSummaryApiResponse).toList();
        return PageApiResponse.of(
                content,
                pageResult.pageMeta().page(),
                pageResult.pageMeta().size(),
                pageResult.pageMeta().totalElements());
    }

    /**
     * CrawlSchedulerResult → CrawlSchedulerSummaryApiResponse 변환
     *
     * @param result Application Layer 스케줄러 결과
     * @return REST API 스케줄러 요약 응답
     */
    public CrawlSchedulerSummaryApiResponse toSummaryApiResponse(CrawlSchedulerResult result) {
        return new CrawlSchedulerSummaryApiResponse(
                result.id(),
                result.sellerId(),
                result.schedulerName(),
                result.cronExpression(),
                result.status(),
                format(result.createdAt()),
                format(result.updatedAt()));
    }

    /**
     * CrawlSchedulerDetailResult → CrawlSchedulerDetailApiResponse 변환
     *
     * @param detailResult Application Layer 스케줄러 상세 결과
     * @return REST API 스케줄러 상세 응답
     */
    public CrawlSchedulerDetailApiResponse toDetailApiResponse(
            CrawlSchedulerDetailResult detailResult) {
        return new CrawlSchedulerDetailApiResponse(
                detailResult.scheduler().id(),
                detailResult.scheduler().schedulerName(),
                detailResult.scheduler().cronExpression(),
                detailResult.scheduler().status(),
                toIsoString(detailResult.scheduler().createdAt()),
                toIsoString(detailResult.scheduler().updatedAt()),
                toSellerSummaryApiResponse(detailResult.seller()),
                toExecutionInfoApiResponse(detailResult.execution()),
                toStatisticsApiResponse(detailResult.statistics()),
                toTaskSummaryApiResponses(detailResult.recentTasks()));
    }

    private SellerSummaryApiResponse toSellerSummaryApiResponse(SellerSummary seller) {
        if (seller == null) {
            return null;
        }
        return new SellerSummaryApiResponse(
                seller.sellerId(), seller.sellerName(), seller.mustItSellerName());
    }

    private ExecutionInfoApiResponse toExecutionInfoApiResponse(ExecutionInfo execution) {
        if (execution == null) {
            return null;
        }
        return new ExecutionInfoApiResponse(
                null, toIsoString(execution.lastExecutionTime()), execution.lastExecutionStatus());
    }

    private SchedulerStatisticsApiResponse toStatisticsApiResponse(SchedulerStatistics statistics) {
        if (statistics == null) {
            return null;
        }
        return new SchedulerStatisticsApiResponse(
                statistics.totalTasks(),
                statistics.successTasks(),
                statistics.failedTasks(),
                statistics.successRate(),
                0L);
    }

    private List<TaskSummaryApiResponse> toTaskSummaryApiResponses(List<TaskSummary> tasks) {
        if (tasks == null) {
            return List.of();
        }
        return tasks.stream().map(this::toTaskSummaryApiResponse).toList();
    }

    private TaskSummaryApiResponse toTaskSummaryApiResponse(TaskSummary task) {
        return new TaskSummaryApiResponse(
                task.taskId(),
                task.status(),
                task.taskType(),
                toIsoString(task.createdAt()),
                toIsoString(task.completedAt()));
    }

    private String toIsoString(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.toString();
    }
}
