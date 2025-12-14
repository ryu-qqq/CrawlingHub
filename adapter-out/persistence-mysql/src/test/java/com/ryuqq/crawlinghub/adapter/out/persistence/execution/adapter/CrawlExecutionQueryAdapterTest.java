package com.ryuqq.crawlinghub.adapter.out.persistence.execution.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity.CrawlExecutionJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.mapper.CrawlExecutionJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.repository.CrawlExecutionQueryDslRepository;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
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
 * CrawlExecutionQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlExecutionQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlExecutionQueryAdapterTest {

    @Mock private CrawlExecutionQueryDslRepository queryDslRepository;

    @Mock private CrawlExecutionJpaEntityMapper mapper;

    private CrawlExecutionQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new CrawlExecutionQueryAdapter(queryDslRepository, mapper);
    }

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("성공 - ID로 CrawlExecution 조회")
        void shouldFindById() {
            // Given
            CrawlExecutionId executionId = CrawlExecutionId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            CrawlExecutionJpaEntity entity =
                    CrawlExecutionJpaEntity.of(
                            1L,
                            1L,
                            1L,
                            1L,
                            CrawlExecutionStatus.RUNNING,
                            null,
                            null,
                            null,
                            now,
                            null,
                            null,
                            now);
            CrawlExecution domain = CrawlExecutionFixture.aRunningExecution();

            given(queryDslRepository.findById(1L)).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            Optional<CrawlExecution> result = queryAdapter.findById(executionId);

            // Then
            assertThat(result).isPresent();
            verify(queryDslRepository).findById(1L);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 ID 조회 시 빈 Optional 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlExecutionId executionId = CrawlExecutionId.of(999L);
            given(queryDslRepository.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<CrawlExecution> result = queryAdapter.findById(executionId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCriteria 테스트")
    class FindByCriteriaTests {

        @Test
        @DisplayName("성공 - 조건으로 CrawlExecution 목록 조회")
        void shouldFindByCriteria() {
            // Given
            CrawlExecutionCriteria criteria =
                    new CrawlExecutionCriteria(
                            null, null, CrawlExecutionStatus.RUNNING, null, null, 0, 10);
            LocalDateTime now = LocalDateTime.now();
            CrawlExecutionJpaEntity entity =
                    CrawlExecutionJpaEntity.of(
                            1L,
                            1L,
                            1L,
                            1L,
                            CrawlExecutionStatus.RUNNING,
                            null,
                            null,
                            null,
                            now,
                            null,
                            null,
                            now);
            CrawlExecution domain = CrawlExecutionFixture.aRunningExecution();

            given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<CrawlExecution> result = queryAdapter.findByCriteria(criteria);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("countByCriteria 테스트")
    class CountByCriteriaTests {

        @Test
        @DisplayName("성공 - 조건으로 개수 조회")
        void shouldCountByCriteria() {
            // Given
            CrawlExecutionCriteria criteria =
                    new CrawlExecutionCriteria(
                            null, null, CrawlExecutionStatus.SUCCESS, null, null, 0, 10);
            given(queryDslRepository.countByCriteria(criteria)).willReturn(10L);

            // When
            long result = queryAdapter.countByCriteria(criteria);

            // Then
            assertThat(result).isEqualTo(10L);
        }
    }
}
