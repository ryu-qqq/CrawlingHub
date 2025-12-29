package com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.adapter.SyncOutboxQueryAdapter;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.entity.ProductSyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.mapper.ProductSyncOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.repository.ProductSyncOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
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
 * SyncOutboxQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("SyncOutboxQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SyncOutboxQueryAdapterTest {

    @Mock private ProductSyncOutboxQueryDslRepository queryDslRepository;

    @Mock private ProductSyncOutboxJpaEntityMapper mapper;

    private SyncOutboxQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new SyncOutboxQueryAdapter(queryDslRepository, mapper);
    }

    @Test
    @DisplayName("성공 - ID로 SyncOutbox 조회")
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
    @DisplayName("성공 - ID로 SyncOutbox 조회 (없는 경우)")
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
    @DisplayName("성공 - CrawledProductId로 SyncOutbox 목록 조회")
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
    @DisplayName("성공 - PENDING 상태 SyncOutbox 조회")
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
    @DisplayName("성공 - 재시도 가능한 SyncOutbox 조회")
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
}
