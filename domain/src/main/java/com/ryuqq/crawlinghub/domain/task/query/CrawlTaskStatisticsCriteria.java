package com.ryuqq.crawlinghub.domain.task.query;

import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;

/**
 * CrawlTask 통계 조회 조건 Value Object
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>crawlSchedulerId: 스케줄러 ID 필터 (optional)
 *   <li>sellerId: 셀러 ID 필터 (optional)
 *   <li>from: 시작 시각 (optional)
 *   <li>to: 종료 시각 (optional)
 * </ul>
 *
 * @param crawlSchedulerId 스케줄러 ID 필터 (optional)
 * @param sellerId 셀러 ID 필터 (optional)
 * @param from 시작 시각 (optional)
 * @param to 종료 시각 (optional)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskStatisticsCriteria(
        CrawlSchedulerId crawlSchedulerId, SellerId sellerId, Instant from, Instant to) {

    /**
     * 스케줄러 ID 필터 여부
     *
     * @return 스케줄러 ID 필터가 있으면 true
     */
    public boolean hasSchedulerFilter() {
        return crawlSchedulerId != null;
    }

    /**
     * 셀러 ID 필터 여부
     *
     * @return 셀러 ID 필터가 있으면 true
     */
    public boolean hasSellerFilter() {
        return sellerId != null;
    }

    /**
     * 기간 필터 여부
     *
     * @return 기간 필터가 있으면 true
     */
    public boolean hasPeriodFilter() {
        return from != null && to != null;
    }

    /**
     * 시작 시각 필터 여부
     *
     * @return 시작 시각 필터가 있으면 true
     */
    public boolean hasFromFilter() {
        return from != null;
    }

    /**
     * 종료 시각 필터 여부
     *
     * @return 종료 시각 필터가 있으면 true
     */
    public boolean hasToFilter() {
        return to != null;
    }
}
