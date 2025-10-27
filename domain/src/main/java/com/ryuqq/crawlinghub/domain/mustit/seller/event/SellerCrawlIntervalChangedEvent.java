package com.ryuqq.crawlinghub.domain.mustit.seller.event;

import com.ryuqq.crawlinghub.domain.common.DomainEvent;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 셀러 크롤링 주기 변경 이벤트
 * <p>
 * 셀러의 크롤링 주기가 변경되었을 때 발행되는 Domain Event입니다.
 * 이 이벤트는 AWS EventBridge 스케줄 업데이트를 트리거하는 데 사용됩니다.
 * </p>
 * <p>
 * 리팩토링 이력:
 * <ul>
 *   <li>2025-01-27: Long sellerPk 추가 - EventHandler에서 DB 조회 없이 직접 사용</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public class SellerCrawlIntervalChangedEvent implements DomainEvent {

    private final String sellerId;
    private final Long sellerPk;
    private final CrawlInterval oldInterval;
    private final CrawlInterval newInterval;
    private final LocalDateTime occurredAt;

    /**
     * 셀러 크롤링 주기 변경 이벤트를 생성합니다.
     *
     * @param sellerId    셀러 비즈니스 ID (String)
     * @param sellerPk    셀러 PK (Long FK)
     * @param oldInterval 기존 크롤링 주기
     * @param newInterval 새로운 크롤링 주기
     * @throws NullPointerException 필수 파라미터가 null인 경우
     */
    public SellerCrawlIntervalChangedEvent(
            String sellerId,
            Long sellerPk,
            CrawlInterval oldInterval,
            CrawlInterval newInterval
    ) {
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.sellerPk = Objects.requireNonNull(sellerPk, "sellerPk must not be null");
        this.oldInterval = Objects.requireNonNull(oldInterval, "oldInterval must not be null");
        this.newInterval = Objects.requireNonNull(newInterval, "newInterval must not be null");
        this.occurredAt = LocalDateTime.now();
    }

    /**
     * 셀러 비즈니스 ID를 반환합니다.
     *
     * @return 셀러 비즈니스 ID (String)
     */
    public String getSellerId() {
        return sellerId;
    }

    /**
     * 셀러 PK를 반환합니다.
     * <p>
     * EventHandler에서 DB 조회 없이 직접 사용할 수 있습니다.
     * </p>
     *
     * @return 셀러 PK (Long FK)
     */
    public Long getSellerPk() {
        return sellerPk;
    }

    /**
     * 기존 크롤링 주기를 반환합니다.
     *
     * @return 기존 크롤링 주기
     */
    public CrawlInterval getOldInterval() {
        return oldInterval;
    }

    /**
     * 새로운 크롤링 주기를 반환합니다.
     *
     * @return 새로운 크롤링 주기
     */
    public CrawlInterval getNewInterval() {
        return newInterval;
    }

    /**
     * 이벤트 발생 시각을 반환합니다.
     *
     * @return 이벤트 발생 시각
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SellerCrawlIntervalChangedEvent that = (SellerCrawlIntervalChangedEvent) o;
        return Objects.equals(sellerId, that.sellerId)
                && Objects.equals(sellerPk, that.sellerPk)
                && Objects.equals(oldInterval, that.oldInterval)
                && Objects.equals(newInterval, that.newInterval)
                && Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sellerId, sellerPk, oldInterval, newInterval, occurredAt);
    }

    @Override
    public String toString() {
        return "SellerCrawlIntervalChangedEvent{"
                + "sellerId='" + sellerId + '\''
                + ", sellerPk=" + sellerPk
                + ", oldInterval=" + oldInterval
                + ", newInterval=" + newInterval
                + ", occurredAt=" + occurredAt
                + '}';
    }
}
