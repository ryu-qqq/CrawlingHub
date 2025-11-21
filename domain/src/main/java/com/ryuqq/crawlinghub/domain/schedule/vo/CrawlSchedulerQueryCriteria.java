package com.ryuqq.crawlinghub.domain.schedule.vo;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * 크롤 스케줄러 조회 조건 Value Object.
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>sellerId: 셀러 ID (optional, null이면 전체 조회)
 *   <li>status: 활성/비활성 필터 (optional, null이면 전체)
 *   <li>page: 페이지 번호 (0부터 시작)
 *   <li>size: 페이지 크기
 * </ul>
 *
 * @param sellerId 셀러 ID (optional)
 * @param status 스케줄러 상태 필터 (optional)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 */
public record CrawlSchedulerQueryCriteria(
        SellerId sellerId, SchedulerStatus status, Integer page, Integer size) {

    /** 오프셋 계산 (페이징용) */
    public long offset() {
        return (long) page * size;
    }

    /** 셀러 필터 여부 */
    public boolean hasSellerFilter() {
        return sellerId != null;
    }

    /** 상태 필터 여부 */
    public boolean hasStatusFilter() {
        return status != null;
    }
}
