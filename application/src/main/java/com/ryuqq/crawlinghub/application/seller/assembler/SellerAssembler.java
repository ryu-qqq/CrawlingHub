package com.ryuqq.crawlinghub.application.seller.assembler;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.SellerStatistics;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SchedulerSummary;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailStatistics;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.TaskSummary;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Seller Assembler
 *
 * <p>Domain → Response DTO 변환 전용 컴포넌트
 *
 * <ul>
 *   <li>Domain → DTO: Domain을 Response DTO로 변환
 *   <li>Query → Criteria: Query DTO를 Domain 조회 조건으로 변환
 *   <li>비즈니스 로직 없음 (단순 변환만)
 * </ul>
 *
 * <p><strong>주의</strong>: Command → Domain 변환은 {@code SellerCommandFactory}에서 담당
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerAssembler {

    /**
     * Seller → SellerResponse
     *
     * @param seller Seller Aggregate
     * @return 전체 상세 응답
     */
    public SellerResponse toResponse(Seller seller) {
        return new SellerResponse(
                seller.getSellerIdValue(),
                seller.getMustItSellerNameValue(),
                seller.getSellerNameValue(),
                seller.isActive(),
                seller.getCreatedAt(),
                seller.getUpdatedAt());
    }

    /**
     * Seller + 통계 → SellerSummaryResponse
     *
     * @param seller Seller Aggregate
     * @param statistics 셀러 통계 정보
     * @return 목록용 요약 응답
     */
    public SellerSummaryResponse toSummaryResponse(Seller seller, SellerStatistics statistics) {
        return new SellerSummaryResponse(
                seller.getSellerIdValue(),
                seller.getMustItSellerNameValue(),
                seller.getSellerNameValue(),
                seller.isActive(),
                seller.getCreatedAt(),
                statistics.activeSchedulerCount(),
                statistics.totalSchedulerCount(),
                statistics.lastTaskStatus(),
                statistics.lastTaskExecutedAt(),
                statistics.totalProductCount());
    }

    /**
     * Seller 목록 + 통계 맵 → SellerSummaryResponse 목록
     *
     * @param sellers Seller Aggregate 목록
     * @param statisticsMap 셀러 ID별 통계 맵
     * @return 요약 응답 목록
     */
    public List<SellerSummaryResponse> toSummaryResponses(
            List<Seller> sellers, Map<SellerId, SellerStatistics> statisticsMap) {
        return sellers.stream()
                .map(
                        seller -> {
                            SellerStatistics stats =
                                    statisticsMap.getOrDefault(
                                            seller.getSellerId(), SellerStatistics.empty());
                            return toSummaryResponse(seller, stats);
                        })
                .toList();
    }

    /**
     * SearchSellersQuery → SellerQueryCriteria (조회 조건 변환)
     *
     * <p>Application Layer Query DTO를 Domain VO로 변환
     *
     * @param query 셀러 검색 Query
     * @return Domain 조회 조건 객체
     */
    public SellerQueryCriteria toCriteria(SearchSellersQuery query) {
        MustItSellerName mustItSellerName =
                query.mustItSellerName() != null
                        ? MustItSellerName.of(query.mustItSellerName())
                        : null;
        SellerName sellerName =
                query.sellerName() != null ? SellerName.of(query.sellerName()) : null;

        return new SellerQueryCriteria(
                mustItSellerName,
                sellerName,
                query.sellerStatus(),
                query.createdFrom(),
                query.createdTo(),
                query.page(),
                query.size());
    }

    /**
     * Seller 목록 + 통계 + 총 개수 → PageResponse<SellerSummaryResponse>
     *
     * <p>페이징 메타데이터를 계산하여 PageResponse로 변환
     *
     * @param sellers Seller 목록
     * @param statisticsMap 셀러 ID별 통계 맵
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return 페이징된 응답
     */
    public PageResponse<SellerSummaryResponse> toPageResponse(
            List<Seller> sellers,
            Map<SellerId, SellerStatistics> statisticsMap,
            int page,
            int size,
            long totalElements) {
        List<SellerSummaryResponse> content = toSummaryResponses(sellers, statisticsMap);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = (page == 0);
        boolean last = (page >= totalPages - 1);

        return PageResponse.of(content, page, size, totalElements, totalPages, first, last);
    }

    /**
     * Seller + 스케줄러 + 태스크 → SellerDetailResponse
     *
     * <p>셀러 상세 조회 시 연관 데이터를 포함한 응답 생성
     *
     * @param seller Seller Aggregate
     * @param schedulers 셀러의 스케줄러 목록
     * @param recentTasks 최근 태스크 목록
     * @return 셀러 상세 응답
     */
    public SellerDetailResponse toDetailResponse(
            Seller seller, List<CrawlScheduler> schedulers, List<CrawlTask> recentTasks) {
        List<SchedulerSummary> schedulerSummaries = toSchedulerSummaries(schedulers);
        List<TaskSummary> taskSummaries = toTaskSummaries(recentTasks);

        return new SellerDetailResponse(
                seller.getSellerIdValue(),
                seller.getMustItSellerNameValue(),
                seller.getSellerNameValue(),
                seller.isActive(),
                seller.getCreatedAt(),
                seller.getUpdatedAt(),
                schedulerSummaries,
                taskSummaries,
                SellerDetailStatistics.empty());
    }

    /**
     * CrawlScheduler 목록 → SchedulerSummary 목록
     *
     * @param schedulers CrawlScheduler 목록
     * @return SchedulerSummary 목록
     */
    private List<SchedulerSummary> toSchedulerSummaries(List<CrawlScheduler> schedulers) {
        return schedulers.stream().map(this::toSchedulerSummary).toList();
    }

    /**
     * CrawlScheduler → SchedulerSummary
     *
     * @param scheduler CrawlScheduler Aggregate
     * @return SchedulerSummary
     */
    private SchedulerSummary toSchedulerSummary(CrawlScheduler scheduler) {
        return new SchedulerSummary(
                scheduler.getCrawlSchedulerIdValue(),
                scheduler.getSchedulerNameValue(),
                scheduler.getStatus().name(),
                scheduler.getCronExpressionValue(),
                null);
    }

    /**
     * CrawlTask 목록 → TaskSummary 목록
     *
     * @param tasks CrawlTask 목록
     * @return TaskSummary 목록
     */
    private List<TaskSummary> toTaskSummaries(List<CrawlTask> tasks) {
        return tasks.stream().map(this::toTaskSummary).toList();
    }

    /**
     * CrawlTask → TaskSummary
     *
     * @param task CrawlTask Aggregate
     * @return TaskSummary
     */
    private TaskSummary toTaskSummary(CrawlTask task) {
        return new TaskSummary(
                task.getIdValue(),
                task.getStatus().name(),
                task.getTaskType().name(),
                task.getCreatedAt());
    }
}
