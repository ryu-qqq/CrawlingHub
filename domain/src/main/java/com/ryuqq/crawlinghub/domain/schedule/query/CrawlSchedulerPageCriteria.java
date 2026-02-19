package com.ryuqq.crawlinghub.domain.schedule.query;

import com.ryuqq.crawlinghub.domain.common.vo.DateRange;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;

/**
 * 크롤 스케줄러 페이지 기반 조회 조건
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>sellerId: 셀러 ID (optional, null이면 전체 조회)
 *   <li>statuses: 활성/비활성 필터 목록 (optional, null이거나 빈 목록이면 전체)
 *   <li>dateRange: 생성일 범위 (optional, null이면 필터 없음)
 *   <li>pageRequest: 페이징 정보
 * </ul>
 *
 * @param sellerId 셀러 ID (optional)
 * @param statuses 스케줄러 상태 필터 목록 (optional)
 * @param dateRange 생성일 범위 (optional)
 * @param pageRequest 페이징 정보
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerPageCriteria(
        SellerId sellerId,
        List<SchedulerStatus> statuses,
        DateRange dateRange,
        PageRequest pageRequest) {

    public CrawlSchedulerPageCriteria {
        statuses = statuses != null ? List.copyOf(statuses) : null;
    }

    /**
     * 팩토리 메서드
     *
     * @param sellerId 셀러 ID
     * @param statuses 상태 필터
     * @param dateRange 날짜 범위
     * @param pageRequest 페이징
     * @return CrawlSchedulerPageCriteria
     */
    public static CrawlSchedulerPageCriteria of(
            SellerId sellerId,
            List<SchedulerStatus> statuses,
            DateRange dateRange,
            PageRequest pageRequest) {
        return new CrawlSchedulerPageCriteria(sellerId, statuses, dateRange, pageRequest);
    }

    /** 오프셋 계산 (페이징용) */
    public long offset() {
        return pageRequest.offset();
    }

    /** 페이지 크기 */
    public int size() {
        return pageRequest.size();
    }

    /** 셀러 필터 여부 */
    public boolean hasSellerFilter() {
        return sellerId != null;
    }

    /** 상태 필터 여부 (다중 상태) */
    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    /** 기간 필터 여부 */
    public boolean hasDateFilter() {
        return dateRange != null && !dateRange.isEmpty();
    }

    /**
     * 단일 상태 반환 (하위 호환성)
     *
     * @return 첫 번째 상태 또는 null
     */
    public SchedulerStatus status() {
        return hasStatusFilter() ? statuses.get(0) : null;
    }
}
