package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskQueryDslRepository;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
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
 * CrawlTaskQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlTaskQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlTaskQueryAdapterTest {

    @Mock private CrawlTaskQueryDslRepository queryDslRepository;

    @Mock private CrawlTaskJpaEntityMapper mapper;

    private CrawlTaskQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new CrawlTaskQueryAdapter(queryDslRepository, mapper);
    }

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("성공 - ID로 CrawlTask 조회")
        void shouldFindById() {
            // Given
            CrawlTaskId taskId = CrawlTaskId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskJpaEntity entity =
                    CrawlTaskJpaEntity.of(
                            1L,
                            1L,
                            1L,
                            CrawlTaskType.MINI_SHOP,
                            "https://example.com",
                            "/api",
                            "{}",
                            CrawlTaskStatus.WAITING,
                            0,
                            now,
                            now);
            CrawlTask domain = CrawlTaskFixture.aWaitingTask();

            given(queryDslRepository.findById(1L)).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            Optional<CrawlTask> result = queryAdapter.findById(taskId);

            // Then
            assertThat(result).isPresent();
            verify(queryDslRepository).findById(1L);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 ID 조회 시 빈 Optional 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlTaskId taskId = CrawlTaskId.of(999L);
            given(queryDslRepository.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<CrawlTask> result = queryAdapter.findById(taskId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByScheduleIdAndStatusIn 테스트")
    class ExistsByScheduleIdAndStatusInTests {

        @Test
        @DisplayName("성공 - 스케줄러 ID와 상태 목록으로 존재 확인")
        void shouldCheckExistence() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerId.of(1L);
            List<CrawlTaskStatus> statuses =
                    List.of(CrawlTaskStatus.WAITING, CrawlTaskStatus.RUNNING);
            given(queryDslRepository.existsBySchedulerIdAndStatusIn(1L, statuses)).willReturn(true);

            // When
            boolean result = queryAdapter.existsByScheduleIdAndStatusIn(schedulerId, statuses);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("findByCriteria 테스트")
    class FindByCriteriaTests {

        @Test
        @DisplayName("성공 - 조건으로 CrawlTask 목록 조회")
        void shouldFindByCriteria() {
            // Given
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(
                            CrawlSchedulerId.of(1L), CrawlTaskStatus.WAITING, null, 0, 10);
            LocalDateTime now = LocalDateTime.now();
            CrawlTaskJpaEntity entity =
                    CrawlTaskJpaEntity.of(
                            1L,
                            1L,
                            1L,
                            CrawlTaskType.MINI_SHOP,
                            "https://example.com",
                            "/api",
                            "{}",
                            CrawlTaskStatus.WAITING,
                            0,
                            now,
                            now);
            CrawlTask domain = CrawlTaskFixture.aWaitingTask();

            given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<CrawlTask> result = queryAdapter.findByCriteria(criteria);

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
            CrawlTaskCriteria criteria =
                    new CrawlTaskCriteria(
                            CrawlSchedulerId.of(1L), CrawlTaskStatus.SUCCESS, null, 0, 10);
            given(queryDslRepository.countByCriteria(criteria)).willReturn(15L);

            // When
            long result = queryAdapter.countByCriteria(criteria);

            // Then
            assertThat(result).isEqualTo(15L);
        }
    }
}
