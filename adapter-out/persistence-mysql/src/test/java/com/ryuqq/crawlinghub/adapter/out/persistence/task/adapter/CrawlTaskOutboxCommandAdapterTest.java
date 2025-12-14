package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskOutboxJpaRepository;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskOutboxCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlTaskOutboxCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlTaskOutboxCommandAdapterTest {

    @Mock private CrawlTaskOutboxJpaRepository jpaRepository;

    @Mock private CrawlTaskOutboxJpaEntityMapper mapper;

    private CrawlTaskOutboxCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new CrawlTaskOutboxCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - CrawlTaskOutbox 저장")
    void shouldPersistOutbox() {
        // Given
        CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();
        LocalDateTime now = LocalDateTime.now();
        CrawlTaskOutboxJpaEntity entity =
                CrawlTaskOutboxJpaEntity.of(
                        1L, "idempotency-key", "{}", OutboxStatus.PENDING, 0, now, null);
        CrawlTaskOutboxJpaEntity savedEntity =
                CrawlTaskOutboxJpaEntity.of(
                        1L, "idempotency-key", "{}", OutboxStatus.PENDING, 0, now, null);

        given(mapper.toEntity(outbox)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(savedEntity);

        // When
        commandAdapter.persist(outbox);

        // Then
        verify(mapper).toEntity(outbox);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - 기존 CrawlTaskOutbox 수정")
    void shouldUpdateExistingOutbox() {
        // Given
        CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aSentOutbox();
        LocalDateTime now = LocalDateTime.now();
        CrawlTaskOutboxJpaEntity entity =
                CrawlTaskOutboxJpaEntity.of(
                        1L, "idempotency-key", "{}", OutboxStatus.SENT, 0, now, now);

        given(mapper.toEntity(outbox)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        commandAdapter.persist(outbox);

        // Then
        verify(jpaRepository).save(entity);
    }
}
