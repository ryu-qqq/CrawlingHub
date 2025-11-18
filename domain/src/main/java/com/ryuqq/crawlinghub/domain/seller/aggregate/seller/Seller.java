package com.ryuqq.crawlinghub.domain.seller.aggregate.seller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

import com.ryuqq.crawlinghub.domain.seller.event.SellerDeactivatedEvent;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerHasActiveSchedulersException;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

/**
 * Seller Aggregate Root.
 */
public final class Seller {

    private static Clock clock = Clock.systemUTC();

    private final SellerId sellerId;
    private final MustItSellerId mustItSellerId;
    private final String sellerName;
    private SellerStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Seller(
        SellerId sellerId,
        MustItSellerId mustItSellerId,
        String sellerName,
        SellerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.mustItSellerId = Objects.requireNonNull(mustItSellerId, "mustItSellerId must not be null");
        this.sellerName = requireSellerName(sellerName);
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * 신규 셀러 생성 전용 정적 팩토리.
     */
    public static Seller forNew(MustItSellerId mustItSellerId, String sellerName) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new Seller(
            SellerId.forNew(),
            mustItSellerId,
            sellerName,
            SellerStatus.ACTIVE,
            now,
            now
        );
    }

    /**
     * 영속화되지 않은 기존 셀러 생성 (ID 보유, 타임스탬프 없음).
     */
    public static Seller of(
        SellerId sellerId,
        MustItSellerId mustItSellerId,
        String sellerName,
        SellerStatus status
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new Seller(sellerId, mustItSellerId, sellerName, status, now, now);
    }

    /**
     * 저장소에서 읽어온 셀러 재구성 전용 팩토리.
     */
    public static Seller reconstitute(
        SellerId sellerId,
        MustItSellerId mustItSellerId,
        String sellerName,
        SellerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Seller(sellerId, mustItSellerId, sellerName, status, createdAt, updatedAt);
    }

    static void changeClock(Clock newClock) {
        clock = Objects.requireNonNull(newClock, "clock must not be null");
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public MustItSellerId getMustItSellerId() {
        return mustItSellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public SellerStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static String requireSellerName(String sellerName) {
        if (sellerName == null || sellerName.isBlank()) {
            throw new IllegalArgumentException("sellerName must not be blank");
        }
        return sellerName;
    }

    public SellerDeactivatedEvent deactivate(int activeSchedulerCount) {
        if (activeSchedulerCount > 0) {
            long sellerIdValue = sellerId.value() == null ? -1L : sellerId.value();
            throw new SellerHasActiveSchedulersException(sellerIdValue, activeSchedulerCount);
        }

        if (status == SellerStatus.INACTIVE) {
            return null;
        }

        this.status = SellerStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
        return new SellerDeactivatedEvent(sellerId, updatedAt);
    }

    public void activate() {
        if (status == SellerStatus.ACTIVE) {
            return;
        }

        this.status = SellerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
    }
}

