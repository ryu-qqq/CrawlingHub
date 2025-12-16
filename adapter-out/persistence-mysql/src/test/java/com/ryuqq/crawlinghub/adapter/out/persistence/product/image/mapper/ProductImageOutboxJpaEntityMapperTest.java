package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductImageOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ProductImageOutboxJpaEntityMapper 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("ProductImageOutboxJpaEntityMapper 단위 테스트")
class ProductImageOutboxJpaEntityMapperTest {

    private ProductImageOutboxJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductImageOutboxJpaEntityMapper();
    }

    @Test
    @DisplayName("성공 - Domain -> Entity 변환 (PENDING 상태)")
    void shouldConvertDomainToEntityWithPendingStatus() {
        // Given
        CrawledProductImageOutbox domain = CrawledProductImageOutboxFixture.aReconstitutedPending();

        // When
        ProductImageOutboxJpaEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getCrawledProductId()).isEqualTo(domain.getCrawledProductIdValue());
        assertThat(entity.getImageType()).isEqualTo(domain.getImageType());
        assertThat(entity.getOriginalUrl()).isEqualTo(domain.getOriginalUrl());
        assertThat(entity.getIdempotencyKey()).isEqualTo(domain.getIdempotencyKey());
        assertThat(entity.getS3Url()).isNull();
        assertThat(entity.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
        assertThat(entity.getRetryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("성공 - Domain -> Entity 변환 (COMPLETED 상태)")
    void shouldConvertDomainToEntityWithCompletedStatus() {
        // Given
        CrawledProductImageOutbox domain =
                CrawledProductImageOutboxFixture.aReconstitutedCompleted();

        // When
        ProductImageOutboxJpaEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(ProductOutboxStatus.COMPLETED);
        assertThat(entity.getS3Url())
                .isEqualTo("https://s3.amazonaws.com/bucket/uploaded-image.jpg");
    }

    @Test
    @DisplayName("성공 - Domain -> Entity 변환 (FAILED 상태)")
    void shouldConvertDomainToEntityWithFailedStatus() {
        // Given
        CrawledProductImageOutbox domain = CrawledProductImageOutboxFixture.aReconstitutedFailed();

        // When
        ProductImageOutboxJpaEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(ProductOutboxStatus.FAILED);
        assertThat(entity.getRetryCount()).isEqualTo(1);
        assertThat(entity.getErrorMessage()).isEqualTo("Upload timeout");
    }

    @Test
    @DisplayName("성공 - Entity -> Domain 변환 (PENDING 상태)")
    void shouldConvertEntityToDomainWithPendingStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        1L,
                        ImageType.THUMBNAIL,
                        "https://example.com/image.jpg",
                        "img-1-12345-abc123",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);

        // When
        CrawledProductImageOutbox domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getCrawledProductId()).isEqualTo(CrawledProductId.of(1L));
        assertThat(domain.getImageType()).isEqualTo(ImageType.THUMBNAIL);
        assertThat(domain.getOriginalUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(domain.getIdempotencyKey()).isEqualTo("img-1-12345-abc123");
        assertThat(domain.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
        assertThat(domain.isPending()).isTrue();
    }

    @Test
    @DisplayName("성공 - Entity -> Domain 변환 (COMPLETED 상태)")
    void shouldConvertEntityToDomainWithCompletedStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        1L,
                        ImageType.DESCRIPTION,
                        "https://example.com/detail-image.jpg",
                        "img-1-67890-def456",
                        "https://s3.amazonaws.com/bucket/uploaded.jpg",
                        ProductOutboxStatus.COMPLETED,
                        0,
                        null,
                        now,
                        now);

        // When
        CrawledProductImageOutbox domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getStatus()).isEqualTo(ProductOutboxStatus.COMPLETED);
        assertThat(domain.getS3Url()).isEqualTo("https://s3.amazonaws.com/bucket/uploaded.jpg");
        assertThat(domain.getImageType()).isEqualTo(ImageType.DESCRIPTION);
        assertThat(domain.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("성공 - Entity -> Domain 변환 (FAILED 상태)")
    void shouldConvertEntityToDomainWithFailedStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        1L,
                        ImageType.THUMBNAIL,
                        "https://example.com/image.jpg",
                        "img-1-12345-abc123",
                        null,
                        ProductOutboxStatus.FAILED,
                        2,
                        "Upload timeout",
                        now,
                        now);

        // When
        CrawledProductImageOutbox domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getStatus()).isEqualTo(ProductOutboxStatus.FAILED);
        assertThat(domain.getRetryCount()).isEqualTo(2);
        assertThat(domain.getErrorMessage()).isEqualTo("Upload timeout");
        assertThat(domain.canRetry()).isTrue();
    }
}
