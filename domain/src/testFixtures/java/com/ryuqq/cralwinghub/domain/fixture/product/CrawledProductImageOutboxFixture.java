package com.ryuqq.cralwinghub.domain.fixture.product;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Clock;
import java.time.Instant;

/**
 * CrawledProductImageOutbox 테스트 Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawledProductImageOutboxFixture {

    private CrawledProductImageOutboxFixture() {}

    /**
     * 신규 PENDING 상태 Outbox 생성
     *
     * @return CrawledProductImageOutbox
     */
    public static CrawledProductImageOutbox aPendingOutbox() {
        return CrawledProductImageOutbox.forNew(
                CrawledProductId.of(1L),
                "https://example.com/image.jpg",
                ImageType.THUMBNAIL,
                Clock.systemDefaultZone());
    }

    /**
     * 복원된 PENDING 상태 Outbox 생성
     *
     * @return CrawledProductImageOutbox
     */
    public static CrawledProductImageOutbox aReconstitutedPending() {
        return CrawledProductImageOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                "https://example.com/image.jpg",
                ImageType.THUMBNAIL,
                "img-1-12345-abc123",
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
     * @return CrawledProductImageOutbox
     */
    public static CrawledProductImageOutbox aReconstitutedProcessing() {
        return CrawledProductImageOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                "https://example.com/image.jpg",
                ImageType.THUMBNAIL,
                "img-1-12345-abc123",
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
     * @return CrawledProductImageOutbox
     */
    public static CrawledProductImageOutbox aReconstitutedCompleted() {
        return CrawledProductImageOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                "https://example.com/image.jpg",
                ImageType.THUMBNAIL,
                "img-1-12345-abc123",
                "https://s3.amazonaws.com/bucket/uploaded-image.jpg",
                ProductOutboxStatus.COMPLETED,
                0,
                null,
                Instant.now(),
                Instant.now());
    }

    /**
     * 복원된 FAILED 상태 Outbox 생성
     *
     * @return CrawledProductImageOutbox
     */
    public static CrawledProductImageOutbox aReconstitutedFailed() {
        return CrawledProductImageOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                "https://example.com/image.jpg",
                ImageType.THUMBNAIL,
                "img-1-12345-abc123",
                null,
                ProductOutboxStatus.FAILED,
                1,
                "Upload timeout",
                Instant.now(),
                Instant.now());
    }

    /**
     * DESCRIPTION 타입의 PENDING 상태 Outbox 생성
     *
     * @return CrawledProductImageOutbox
     */
    public static CrawledProductImageOutbox aDescriptionPendingOutbox() {
        return CrawledProductImageOutbox.forNew(
                CrawledProductId.of(1L),
                "https://example.com/detail-image.jpg",
                ImageType.DESCRIPTION,
                Clock.systemDefaultZone());
    }
}
