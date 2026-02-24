package com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter.CrawledProductSyncOutboxQueryAdapter;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.ProductSyncOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.ProductSyncOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.query.ProductSyncOutboxCriteria;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
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
 * CrawledProductSyncOutboxQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawledProductSyncOutboxQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawledProductSyncOutboxQueryAdapterTest {

    @Mock private ProductSyncOutboxQueryDslRepository queryDslRepository;

    @Mock private ProductSyncOutboxJpaEntityMapper mapper;

    private CrawledProductSyncOutboxQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new CrawledProductSyncOutboxQueryAdapter(queryDslRepository, mapper);
    }

    @Test
    @DisplayName("성공 - ID로 CrawledProductSyncOutbox 조회")
    void shouldFindById() {
        // Given
        Long outboxId = 1L;
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        outboxId,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findById(outboxId)).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<CrawledProductSyncOutbox> result = queryAdapter.findById(outboxId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(domain);
    }

    @Test
    @DisplayName("성공 - ID로 CrawledProductSyncOutbox 조회 (없는 경우)")
    void shouldReturnEmptyWhenNotFoundById() {
        // Given
        Long outboxId = 999L;
        given(queryDslRepository.findById(outboxId)).willReturn(Optional.empty());

        // When
        Optional<CrawledProductSyncOutbox> result = queryAdapter.findById(outboxId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - CrawledProductId로 CrawledProductSyncOutbox 목록 조회")
    void shouldFindByCrawledProductId() {
        // Given
        CrawledProductId crawledProductId = CrawledProductId.of(1L);
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findByCrawledProductId(1L)).willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductSyncOutbox> result =
                queryAdapter.findByCrawledProductId(crawledProductId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(domain);
    }

    @Test
    @DisplayName("성공 - PENDING 상태 CrawledProductSyncOutbox 조회")
    void shouldFindPendingOutboxes() {
        // Given
        int limit = 10;
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findPendingOutboxes(limit)).willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductSyncOutbox> result = queryAdapter.findPendingOutboxes(limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 재시도 가능한 CrawledProductSyncOutbox 조회")
    void shouldFindRetryableOutboxes() {
        // Given
        int maxRetryCount = 3;
        int limit = 10;
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        ProductOutboxStatus.FAILED,
                        1,
                        "Connection timeout",
                        now,
                        now);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedFailed();

        given(queryDslRepository.findRetryableOutboxes(maxRetryCount, limit))
                .willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductSyncOutbox> result =
                queryAdapter.findRetryableOutboxes(maxRetryCount, limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - IdempotencyKey로 CrawledProductSyncOutbox 조회")
    void shouldFindByIdempotencyKey() {
        // Given
        String idempotencyKey = "sync-key-123";
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        idempotencyKey,
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findByIdempotencyKey(idempotencyKey))
                .willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<CrawledProductSyncOutbox> result =
                queryAdapter.findByIdempotencyKey(idempotencyKey);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(domain);
    }

    @Test
    @DisplayName("성공 - IdempotencyKey로 CrawledProductSyncOutbox 조회 (없는 경우)")
    void shouldReturnEmptyWhenNotFoundByIdempotencyKey() {
        // Given
        String idempotencyKey = "non-existent-key";
        given(queryDslRepository.findByIdempotencyKey(idempotencyKey)).willReturn(Optional.empty());

        // When
        Optional<CrawledProductSyncOutbox> result =
                queryAdapter.findByIdempotencyKey(idempotencyKey);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - 상태로 CrawledProductSyncOutbox 목록 조회")
    void shouldFindByStatus() {
        // Given
        ProductOutboxStatus status = ProductOutboxStatus.PENDING;
        int limit = 10;
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        status,
                        0,
                        null,
                        now,
                        null);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findByStatus(status, limit)).willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductSyncOutbox> result = queryAdapter.findByStatus(status, limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - Criteria 기반 CrawledProductSyncOutbox 조회")
    void shouldFindByCriteria() {
        // Given
        ProductSyncOutboxCriteria criteria = ProductSyncOutboxCriteria.retryable(3, 100);
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductSyncOutbox> result = queryAdapter.findByCriteria(criteria);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - Criteria 기반 CrawledProductSyncOutbox 개수 조회")
    void shouldCountByCriteria() {
        // Given
        ProductSyncOutboxCriteria criteria = ProductSyncOutboxCriteria.retryable(3, 100);

        given(queryDslRepository.countByCriteria(criteria)).willReturn(5L);

        // When
        long result = queryAdapter.countByCriteria(criteria);

        // Then
        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("성공 - 조건으로 CrawledProductSyncOutbox 검색 (페이징)")
    void shouldSearch() {
        // Given
        Long crawledProductId = 1L;
        Long sellerId = 100L;
        List<Long> itemNos = List.of(12345L);
        List<ProductOutboxStatus> statuses = List.of(ProductOutboxStatus.PENDING);
        Instant createdFrom = Instant.now().minusSeconds(3600);
        Instant createdTo = Instant.now();
        long offset = 0L;
        int size = 10;

        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        crawledProductId,
                        sellerId,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedPending();

        given(
                        queryDslRepository.search(
                                crawledProductId,
                                sellerId,
                                itemNos,
                                statuses,
                                createdFrom,
                                createdTo,
                                offset,
                                size))
                .willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductSyncOutbox> result =
                queryAdapter.search(
                        crawledProductId,
                        sellerId,
                        itemNos,
                        statuses,
                        createdFrom,
                        createdTo,
                        offset,
                        size);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 조건으로 CrawledProductSyncOutbox 개수 조회")
    void shouldCount() {
        // Given
        Long crawledProductId = 1L;
        Long sellerId = 100L;
        List<Long> itemNos = List.of(12345L);
        List<ProductOutboxStatus> statuses = List.of(ProductOutboxStatus.PENDING);
        Instant createdFrom = Instant.now().minusSeconds(3600);
        Instant createdTo = Instant.now();

        given(
                        queryDslRepository.count(
                                crawledProductId,
                                sellerId,
                                itemNos,
                                statuses,
                                createdFrom,
                                createdTo))
                .willReturn(5L);

        // When
        long result =
                queryAdapter.count(
                        crawledProductId, sellerId, itemNos, statuses, createdFrom, createdTo);

        // Then
        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("성공 - findFailedOlderThan 호출")
    void shouldFindFailedOlderThan() {
        // Given - 일정 시간 이상 경과한 FAILED 상태 Outbox 조회
        int limit = 10;
        int delaySeconds = 30;
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        ProductOutboxStatus.FAILED,
                        2,
                        "Timeout",
                        now,
                        now);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedFailed();

        given(queryDslRepository.findFailedOlderThan(limit, delaySeconds))
                .willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductSyncOutbox> result =
                queryAdapter.findFailedOlderThan(limit, delaySeconds);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - findStaleProcessing 호출")
    void shouldFindStaleProcessing() {
        // Given - 타임아웃된 PROCESSING 상태 Outbox 조회
        int limit = 5;
        long timeoutSeconds = 300L;
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.UPDATE_PRICE,
                        "sync-key-456",
                        null,
                        ProductOutboxStatus.PROCESSING,
                        0,
                        null,
                        now,
                        null);
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedPending();

        given(queryDslRepository.findStaleProcessing(limit, timeoutSeconds))
                .willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductSyncOutbox> result =
                queryAdapter.findStaleProcessing(limit, timeoutSeconds);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - existsByProductIdAndSyncTypeAndStatuses 호출")
    void shouldCheckExistsByProductIdAndSyncTypeAndStatuses() {
        // Given - 특정 상품의 SyncType과 상태로 존재 여부 확인
        Long productId = 1L;
        SyncType syncType = SyncType.CREATE;
        List<ProductOutboxStatus> statuses =
                List.of(ProductOutboxStatus.PENDING, ProductOutboxStatus.PROCESSING);

        given(
                        queryDslRepository.existsByProductIdAndSyncTypeAndStatuses(
                                productId, syncType, statuses))
                .willReturn(true);

        // When
        boolean result =
                queryAdapter.existsByProductIdAndSyncTypeAndStatuses(productId, syncType, statuses);

        // Then - 존재 여부가 true여야 함
        assertThat(result).isTrue();
    }
}
