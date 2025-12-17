package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductImageOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.mapper.ProductImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.repository.ProductImageOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ImageOutboxQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("ImageOutboxQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ImageOutboxQueryAdapterTest {

    @Mock private ProductImageOutboxQueryDslRepository queryDslRepository;

    @Mock private ProductImageOutboxJpaEntityMapper mapper;

    private ImageOutboxQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new ImageOutboxQueryAdapter(queryDslRepository, mapper);
    }

    @Test
    @DisplayName("성공 - ID로 ImageOutbox 조회")
    void shouldFindById() {
        // Given
        Long outboxId = 1L;
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        outboxId,
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
        CrawledProductImageOutbox domain = CrawledProductImageOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findById(outboxId)).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<CrawledProductImageOutbox> result = queryAdapter.findById(outboxId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(domain);
    }

    @Test
    @DisplayName("성공 - ID로 ImageOutbox 조회 (없는 경우)")
    void shouldReturnEmptyWhenNotFoundById() {
        // Given
        Long outboxId = 999L;
        given(queryDslRepository.findById(outboxId)).willReturn(Optional.empty());

        // When
        Optional<CrawledProductImageOutbox> result = queryAdapter.findById(outboxId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - 멱등성 키로 ImageOutbox 조회")
    void shouldFindByIdempotencyKey() {
        // Given
        String idempotencyKey = "img-1-12345-abc123";
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        1L,
                        ImageType.THUMBNAIL,
                        "https://example.com/image.jpg",
                        idempotencyKey,
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        CrawledProductImageOutbox domain = CrawledProductImageOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findByIdempotencyKey(idempotencyKey))
                .willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<CrawledProductImageOutbox> result =
                queryAdapter.findByIdempotencyKey(idempotencyKey);

        // Then
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("성공 - CrawledProductId로 ImageOutbox 목록 조회")
    void shouldFindByCrawledProductId() {
        // Given
        CrawledProductId crawledProductId = CrawledProductId.of(1L);
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
        CrawledProductImageOutbox domain = CrawledProductImageOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findByCrawledProductId(1L)).willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductImageOutbox> result =
                queryAdapter.findByCrawledProductId(crawledProductId);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - PENDING 상태 ImageOutbox 조회")
    void shouldFindPendingOutboxes() {
        // Given
        int limit = 10;
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
        CrawledProductImageOutbox domain = CrawledProductImageOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findPendingOutboxes(limit)).willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductImageOutbox> result = queryAdapter.findPendingOutboxes(limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 재시도 가능한 ImageOutbox 조회")
    void shouldFindRetryableOutboxes() {
        // Given
        int maxRetryCount = 3;
        int limit = 10;
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
                        1,
                        "Upload timeout",
                        now,
                        now);
        CrawledProductImageOutbox domain = CrawledProductImageOutboxFixture.aReconstitutedFailed();

        given(queryDslRepository.findRetryableOutboxes(maxRetryCount, limit))
                .willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductImageOutbox> result =
                queryAdapter.findRetryableOutboxes(maxRetryCount, limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 원본 URL로 존재 여부 확인 (존재함)")
    void shouldReturnTrueWhenExists() {
        // Given
        CrawledProductId crawledProductId = CrawledProductId.of(1L);
        String originalUrl = "https://example.com/image.jpg";

        given(queryDslRepository.existsByCrawledProductIdAndOriginalUrl(1L, originalUrl))
                .willReturn(true);

        // When
        boolean result =
                queryAdapter.existsByCrawledProductIdAndOriginalUrl(crawledProductId, originalUrl);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("성공 - 원본 URL로 존재 여부 확인 (존재하지 않음)")
    void shouldReturnFalseWhenNotExists() {
        // Given
        CrawledProductId crawledProductId = CrawledProductId.of(1L);
        String originalUrl = "https://example.com/nonexistent.jpg";

        given(queryDslRepository.existsByCrawledProductIdAndOriginalUrl(1L, originalUrl))
                .willReturn(false);

        // When
        boolean result =
                queryAdapter.existsByCrawledProductIdAndOriginalUrl(crawledProductId, originalUrl);

        // Then
        assertThat(result).isFalse();
    }
}
