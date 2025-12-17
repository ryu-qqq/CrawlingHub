package com.ryuqq.cralwinghub.domain.fixture.product;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Clock;
import java.time.Instant;

/**
 * ProductImageOutbox 테스트 Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public final class ProductImageOutboxFixture {

    private ProductImageOutboxFixture() {}

    /**
     * 복원된 PENDING 상태 Outbox 생성
     *
     * @return ProductImageOutbox
     */
    public static ProductImageOutbox aReconstitutedPending() {
        return ProductImageOutbox.reconstitute(
                1L,
                1L, // crawledProductImageId
                "img-1-12345-abc123",
                ProductOutboxStatus.PENDING,
                0,
                null,
                Instant.now(),
                null);
    }

    /**
     * 복원된 PROCESSING 상태 Outbox 생성
     *
     * @return ProductImageOutbox
     */
    public static ProductImageOutbox aReconstitutedProcessing() {
        return ProductImageOutbox.reconstitute(
                1L,
                1L,
                "img-1-12345-abc123",
                ProductOutboxStatus.PROCESSING,
                0,
                null,
                Instant.now(),
                Instant.now());
    }

    /**
     * 복원된 COMPLETED 상태 Outbox 생성
     *
     * @return ProductImageOutbox
     */
    public static ProductImageOutbox aReconstitutedCompleted() {
        return ProductImageOutbox.reconstitute(
                1L,
                1L,
                "img-1-12345-abc123",
                ProductOutboxStatus.COMPLETED,
                0,
                null,
                Instant.now(),
                Instant.now());
    }

    /**
     * 복원된 FAILED 상태 Outbox 생성
     *
     * @return ProductImageOutbox
     */
    public static ProductImageOutbox aReconstitutedFailed() {
        return ProductImageOutbox.reconstitute(
                1L,
                1L,
                "img-1-12345-abc123",
                ProductOutboxStatus.FAILED,
                1,
                "Upload timeout",
                Instant.now(),
                Instant.now());
    }

    /**
     * 신규 PENDING 상태 Outbox 생성 (이미지 ID로)
     *
     * @param crawledProductImageId 이미지 ID
     * @param originalUrl 원본 URL (멱등성 키 생성용)
     * @return ProductImageOutbox
     */
    public static ProductImageOutbox aNewPendingOutbox(
            Long crawledProductImageId, String originalUrl) {
        return ProductImageOutbox.forNewWithImageId(
                crawledProductImageId, originalUrl, Clock.systemDefaultZone());
    }

    /**
     * 특정 ID로 복원된 PENDING 상태 Outbox 생성
     *
     * @param id Outbox ID
     * @param crawledProductImageId 이미지 ID
     * @return ProductImageOutbox
     */
    public static ProductImageOutbox aReconstitutedPending(Long id, Long crawledProductImageId) {
        return ProductImageOutbox.reconstitute(
                id,
                crawledProductImageId,
                "img-" + crawledProductImageId + "-12345-abc123",
                ProductOutboxStatus.PENDING,
                0,
                null,
                Instant.now(),
                null);
    }

    /**
     * CrawledProductImage로 신규 Outbox 생성
     *
     * @param image 이미지
     * @return ProductImageOutbox
     */
    public static ProductImageOutbox forImage(CrawledProductImage image) {
        return ProductImageOutbox.forNew(image, Clock.systemDefaultZone());
    }
}
