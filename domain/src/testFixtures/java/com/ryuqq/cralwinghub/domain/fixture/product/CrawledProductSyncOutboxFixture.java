package com.ryuqq.cralwinghub.domain.fixture.product;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;

/**
 * CrawledProductSyncOutbox 테스트 Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawledProductSyncOutboxFixture {

    private CrawledProductSyncOutboxFixture() {}

    /**
     * 신규 등록용 PENDING 상태 Outbox 생성
     *
     * @return CrawledProductSyncOutbox
     */
    public static CrawledProductSyncOutbox aPendingForCreate() {
        return CrawledProductSyncOutbox.forCreate(
                CrawledProductId.of(1L), SellerId.of(100L), 12345L, Instant.now());
    }

    /**
     * 갱신용 PENDING 상태 Outbox 생성
     *
     * @return CrawledProductSyncOutbox
     */
    public static CrawledProductSyncOutbox aPendingForUpdate() {
        return CrawledProductSyncOutbox.forUpdate(
                CrawledProductId.of(1L), SellerId.of(100L), 12345L, 99999L, Instant.now());
    }

    /**
     * 복원된 PENDING 상태 Outbox 생성
     *
     * @return CrawledProductSyncOutbox
     */
    public static CrawledProductSyncOutbox aReconstitutedPending() {
        return CrawledProductSyncOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                SyncType.CREATE,
                "sync-key-123",
                null,
                ProductOutboxStatus.PENDING,
                0,
                null,
                Instant.now(),
                null);
    }

    /**
     * 복원된 PROCESSING 상태 Outbox 생성
     *
     * @return CrawledProductSyncOutbox
     */
    public static CrawledProductSyncOutbox aReconstitutedProcessing() {
        return CrawledProductSyncOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                SyncType.CREATE,
                "sync-key-123",
                null,
                ProductOutboxStatus.PROCESSING,
                0,
                null,
                Instant.now(),
                Instant.now());
    }

    /**
     * 복원된 COMPLETED 상태 Outbox 생성
     *
     * @return CrawledProductSyncOutbox
     */
    public static CrawledProductSyncOutbox aReconstitutedCompleted() {
        return CrawledProductSyncOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                SyncType.CREATE,
                "sync-key-123",
                99999L,
                ProductOutboxStatus.COMPLETED,
                0,
                null,
                Instant.now(),
                Instant.now());
    }

    /**
     * 복원된 FAILED 상태 Outbox 생성
     *
     * @return CrawledProductSyncOutbox
     */
    public static CrawledProductSyncOutbox aReconstitutedFailed() {
        return CrawledProductSyncOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                SyncType.CREATE,
                "sync-key-123",
                null,
                ProductOutboxStatus.FAILED,
                1,
                "Connection timeout",
                Instant.now(),
                Instant.now());
    }
}
