package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.product.ProductImageOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.adapter.ImageOutboxQueryAdapter;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.dto.ProductImageOutboxWithImageDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper.ProductImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.repository.ProductImageOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxWithImageResponse;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
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

    @Test
    @DisplayName("성공 - 조건으로 ProductImageOutbox 검색")
    void shouldSearchWithConditions() {
        // Given
        Long crawledProductImageId = 1L;
        ProductOutboxStatus status = ProductOutboxStatus.PENDING;
        long offset = 0L;
        int size = 20;
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        crawledProductImageId,
                        "img-1-12345-abc123",
                        status,
                        0,
                        null,
                        now,
                        null);
        ProductImageOutbox domain = ProductImageOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.search(crawledProductImageId, status, offset, size))
                .willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<ProductImageOutbox> result =
                queryAdapter.search(crawledProductImageId, status, offset, size);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 조건으로 ProductImageOutbox 개수 조회")
    void shouldCountWithConditions() {
        // Given
        Long crawledProductImageId = 1L;
        ProductOutboxStatus status = ProductOutboxStatus.PENDING;
        long expectedCount = 5L;

        given(queryDslRepository.count(crawledProductImageId, status)).willReturn(expectedCount);

        // When
        long result = queryAdapter.count(crawledProductImageId, status);

        // Then
        assertThat(result).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("성공 - 타임아웃된 PROCESSING 상태 ProductImageOutbox 조회")
    void shouldFindTimedOutProcessingOutboxes() {
        // Given
        int timeoutSeconds = 300;
        int limit = 10;
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        1L,
                        "img-1-12345-abc123",
                        ProductOutboxStatus.PROCESSING,
                        0,
                        null,
                        now.minusMinutes(10),
                        now.minusMinutes(10));
        ProductImageOutbox domain = ProductImageOutboxFixture.aReconstitutedProcessing();

        given(queryDslRepository.findTimedOutProcessingOutboxes(timeoutSeconds, limit))
                .willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<ProductImageOutbox> result =
                queryAdapter.findTimedOutProcessingOutboxes(timeoutSeconds, limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 이미지 정보 포함 ProductImageOutbox 검색")
    void shouldSearchWithImageInfo() {
        // Given
        Long crawledProductImageId = 100L;
        ProductOutboxStatus status = ProductOutboxStatus.PENDING;
        long offset = 0L;
        int size = 20;
        LocalDateTime now = LocalDateTime.now();

        ProductImageOutboxWithImageDto dto =
                new ProductImageOutboxWithImageDto(
                        1L,
                        crawledProductImageId,
                        "image-100-1234567890",
                        status,
                        0,
                        null,
                        now,
                        null,
                        50L,
                        "https://example.com/image.jpg",
                        "https://s3.example.com/image.jpg",
                        ImageType.THUMBNAIL);

        given(queryDslRepository.searchWithImageInfo(crawledProductImageId, status, offset, size))
                .willReturn(List.of(dto));

        // When
        List<ProductImageOutboxWithImageResponse> result =
                queryAdapter.searchWithImageInfo(crawledProductImageId, status, offset, size);

        // Then
        assertThat(result).hasSize(1);
        ProductImageOutboxWithImageResponse response = result.get(0);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.crawledProductImageId()).isEqualTo(crawledProductImageId);
        assertThat(response.crawledProductId()).isEqualTo(50L);
        assertThat(response.originalUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(response.s3Url()).isEqualTo("https://s3.example.com/image.jpg");
        assertThat(response.imageType()).isEqualTo(ImageType.THUMBNAIL);
    }

    @Test
    @DisplayName("성공 - 이미지 정보 포함 검색 (빈 결과)")
    void shouldReturnEmptyWhenSearchWithImageInfoNoResults() {
        // Given
        Long crawledProductImageId = 999L;
        ProductOutboxStatus status = ProductOutboxStatus.PENDING;
        long offset = 0L;
        int size = 20;

        given(queryDslRepository.searchWithImageInfo(crawledProductImageId, status, offset, size))
                .willReturn(List.of());

        // When
        List<ProductImageOutboxWithImageResponse> result =
                queryAdapter.searchWithImageInfo(crawledProductImageId, status, offset, size);

        // Then
        assertThat(result).isEmpty();
    }
}
