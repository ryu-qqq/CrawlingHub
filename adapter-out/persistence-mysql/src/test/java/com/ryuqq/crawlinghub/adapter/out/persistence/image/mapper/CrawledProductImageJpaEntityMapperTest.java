package com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductImageFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.CrawledProductImageJpaEntity;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawledProductImageJpaEntityMapper 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawledProductImageJpaEntityMapper 단위 테스트")
class CrawledProductImageJpaEntityMapperTest {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private CrawledProductImageJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawledProductImageJpaEntityMapper();
    }

    @Test
    @DisplayName("성공 - Domain → Entity 변환 (업로드 전)")
    void shouldConvertDomainToEntityWhenPending() {
        // Given
        CrawledProductImage domain = CrawledProductImageFixture.aReconstitutedThumbnailPending();

        // When
        CrawledProductImageJpaEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getCrawledProductId()).isEqualTo(1L);
        assertThat(entity.getOriginalUrl()).isEqualTo("https://example.com/thumbnail.jpg");
        assertThat(entity.getS3Url()).isNull();
        assertThat(entity.getImageType()).isEqualTo(ImageType.THUMBNAIL);
        assertThat(entity.getDisplayOrder()).isZero();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("성공 - Domain → Entity 변환 (업로드 완료)")
    void shouldConvertDomainToEntityWhenUploaded() {
        // Given
        CrawledProductImage domain = CrawledProductImageFixture.aReconstitutedThumbnailUploaded();

        // When
        CrawledProductImageJpaEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getCrawledProductId()).isEqualTo(1L);
        assertThat(entity.getOriginalUrl()).isEqualTo("https://example.com/thumbnail.jpg");
        assertThat(entity.getS3Url()).isEqualTo("https://s3.amazonaws.com/bucket/thumbnail.jpg");
        assertThat(entity.getImageType()).isEqualTo(ImageType.THUMBNAIL);
        assertThat(entity.getDisplayOrder()).isZero();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("성공 - Entity → Domain 변환 (업로드 전)")
    void shouldConvertEntityToDomainWhenPending() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        CrawledProductImageJpaEntity entity =
                CrawledProductImageJpaEntity.of(
                        1L,
                        1L,
                        "https://example.com/thumbnail.jpg",
                        null,
                        null,
                        ImageType.THUMBNAIL,
                        0,
                        now,
                        null);

        // When
        CrawledProductImage domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getCrawledProductIdValue()).isEqualTo(1L);
        assertThat(domain.getOriginalUrl()).isEqualTo("https://example.com/thumbnail.jpg");
        assertThat(domain.getS3Url()).isNull();
        assertThat(domain.getImageType()).isEqualTo(ImageType.THUMBNAIL);
        assertThat(domain.getDisplayOrder()).isZero();
        assertThat(domain.getCreatedAt()).isNotNull();
        assertThat(domain.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("성공 - Entity → Domain 변환 (업로드 완료)")
    void shouldConvertEntityToDomainWhenUploaded() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        CrawledProductImageJpaEntity entity =
                CrawledProductImageJpaEntity.of(
                        1L,
                        1L,
                        "https://example.com/thumbnail.jpg",
                        "https://s3.amazonaws.com/bucket/thumbnail.jpg",
                        "asset-uuid-123",
                        ImageType.THUMBNAIL,
                        0,
                        now,
                        now);

        // When
        CrawledProductImage domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getCrawledProductIdValue()).isEqualTo(1L);
        assertThat(domain.getOriginalUrl()).isEqualTo("https://example.com/thumbnail.jpg");
        assertThat(domain.getS3Url()).isEqualTo("https://s3.amazonaws.com/bucket/thumbnail.jpg");
        assertThat(domain.getImageType()).isEqualTo(ImageType.THUMBNAIL);
        assertThat(domain.getDisplayOrder()).isZero();
        assertThat(domain.getCreatedAt()).isNotNull();
        assertThat(domain.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("성공 - 시간 변환 (Instant ↔ LocalDateTime)")
    void shouldConvertTimeCorrectly() {
        // Given
        Instant originalInstant = Instant.parse("2025-01-01T00:00:00Z");
        CrawledProductImage domain =
                CrawledProductImageFixture.aReconstitutedUploaded(
                        1L,
                        1L,
                        "https://example.com/test.jpg",
                        "https://s3.amazonaws.com/test.jpg",
                        ImageType.THUMBNAIL,
                        0);

        // When
        CrawledProductImageJpaEntity entity = mapper.toEntity(domain);
        CrawledProductImage reconstituted = mapper.toDomain(entity);

        // Then
        assertThat(reconstituted.getId()).isEqualTo(domain.getId());
        assertThat(reconstituted.getOriginalUrl()).isEqualTo(domain.getOriginalUrl());
        assertThat(reconstituted.getS3Url()).isEqualTo(domain.getS3Url());
    }

    @Test
    @DisplayName("성공 - 다양한 ImageType 변환")
    void shouldConvertDifferentImageTypes() {
        // Given - DESCRIPTION 타입
        CrawledProductImage descriptionDomain =
                CrawledProductImageFixture.aReconstitutedDescriptionUploaded();

        // When
        CrawledProductImageJpaEntity entity = mapper.toEntity(descriptionDomain);

        // Then
        assertThat(entity.getImageType()).isEqualTo(ImageType.DESCRIPTION);

        // When - Entity → Domain
        CrawledProductImage reconstituted = mapper.toDomain(entity);

        // Then
        assertThat(reconstituted.getImageType()).isEqualTo(ImageType.DESCRIPTION);
    }
}
