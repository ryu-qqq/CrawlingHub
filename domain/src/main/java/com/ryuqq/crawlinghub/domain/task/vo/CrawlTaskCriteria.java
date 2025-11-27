package com.ryuqq.crawlinghub.domain.task.vo;

import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;

/**
 * CrawlTask 조회 조건 Value Object
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>crawlSchedulerId: 스케줄러 ID (필수)
 *   <li>status: 상태 필터 (optional, null이면 전체)
 *   <li>page: 페이지 번호 (0부터 시작)
 *   <li>size: 페이지 크기
 * </ul>
 *
 * @param crawlSchedulerId 스케줄러 ID (필수)
 * @param status 상태 필터 (optional)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskCriteria(
        CrawlSchedulerId crawlSchedulerId, CrawlTaskStatus status, int page, int size) {
    public CrawlTaskCriteria {
        if (crawlSchedulerId == null) {
            throw new IllegalArgumentException("crawlSchedulerId는 null일 수 없습니다.");
        }
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
    }

    /**
     * offset 계산 (페이징용)
     *
     * @return 시작 위치
     */
    public long offset() {
        return (long) page * size;
    }

    /**
     * 상태 필터 여부
     *
     * @return 상태 필터가 있으면 true
     */
    public boolean hasStatusFilter() {
        return status != null;
    }
}
