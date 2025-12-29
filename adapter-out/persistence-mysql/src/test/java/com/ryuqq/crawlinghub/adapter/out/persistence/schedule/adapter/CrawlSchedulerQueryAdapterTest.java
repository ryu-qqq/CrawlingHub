package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerQueryDslRepository;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
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
 * CrawlSchedulerQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlSchedulerQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlSchedulerQueryAdapterTest {

    @Mock private CrawlSchedulerQueryDslRepository queryDslRepository;

    @Mock private CrawlSchedulerJpaEntityMapper mapper;

    private CrawlSchedulerQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new CrawlSchedulerQueryAdapter(queryDslRepository, mapper);
    }

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("성공 - ID로 CrawlScheduler 조회")
        void shouldFindById() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerJpaEntity entity =
                    CrawlSchedulerJpaEntity.of(
                            1L,
                            1L,
                            "test-scheduler",
                            "0 0 * * *",
                            SchedulerStatus.ACTIVE,
                            now,
                            now);
            CrawlScheduler domain = CrawlSchedulerFixture.anActiveScheduler();

            given(queryDslRepository.findById(1L)).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            Optional<CrawlScheduler> result = queryAdapter.findById(schedulerId);

            // Then
            assertThat(result).isPresent();
            verify(queryDslRepository).findById(1L);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 ID 조회 시 빈 Optional 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerId.of(999L);
            given(queryDslRepository.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<CrawlScheduler> result = queryAdapter.findById(schedulerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsBySellerIdAndSchedulerName 테스트")
    class ExistsBySellerIdAndSchedulerNameTests {

        @Test
        @DisplayName("성공 - 셀러 ID와 스케줄러 이름으로 존재 확인")
        void shouldCheckExistence() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            String schedulerName = "test-scheduler";
            given(queryDslRepository.existsBySellerIdAndSchedulerName(1L, schedulerName))
                    .willReturn(true);

            // When
            boolean result = queryAdapter.existsBySellerIdAndSchedulerName(sellerId, schedulerName);

            // Then
            assertThat(result).isTrue();
            verify(queryDslRepository).existsBySellerIdAndSchedulerName(1L, schedulerName);
        }
    }

    @Nested
    @DisplayName("findByCriteria 테스트")
    class FindByCriteriaTests {

        @Test
        @DisplayName("성공 - 조건으로 CrawlScheduler 목록 조회")
        void shouldFindByCriteria() {
            // Given
            CrawlSchedulerQueryCriteria criteria =
                    new CrawlSchedulerQueryCriteria(
                            null, List.of(SchedulerStatus.ACTIVE), null, null, 0, 10);
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerJpaEntity entity =
                    CrawlSchedulerJpaEntity.of(
                            1L,
                            1L,
                            "test-scheduler",
                            "0 0 * * *",
                            SchedulerStatus.ACTIVE,
                            now,
                            now);
            CrawlScheduler domain = CrawlSchedulerFixture.anActiveScheduler();

            given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<CrawlScheduler> result = queryAdapter.findByCriteria(criteria);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("count 테스트")
    class CountTests {

        @Test
        @DisplayName("성공 - 조건으로 개수 조회")
        void shouldCount() {
            // Given
            CrawlSchedulerQueryCriteria criteria =
                    new CrawlSchedulerQueryCriteria(
                            null, List.of(SchedulerStatus.ACTIVE), null, null, null, null);
            given(queryDslRepository.countByCriteria(criteria)).willReturn(5L);

            // When
            long result = queryAdapter.count(criteria);

            // Then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("findActiveSchedulersBySellerId 테스트")
    class FindActiveSchedulersBySellerIdTests {

        @Test
        @DisplayName("성공 - 셀러별 활성 스케줄러 조회")
        void shouldFindActiveSchedulersBySellerId() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerJpaEntity entity =
                    CrawlSchedulerJpaEntity.of(
                            1L,
                            1L,
                            "test-scheduler",
                            "0 0 * * *",
                            SchedulerStatus.ACTIVE,
                            now,
                            now);
            CrawlScheduler domain = CrawlSchedulerFixture.anActiveScheduler();

            given(queryDslRepository.findActiveBySellerIdmethod(1L)).willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<CrawlScheduler> result = queryAdapter.findActiveSchedulersBySellerId(sellerId);

            // Then
            assertThat(result).hasSize(1);
        }
    }
}
