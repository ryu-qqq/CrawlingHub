package com.ryuqq.crawlinghub.domain.product.vo;

import java.time.Instant;
import java.util.List;

/**
 * ProductImageOutbox 조회 조건 Value Object
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>status: 단일 상태 필터 (optional)
 *   <li>statuses: 다중 상태 필터 (optional) - OR 조건
 *   <li>createdFrom: 생성일 시작 범위 (optional, inclusive)
 *   <li>createdTo: 생성일 종료 범위 (optional, exclusive)
 *   <li>maxRetryCount: 최대 재시도 횟수 (optional)
 *   <li>offset: 페이징 오프셋 (skip count)
 *   <li>limit: 조회 개수 제한
 * </ul>
 *
 * <p><strong>사용 예시</strong>:
 *
 * <pre>
 * // 단일 상태 조회
 * ProductImageOutboxCriteria.byStatus(ProductOutboxStatus.PENDING, 100);
 *
 * // 다중 상태 조회 (PENDING OR FAILED)
 * ProductImageOutboxCriteria.pendingOrFailed(100);
 *
 * // 재시도 가능한 Outbox 조회 (PENDING OR FAILED + 재시도 횟수 제한)
 * ProductImageOutboxCriteria.retryable(3, 100);
 * </pre>
 *
 * @param status 단일 상태 필터 (optional)
 * @param statuses 다중 상태 필터 (optional)
 * @param createdFrom 생성일 시작 범위 (optional, inclusive)
 * @param createdTo 생성일 종료 범위 (optional, exclusive)
 * @param maxRetryCount 최대 재시도 횟수 (optional, null이면 무제한)
 * @param offset 페이징 오프셋 (0부터 시작)
 * @param limit 조회 개수 제한
 * @author development-team
 * @since 1.0.0
 */
public record ProductImageOutboxCriteria(
        ProductOutboxStatus status,
        List<ProductOutboxStatus> statuses,
        Instant createdFrom,
        Instant createdTo,
        Integer maxRetryCount,
        int offset,
        int limit) {

    private static final int DEFAULT_LIMIT = 100;
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_MAX_RETRY_COUNT = 3;

    public ProductImageOutboxCriteria {
        if (offset < 0) {
            offset = DEFAULT_OFFSET;
        }
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        if (statuses != null) {
            statuses = List.copyOf(statuses);
        }
    }

    // ===== Static Factory Methods =====

    /**
     * 단일 상태로 조회 조건 생성
     *
     * @param status Outbox 상태
     * @param limit 조회 개수 제한
     * @return ProductImageOutboxCriteria
     */
    public static ProductImageOutboxCriteria byStatus(ProductOutboxStatus status, int limit) {
        return new ProductImageOutboxCriteria(status, null, null, null, null, 0, limit);
    }

    /**
     * 다중 상태로 조회 조건 생성 (OR 조건)
     *
     * @param statuses 상태 목록
     * @param limit 조회 개수 제한
     * @return ProductImageOutboxCriteria
     */
    public static ProductImageOutboxCriteria byStatuses(
            List<ProductOutboxStatus> statuses, int limit) {
        return new ProductImageOutboxCriteria(null, statuses, null, null, null, 0, limit);
    }

    /**
     * PENDING 또는 FAILED 상태 조회 조건 생성
     *
     * <p>Outbox 스케줄러에서 사용
     *
     * @param limit 조회 개수 제한
     * @return ProductImageOutboxCriteria
     */
    public static ProductImageOutboxCriteria pendingOrFailed(int limit) {
        return new ProductImageOutboxCriteria(
                null,
                List.of(ProductOutboxStatus.PENDING, ProductOutboxStatus.FAILED),
                null,
                null,
                null,
                0,
                limit);
    }

    /**
     * 재시도 가능한 Outbox 조회 조건 생성
     *
     * <p>PENDING 또는 FAILED 상태이면서 최대 재시도 횟수 미만인 Outbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return ProductImageOutboxCriteria
     */
    public static ProductImageOutboxCriteria retryable(int maxRetryCount, int limit) {
        return new ProductImageOutboxCriteria(
                null,
                List.of(ProductOutboxStatus.PENDING, ProductOutboxStatus.FAILED),
                null,
                null,
                maxRetryCount,
                0,
                limit);
    }

    /**
     * 다중 상태 + 기간으로 페이징 조회 조건 생성
     *
     * @param statuses 상태 목록
     * @param createdFrom 생성일 시작 범위 (inclusive)
     * @param createdTo 생성일 종료 범위 (exclusive)
     * @param offset 페이징 오프셋
     * @param limit 조회 개수 제한
     * @return ProductImageOutboxCriteria
     */
    public static ProductImageOutboxCriteria withDateRange(
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            int offset,
            int limit) {
        return new ProductImageOutboxCriteria(
                null, statuses, createdFrom, createdTo, null, offset, limit);
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

    /**
     * 생성일 시작 범위 필터 여부
     *
     * @return 생성일 시작 필터가 있으면 true
     */
    public boolean hasCreatedFromFilter() {
        return createdFrom != null;
    }

    /**
     * 생성일 종료 범위 필터 여부
     *
     * @return 생성일 종료 필터가 있으면 true
     */
    public boolean hasCreatedToFilter() {
        return createdTo != null;
    }

    /**
     * 기간 필터 존재 여부
     *
     * @return 기간 필터가 있으면 true
     */
    public boolean hasDateRangeFilter() {
        return hasCreatedFromFilter() || hasCreatedToFilter();
    }

    /**
     * 재시도 횟수 필터 여부
     *
     * @return 재시도 횟수 필터가 있으면 true
     */
    public boolean hasMaxRetryCountFilter() {
        return maxRetryCount != null;
    }
}
