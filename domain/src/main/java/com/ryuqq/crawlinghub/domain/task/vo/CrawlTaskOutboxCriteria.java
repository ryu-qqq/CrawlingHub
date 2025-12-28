package com.ryuqq.crawlinghub.domain.task.vo;

import java.util.List;

/**
 * CrawlTaskOutbox 조회 조건 Value Object
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>status: 단일 상태 필터 (optional)
 *   <li>statuses: 다중 상태 필터 (optional) - OR 조건
 *   <li>offset: 페이징 오프셋 (skip count)
 *   <li>limit: 조회 개수 제한
 * </ul>
 *
 * <p><strong>사용 예시</strong>:
 *
 * <pre>
 * // 단일 상태 조회
 * CrawlTaskOutboxCriteria.byStatus(OutboxStatus.PENDING, 100);
 *
 * // 다중 상태 조회 (PENDING OR FAILED)
 * CrawlTaskOutboxCriteria.byStatuses(List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), 100);
 *
 * // 페이징 조회 (page=1, size=20 → offset=20, limit=20)
 * CrawlTaskOutboxCriteria.byStatusesWithPaging(List.of(OutboxStatus.PENDING), 20, 20);
 * </pre>
 *
 * @param status 단일 상태 필터 (optional)
 * @param statuses 다중 상태 필터 (optional)
 * @param offset 페이징 오프셋 (0부터 시작)
 * @param limit 조회 개수 제한
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskOutboxCriteria(
        OutboxStatus status, List<OutboxStatus> statuses, int offset, int limit) {

    private static final int DEFAULT_LIMIT = 100;
    private static final int DEFAULT_OFFSET = 0;

    public CrawlTaskOutboxCriteria {
        if (offset < 0) {
            offset = DEFAULT_OFFSET;
        }
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        // 방어적 복사 - SpotBugs EI2 경고 수정
        if (statuses != null) {
            statuses = List.copyOf(statuses);
        }
    }

    // ===== Static Factory Methods =====

    /**
     * 단일 상태로 조회 조건 생성
     *
     * @param status 아웃박스 상태
     * @param limit 조회 개수 제한
     * @return CrawlTaskOutboxCriteria
     */
    public static CrawlTaskOutboxCriteria byStatus(OutboxStatus status, int limit) {
        return new CrawlTaskOutboxCriteria(status, null, 0, limit);
    }

    /**
     * 다중 상태로 조회 조건 생성 (OR 조건)
     *
     * @param statuses 상태 목록
     * @param limit 조회 개수 제한
     * @return CrawlTaskOutboxCriteria
     */
    public static CrawlTaskOutboxCriteria byStatuses(List<OutboxStatus> statuses, int limit) {
        return new CrawlTaskOutboxCriteria(null, statuses, 0, limit);
    }

    /**
     * 다중 상태로 페이징 조회 조건 생성
     *
     * @param statuses 상태 목록
     * @param offset 페이징 오프셋
     * @param limit 조회 개수 제한
     * @return CrawlTaskOutboxCriteria
     */
    public static CrawlTaskOutboxCriteria byStatusesWithPaging(
            List<OutboxStatus> statuses, int offset, int limit) {
        return new CrawlTaskOutboxCriteria(null, statuses, offset, limit);
    }

    /**
     * PENDING 또는 FAILED 상태 조회 조건 생성
     *
     * <p>재시도 스케줄러에서 사용
     *
     * @param limit 조회 개수 제한
     * @return CrawlTaskOutboxCriteria
     */
    public static CrawlTaskOutboxCriteria pendingOrFailed(int limit) {
        return new CrawlTaskOutboxCriteria(
                null, List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), 0, limit);
    }

    // ===== Helper Methods =====

    /**
     * 단일 상태 필터 여부
     *
     * @return 단일 상태 필터가 있으면 true
     */
    public boolean hasSingleStatusFilter() {
        return status != null;
    }

    /**
     * 다중 상태 필터 여부
     *
     * @return 다중 상태 필터가 있으면 true
     */
    public boolean hasMultipleStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    /**
     * 상태 필터 존재 여부
     *
     * @return 상태 필터가 있으면 true
     */
    public boolean hasStatusFilter() {
        return hasSingleStatusFilter() || hasMultipleStatusFilter();
    }
}
