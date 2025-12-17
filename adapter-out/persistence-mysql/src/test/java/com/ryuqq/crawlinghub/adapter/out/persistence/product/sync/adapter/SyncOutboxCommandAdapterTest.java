package com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.entity.ProductSyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.mapper.ProductSyncOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.repository.ProductSyncOutboxJpaRepository;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SyncOutboxCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("SyncOutboxCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SyncOutboxCommandAdapterTest {

    @Mock private ProductSyncOutboxJpaRepository jpaRepository;

    @Mock private ProductSyncOutboxJpaEntityMapper mapper;

    private SyncOutboxCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new SyncOutboxCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - CrawledProductSyncOutbox 저장")
    void shouldPersistOutbox() {
        // Given
        CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForCreate();
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        null,
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

        given(mapper.toEntity(outbox)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        commandAdapter.persist(outbox);

        // Then
        verify(mapper).toEntity(outbox);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - 기존 CrawledProductSyncOutbox 업데이트")
    void shouldUpdateExistingOutbox() {
        // Given
        CrawledProductSyncOutbox outbox =
                CrawledProductSyncOutboxFixture.aReconstitutedProcessing();
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
                        ProductOutboxStatus.PROCESSING,
                        0,
                        null,
                        now,
                        now);

        given(mapper.toEntity(outbox)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        commandAdapter.update(outbox);

        // Then
        verify(mapper).toEntity(outbox);
        verify(jpaRepository).save(entity);
    }
}
