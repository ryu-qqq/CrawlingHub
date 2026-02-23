package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskOutboxCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskOutboxQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlTaskOutboxQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlTaskOutboxQueryAdapterTest {

    @Mock private CrawlTaskOutboxQueryDslRepository queryDslRepository;

    @Mock private CrawlTaskOutboxJpaEntityMapper mapper;

    private CrawlTaskOutboxQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new CrawlTaskOutboxQueryAdapter(queryDslRepository, mapper);
    }

    @Nested
    @DisplayName("findByCrawlTaskId 테스트")
    class FindByCrawlTaskIdTests {

        @Test
        @DisplayName("성공 - Task ID로 Outbox 조회")
        void shouldFindByTaskId() {
            // Given
            CrawlTaskId taskId = CrawlTaskId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskOutboxJpaEntity entity =
                    CrawlTaskOutboxJpaEntity.of(
                            1L, "idempotency-key", "{}", OutboxStatus.PENDING, 0, now, null);
            CrawlTaskOutbox domain = CrawlTaskOutboxFixture.aPendingOutbox();

            given(queryDslRepository.findByCrawlTaskId(1L)).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            Optional<CrawlTaskOutbox> result = queryAdapter.findByCrawlTaskId(taskId);

            // Then
            assertThat(result).isPresent();
            verify(queryDslRepository).findByCrawlTaskId(1L);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 Task ID 조회 시 빈 Optional 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlTaskId taskId = CrawlTaskId.of(999L);
            given(queryDslRepository.findByCrawlTaskId(999L)).willReturn(Optional.empty());

            // When
            Optional<CrawlTaskOutbox> result = queryAdapter.findByCrawlTaskId(taskId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCriteria 테스트")
    class FindByCriteriaTests {

        @Test
        @DisplayName("성공 - 조건으로 Outbox 목록 조회")
        void shouldFindByCriteria() {
            // Given
            CrawlTaskOutboxCriteria criteria =
                    CrawlTaskOutboxCriteria.byStatus(OutboxStatus.PENDING, 10);
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskOutboxJpaEntity entity =
                    CrawlTaskOutboxJpaEntity.of(
                            1L, "idempotency-key", "{}", OutboxStatus.PENDING, 0, now, null);
            CrawlTaskOutbox domain = CrawlTaskOutboxFixture.aPendingOutbox();

            given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<CrawlTaskOutbox> result = queryAdapter.findByCriteria(criteria);

            // Then
            assertThat(result).hasSize(1);
            verify(queryDslRepository).findByCriteria(criteria);
        }
    }
}
