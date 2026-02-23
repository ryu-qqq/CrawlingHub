package com.ryuqq.crawlinghub.application.task.factory.query;

import com.ryuqq.crawlinghub.application.task.dto.query.CrawlTaskSearchParams;
import com.ryuqq.crawlinghub.application.task.dto.query.GetTaskStatisticsQuery;
import com.ryuqq.crawlinghub.domain.execution.query.CrawlExecutionStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawlTask QueryFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>SearchParams → Criteria 변환
 * </ul>
 *
 * <p><strong>금지</strong>:
 *
 * <ul>
 *   <li>@Transactional 금지 (변환만, 트랜잭션 불필요)
 *   <li>Port 의존 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskQueryFactory {

    /**
     * CrawlTaskSearchParams → CrawlTaskCriteria 변환
     *
     * @param params 검색 파라미터
     * @return Domain 조회 조건 객체
     */
    public CrawlTaskCriteria createCriteria(CrawlTaskSearchParams params) {
        List<CrawlSchedulerId> schedulerIds = toSchedulerIds(params.crawlSchedulerIds());
        List<SellerId> sellerIds = toSellerIds(params.sellerIds());
        List<CrawlTaskStatus> statuses = parseStatuses(params.statuses());
        List<CrawlTaskType> taskTypes = parseTaskTypes(params.taskTypes());

        return new CrawlTaskCriteria(
                schedulerIds,
                sellerIds,
                statuses,
                taskTypes,
                params.createdFrom(),
                params.createdTo(),
                params.page(),
                params.size());
    }

    /**
     * GetTaskStatisticsQuery → CrawlTaskStatisticsCriteria 변환
     *
     * @param query 통계 조회 쿼리
     * @return Domain 통계 조회 조건 객체
     */
    public CrawlTaskStatisticsCriteria createStatisticsCriteria(GetTaskStatisticsQuery query) {
        CrawlSchedulerId schedulerId =
                query.schedulerId() != null ? CrawlSchedulerId.of(query.schedulerId()) : null;
        SellerId sellerId = query.sellerId() != null ? SellerId.of(query.sellerId()) : null;
        Instant from = toInstant(query.from());
        Instant to = toInstant(query.to());
        return new CrawlTaskStatisticsCriteria(schedulerId, sellerId, from, to);
    }

    /**
     * GetTaskStatisticsQuery → CrawlExecutionStatisticsCriteria 변환
     *
     * @param query 통계 조회 쿼리
     * @return Domain 실행 통계 조회 조건 객체
     */
    public CrawlExecutionStatisticsCriteria createExecutionStatisticsCriteria(
            GetTaskStatisticsQuery query) {
        CrawlSchedulerId schedulerId =
                query.schedulerId() != null ? CrawlSchedulerId.of(query.schedulerId()) : null;
        SellerId sellerId = query.sellerId() != null ? SellerId.of(query.sellerId()) : null;
        Instant from = toInstant(query.from());
        Instant to = toInstant(query.to());
        return new CrawlExecutionStatisticsCriteria(schedulerId, sellerId, from, to);
    }

    private List<CrawlSchedulerId> toSchedulerIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return ids.stream().map(CrawlSchedulerId::of).toList();
    }

    private List<SellerId> toSellerIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return ids.stream().map(SellerId::of).toList();
    }

    private List<CrawlTaskStatus> parseStatuses(List<String> statusStrings) {
        if (statusStrings == null || statusStrings.isEmpty()) {
            return null;
        }
        return statusStrings.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(CrawlTaskStatus::valueOf)
                .toList();
    }

    private List<CrawlTaskType> parseTaskTypes(List<String> taskTypeStrings) {
        if (taskTypeStrings == null || taskTypeStrings.isEmpty()) {
            return null;
        }
        return taskTypeStrings.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(CrawlTaskType::valueOf)
                .toList();
    }

    private Instant toInstant(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
