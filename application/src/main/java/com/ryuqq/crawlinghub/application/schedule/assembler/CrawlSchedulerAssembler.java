package com.ryuqq.crawlinghub.application.schedule.assembler;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.ExecutionInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerStatistics;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SellerSummaryForScheduler;
import com.ryuqq.crawlinghub.application.schedule.dto.response.TaskSummaryForScheduler;
import com.ryuqq.crawlinghub.domain.common.vo.DateRange;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerPageCriteria;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
     * SearchCrawlSchedulersQuery → CrawlSchedulerPageCriteria (조회 조건 변환)
     *
     * <p>Application Layer Query DTO를 Domain VO로 변환
     *
     * @param query 스케줄러 검색 Query
     * @return Domain 조회 조건 객체
     */
    public CrawlSchedulerPageCriteria toCriteria(SearchCrawlSchedulersQuery query) {
        SellerId sellerId = query.sellerId() != null ? SellerId.of(query.sellerId()) : null;
        DateRange dateRange = toDateRange(query.createdFrom(), query.createdTo());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());

        return CrawlSchedulerPageCriteria.of(sellerId, query.statuses(), dateRange, pageRequest);
    }

    private DateRange toDateRange(Instant from, Instant to) {
        if (from == null && to == null) {
            return null;
        }
        java.time.LocalDate startDate =
                from != null
                        ? java.time.LocalDate.ofInstant(from, java.time.ZoneId.systemDefault())
                        : null;
        java.time.LocalDate endDate =
                to != null
                        ? java.time.LocalDate.ofInstant(to, java.time.ZoneId.systemDefault())
                        : null;
        return DateRange.of(startDate, endDate);
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

    /**
     * CrawlScheduler + 관련 데이터 → CrawlSchedulerDetailResponse 변환
     *
     * <p>스케줄러 상세 조회에 필요한 모든 정보를 조립합니다.
     *
     * @param scheduler 크롤 스케줄러 Aggregate
     * @param seller 연관 셀러 (nullable)
     * @param recentTasks 최근 태스크 목록
     * @param statusCounts 상태별 태스크 개수
     * @return 스케줄러 상세 응답 DTO
     */
    public CrawlSchedulerDetailResponse toDetailResponse(
            CrawlScheduler scheduler,
            Seller seller,
            List<CrawlTask> recentTasks,
            Map<CrawlTaskStatus, Long> statusCounts) {

        SellerSummaryForScheduler sellerSummary = toSellerSummary(seller);
        ExecutionInfo executionInfo = toExecutionInfo(recentTasks);
        SchedulerStatistics statistics = toStatistics(statusCounts);
        List<TaskSummaryForScheduler> taskSummaries = toTaskSummaries(recentTasks);

        return new CrawlSchedulerDetailResponse(
                scheduler.getCrawlSchedulerIdValue(),
                scheduler.getSchedulerNameValue(),
                scheduler.getCronExpressionValue(),
                scheduler.getStatus(),
                scheduler.getCreatedAt(),
                scheduler.getUpdatedAt(),
                sellerSummary,
                executionInfo,
                statistics,
                taskSummaries);
    }

    private SellerSummaryForScheduler toSellerSummary(Seller seller) {
        if (seller == null) {
            return null;
        }
        return new SellerSummaryForScheduler(
                seller.getSellerIdValue(),
                seller.getSellerNameValue(),
                seller.getMustItSellerNameValue());
    }

    private ExecutionInfo toExecutionInfo(List<CrawlTask> recentTasks) {
        if (recentTasks.isEmpty()) {
            return new ExecutionInfo(null, null, null);
        }

        CrawlTask latestTask = recentTasks.get(0);
        Instant lastExecutionTime = latestTask.getCreatedAt();
        String lastExecutionStatus = latestTask.getStatus().name();

        return new ExecutionInfo(null, lastExecutionTime, lastExecutionStatus);
    }

    private SchedulerStatistics toStatistics(Map<CrawlTaskStatus, Long> statusCounts) {
        long totalTasks = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        long successTasks = statusCounts.getOrDefault(CrawlTaskStatus.SUCCESS, 0L);
        long failedTasks =
                statusCounts.getOrDefault(CrawlTaskStatus.FAILED, 0L)
                        + statusCounts.getOrDefault(CrawlTaskStatus.TIMEOUT, 0L);

        double successRate = totalTasks > 0 ? (double) successTasks / totalTasks : 0.0;

        return new SchedulerStatistics(totalTasks, successTasks, failedTasks, successRate, 0L);
    }

    private List<TaskSummaryForScheduler> toTaskSummaries(List<CrawlTask> tasks) {
        return tasks.stream().map(this::toTaskSummary).toList();
    }

    private TaskSummaryForScheduler toTaskSummary(CrawlTask task) {
        Instant completedAt = isCompletedStatus(task.getStatus()) ? task.getUpdatedAt() : null;

        return new TaskSummaryForScheduler(
                task.getIdValue(),
                task.getStatus().name(),
                task.getTaskType().name(),
                task.getCreatedAt(),
                completedAt);
    }

    private boolean isCompletedStatus(CrawlTaskStatus status) {
        return status == CrawlTaskStatus.SUCCESS
                || status == CrawlTaskStatus.FAILED
                || status == CrawlTaskStatus.TIMEOUT;
    }
}
