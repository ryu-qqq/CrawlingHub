package com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query.SearchCrawlSchedulersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.ExecutionInfoApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.SchedulerStatisticsApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.TaskSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.ExecutionInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerStatistics;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SellerSummaryForScheduler;
import com.ryuqq.crawlinghub.application.schedule.dto.response.TaskSummaryForScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
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
        List<SchedulerStatus> statuses = parseStatuses(request.statuses());

        return new SearchCrawlSchedulersQuery(
                request.sellerId(),
                statuses,
                request.createdFrom(),
                request.createdTo(),
                request.page(),
                request.size());
    }

    /**
     * 상태 문자열 목록 → SchedulerStatus Enum 목록 변환
     *
     * @param statusStrings 상태 문자열 목록
     * @return SchedulerStatus Enum 목록 (null이거나 빈 리스트면 null)
     */
    private List<SchedulerStatus> parseStatuses(List<String> statusStrings) {
        if (statusStrings == null || statusStrings.isEmpty()) {
            return null;
        }
        return statusStrings.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(
                        s -> {
                            try {
                                return SchedulerStatus.valueOf(s);
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        })
                .filter(Objects::nonNull)
                .toList();
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
                toIsoString(appResponse.createdAt()),
                toIsoString(appResponse.updatedAt()));
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

    /**
     * CrawlSchedulerDetailResponse → CrawlSchedulerDetailApiResponse 변환
     *
     * @param appDetailResponse Application Layer 스케줄러 상세 응답
     * @return REST API 스케줄러 상세 응답
     */
    public CrawlSchedulerDetailApiResponse toDetailApiResponse(
            CrawlSchedulerDetailResponse appDetailResponse) {
        return new CrawlSchedulerDetailApiResponse(
                appDetailResponse.crawlSchedulerId(),
                appDetailResponse.schedulerName(),
                appDetailResponse.cronExpression(),
                appDetailResponse.status().name(),
                toIsoString(appDetailResponse.createdAt()),
                toIsoString(appDetailResponse.updatedAt()),
                toSellerSummaryApiResponse(appDetailResponse.seller()),
                toExecutionInfoApiResponse(appDetailResponse.execution()),
                toStatisticsApiResponse(appDetailResponse.statistics()),
                toTaskSummaryApiResponses(appDetailResponse.recentTasks()));
    }

    private SellerSummaryApiResponse toSellerSummaryApiResponse(SellerSummaryForScheduler seller) {
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
                toIsoString(execution.nextExecutionTime()),
                toIsoString(execution.lastExecutionTime()),
                execution.lastExecutionStatus());
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
                statistics.avgDurationMs());
    }

    private List<TaskSummaryApiResponse> toTaskSummaryApiResponses(
            List<TaskSummaryForScheduler> tasks) {
        if (tasks == null) {
            return List.of();
        }
        return tasks.stream().map(this::toTaskSummaryApiResponse).toList();
    }

    private TaskSummaryApiResponse toTaskSummaryApiResponse(TaskSummaryForScheduler task) {
        return new TaskSummaryApiResponse(
                task.taskId(),
                task.status(),
                task.taskType(),
                toIsoString(task.createdAt()),
                toIsoString(task.completedAt()));
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
