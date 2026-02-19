package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerOutBoxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerOutBoxQueryDslRepository;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
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
 * CrawlSchedulerOutBoxQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlSchedulerOutBoxQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlSchedulerOutBoxQueryAdapterTest {

    @Mock private CrawlSchedulerOutBoxQueryDslRepository queryDslRepository;

    @Mock private CrawlSchedulerOutBoxJpaEntityMapper mapper;

    private CrawlSchedulerOutBoxQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new CrawlSchedulerOutBoxQueryAdapter(queryDslRepository, mapper);
    }

    @Nested
    @DisplayName("findByHistoryId 테스트")
    class FindByHistoryIdTests {

        @Test
        @DisplayName("성공 - History ID로 OutBox 조회")
        void shouldFindByHistoryId() {
            // Given
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerOutBoxJpaEntity entity =
                    CrawlSchedulerOutBoxJpaEntity.of(
                            1L, 1L, CrawlSchedulerOubBoxStatus.PENDING, "{}", null, 0L, now, null);
            CrawlSchedulerOutBox domain = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            given(queryDslRepository.findByHistoryId(1L)).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            Optional<CrawlSchedulerOutBox> result = queryAdapter.findByHistoryId(historyId);

            // Then
            assertThat(result).isPresent();
            verify(queryDslRepository).findByHistoryId(1L);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 History ID 조회 시 빈 Optional 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryId.of(999L);
            given(queryDslRepository.findByHistoryId(999L)).willReturn(Optional.empty());

            // When
            Optional<CrawlSchedulerOutBox> result = queryAdapter.findByHistoryId(historyId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatus 테스트")
    class FindByStatusTests {

        @Test
        @DisplayName("성공 - 상태별 OutBox 목록 조회")
        void shouldFindByStatus() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerOutBoxJpaEntity entity =
                    CrawlSchedulerOutBoxJpaEntity.of(
                            1L, 1L, CrawlSchedulerOubBoxStatus.PENDING, "{}", null, 0L, now, null);
            CrawlSchedulerOutBox domain = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            given(queryDslRepository.findByStatus(CrawlSchedulerOubBoxStatus.PENDING, 10))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<CrawlSchedulerOutBox> result =
                    queryAdapter.findByStatus(CrawlSchedulerOubBoxStatus.PENDING, 10);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findPendingOrFailed 테스트")
    class FindPendingOrFailedTests {

        @Test
        @DisplayName("성공 - PENDING 또는 FAILED 상태의 OutBox 조회")
        void shouldFindPendingOrFailed() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerOutBoxJpaEntity entity =
                    CrawlSchedulerOutBoxJpaEntity.of(
                            1L, 1L, CrawlSchedulerOubBoxStatus.PENDING, "{}", null, 0L, now, null);
            CrawlSchedulerOutBox domain = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            given(queryDslRepository.findByStatusIn(anyList(), eq(10))).willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<CrawlSchedulerOutBox> result = queryAdapter.findPendingOrFailed(10);

            // Then
            assertThat(result).hasSize(1);
        }
    }
}
