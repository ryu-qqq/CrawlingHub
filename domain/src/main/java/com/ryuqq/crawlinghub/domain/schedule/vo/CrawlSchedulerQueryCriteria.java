package com.ryuqq.crawlinghub.domain.schedule.vo;

import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.util.List;

/**
 * 크롤 스케줄러 조회 조건 Value Object.
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>sellerId: 셀러 ID (optional, null이면 전체 조회)
 *   <li>statuses: 활성/비활성 필터 목록 (optional, null이거나 빈 목록이면 전체)
 *   <li>createdFrom: 생성일 시작 (optional, null이면 필터 없음)
 *   <li>createdTo: 생성일 종료 (optional, null이면 필터 없음)
 *   <li>page: 페이지 번호 (0부터 시작)
 *   <li>size: 페이지 크기
 * </ul>
 *
 * @param sellerId 셀러 ID (optional)
 * @param statuses 스케줄러 상태 필터 목록 (optional)
 * @param createdFrom 생성일 시작 (optional)
 * @param createdTo 생성일 종료 (optional)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 */
public record CrawlSchedulerQueryCriteria(
        SellerId sellerId,
        List<SchedulerStatus> statuses,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer size) {

    public CrawlSchedulerQueryCriteria {
        statuses = statuses != null ? List.copyOf(statuses) : null;
    }

    /** 오프셋 계산 (페이징용) */
    public long offset() {
        return (long) page * size;
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
        return createdFrom != null || createdTo != null;
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
