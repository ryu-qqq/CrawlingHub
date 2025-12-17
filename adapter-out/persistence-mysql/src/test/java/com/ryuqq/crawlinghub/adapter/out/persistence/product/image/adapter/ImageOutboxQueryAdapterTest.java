package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.product.ProductImageOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.adapter.ImageOutboxQueryAdapter;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper.ProductImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.repository.ProductImageOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
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
    @DisplayName("성공 - ID로 ProductImageOutbox 조회")
    void shouldFindById() {
        // Given
        Long outboxId = 1L;
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        outboxId,
                        1L, // crawledProductImageId
                        "img-1-12345-abc123",
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        ProductImageOutbox domain = ProductImageOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findById(outboxId)).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<ProductImageOutbox> result = queryAdapter.findById(outboxId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(domain);
    }

    @Test
    @DisplayName("성공 - ID로 ProductImageOutbox 조회 (없는 경우)")
    void shouldReturnEmptyWhenNotFoundById() {
        // Given
        Long outboxId = 999L;
        given(queryDslRepository.findById(outboxId)).willReturn(Optional.empty());

        // When
        Optional<ProductImageOutbox> result = queryAdapter.findById(outboxId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - 멱등성 키로 ProductImageOutbox 조회")
    void shouldFindByIdempotencyKey() {
        // Given
        String idempotencyKey = "img-1-12345-abc123";
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L, 1L, idempotencyKey, ProductOutboxStatus.PENDING, 0, null, now, null);
        ProductImageOutbox domain = ProductImageOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findByIdempotencyKey(idempotencyKey))
                .willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<ProductImageOutbox> result = queryAdapter.findByIdempotencyKey(idempotencyKey);

        // Then
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("성공 - CrawledProductImageId로 ProductImageOutbox 조회")
    void shouldFindByCrawledProductImageId() {
        // Given
        Long crawledProductImageId = 1L;
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        crawledProductImageId,
                        "img-1-12345-abc123",
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        ProductImageOutbox domain = ProductImageOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findByCrawledProductImageId(crawledProductImageId))
                .willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<ProductImageOutbox> result =
                queryAdapter.findByCrawledProductImageId(crawledProductImageId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(domain);
    }

    @Test
    @DisplayName("성공 - CrawledProductImageId로 ProductImageOutbox 조회 (없는 경우)")
    void shouldReturnEmptyWhenNotFoundByCrawledProductImageId() {
        // Given
        Long crawledProductImageId = 999L;
        given(queryDslRepository.findByCrawledProductImageId(crawledProductImageId))
                .willReturn(Optional.empty());

        // When
        Optional<ProductImageOutbox> result =
                queryAdapter.findByCrawledProductImageId(crawledProductImageId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - PENDING 상태 ProductImageOutbox 조회")
    void shouldFindPendingOutboxes() {
        // Given
        int limit = 10;
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        1L,
                        "img-1-12345-abc123",
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        ProductImageOutbox domain = ProductImageOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findPendingOutboxes(limit)).willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<ProductImageOutbox> result = queryAdapter.findPendingOutboxes(limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 재시도 가능한 ProductImageOutbox 조회")
    void shouldFindRetryableOutboxes() {
        // Given
        int maxRetryCount = 3;
        int limit = 10;
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        1L,
                        "img-1-12345-abc123",
                        ProductOutboxStatus.FAILED,
                        1,
                        "Upload timeout",
                        now,
                        now);
        ProductImageOutbox domain = ProductImageOutboxFixture.aReconstitutedFailed();

        given(queryDslRepository.findRetryableOutboxes(maxRetryCount, limit))
                .willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<ProductImageOutbox> result = queryAdapter.findRetryableOutboxes(maxRetryCount, limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 상태로 ProductImageOutbox 조회")
    void shouldFindByStatus() {
        // Given
        ProductOutboxStatus status = ProductOutboxStatus.PROCESSING;
        int limit = 10;
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L, 1L, "img-1-12345-abc123", status, 0, null, now, now);
        ProductImageOutbox domain = ProductImageOutboxFixture.aReconstitutedProcessing();

        given(queryDslRepository.findByStatus(status, limit)).willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<ProductImageOutbox> result = queryAdapter.findByStatus(status, limit);

        // Then
        assertThat(result).hasSize(1);
    }
}
