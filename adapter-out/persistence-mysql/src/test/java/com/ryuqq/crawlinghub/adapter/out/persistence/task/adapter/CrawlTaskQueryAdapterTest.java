package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskQueryDslRepository;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    @DisplayName("existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn 테스트")
    class ExistsBySchedulerIdAndTaskTypeAndEndpointAndStatusInTests {

        @Test
        @DisplayName("성공 - 스케줄러 ID, 태스크 타입, 엔드포인트 조합으로 존재 확인")
        void shouldCheckExistenceWithEndpoint() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerId.of(1L);
            CrawlTaskType taskType = CrawlTaskType.SEARCH;
            String endpointPath = "/api/search";
            String endpointQueryParams = "{\"keyword\":\"test\"}";
            List<CrawlTaskStatus> statuses =
                    List.of(CrawlTaskStatus.WAITING, CrawlTaskStatus.RUNNING);

            given(
                            queryDslRepository.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                                    1L, taskType, endpointPath, endpointQueryParams, statuses))
                    .willReturn(true);

            // When
            boolean result =
                    queryAdapter.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                            schedulerId, taskType, endpointPath, endpointQueryParams, statuses);

            // Then
            assertThat(result).isTrue();
            verify(queryDslRepository)
                    .existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                            1L, taskType, endpointPath, endpointQueryParams, statuses);
        }

        @Test
        @DisplayName("성공 - null queryParams로 존재 확인")
        void shouldCheckExistenceWithNullQueryParams() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerId.of(1L);
            CrawlTaskType taskType = CrawlTaskType.DETAIL;
            String endpointPath = "/api/detail";
            String endpointQueryParams = null;
            List<CrawlTaskStatus> statuses = List.of(CrawlTaskStatus.WAITING);

            given(
                            queryDslRepository.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                                    1L, taskType, endpointPath, null, statuses))
                    .willReturn(false);

            // When
            boolean result =
                    queryAdapter.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                            schedulerId, taskType, endpointPath, endpointQueryParams, statuses);

            // Then
            assertThat(result).isFalse();
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
                            List.of(CrawlSchedulerId.of(1L)),
                            null,
                            List.of(CrawlTaskStatus.WAITING),
                            null,
                            null,
                            null,
                            0,
                            10);
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
                            List.of(CrawlSchedulerId.of(1L)),
                            null,
                            List.of(CrawlTaskStatus.SUCCESS),
                            null,
                            null,
                            null,
                            0,
                            10);
            given(queryDslRepository.countByCriteria(criteria)).willReturn(15L);

            // When
            long result = queryAdapter.countByCriteria(criteria);

            // Then
            assertThat(result).isEqualTo(15L);
        }
    }

    @Nested
    @DisplayName("countByStatus 테스트")
    class CountByStatusTests {

        @Test
        @DisplayName("성공 - 상태별 CrawlTask 개수 조회")
        void shouldCountByStatus() {
            // Given - 통계 조회 조건
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, null, null);
            Map<CrawlTaskStatus, Long> statusCounts = new EnumMap<>(CrawlTaskStatus.class);
            statusCounts.put(CrawlTaskStatus.WAITING, 3L);
            statusCounts.put(CrawlTaskStatus.RUNNING, 2L);
            statusCounts.put(CrawlTaskStatus.SUCCESS, 10L);

            given(queryDslRepository.countByStatus(criteria)).willReturn(statusCounts);

            // When
            Map<CrawlTaskStatus, Long> result = queryAdapter.countByStatus(criteria);

            // Then - 상태별 개수가 반환되어야 함
            assertThat(result).containsEntry(CrawlTaskStatus.WAITING, 3L);
            assertThat(result).containsEntry(CrawlTaskStatus.RUNNING, 2L);
            assertThat(result).containsEntry(CrawlTaskStatus.SUCCESS, 10L);
        }
    }

    @Nested
    @DisplayName("findLatestBySellerId 테스트")
    class FindLatestBySellerIdTests {

        @Test
        @DisplayName("성공 - 셀러별 최근 태스크 조회")
        void shouldFindLatestBySellerId() {
            // Given - 셀러의 최근 태스크 조회
            SellerId sellerId = SellerId.of(1L);
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
                            CrawlTaskStatus.SUCCESS,
                            0,
                            now,
                            now);
            CrawlTask domain = CrawlTaskFixture.aSuccessTask();

            given(queryDslRepository.findLatestBySellerId(1L)).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            Optional<CrawlTask> result = queryAdapter.findLatestBySellerId(sellerId);

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("성공 - 최근 태스크 없을 때 빈 Optional 반환")
        void shouldReturnEmptyWhenNoLatestTask() {
            // Given
            SellerId sellerId = SellerId.of(999L);
            given(queryDslRepository.findLatestBySellerId(999L)).willReturn(Optional.empty());

            // When
            Optional<CrawlTask> result = queryAdapter.findLatestBySellerId(sellerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findRecentBySellerId 테스트")
    class FindRecentBySellerIdTests {

        @Test
        @DisplayName("성공 - 셀러별 최근 N개 태스크 조회")
        void shouldFindRecentBySellerId() {
            // Given - 셀러의 최근 5개 태스크 조회
            SellerId sellerId = SellerId.of(1L);
            int limit = 5;
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
                            CrawlTaskStatus.SUCCESS,
                            0,
                            now,
                            now);
            CrawlTask domain = CrawlTaskFixture.aSuccessTask();

            given(queryDslRepository.findRecentBySellerId(1L, limit)).willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<CrawlTask> result = queryAdapter.findRecentBySellerId(sellerId, limit);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 최근 태스크 없을 때 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoRecentTasks() {
            // Given
            SellerId sellerId = SellerId.of(999L);
            given(queryDslRepository.findRecentBySellerId(999L, 5)).willReturn(List.of());

            // When
            List<CrawlTask> result = queryAdapter.findRecentBySellerId(sellerId, 5);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findRunningOlderThan 테스트")
    class FindRunningOlderThanTests {

        @Test
        @DisplayName("성공 - RUNNING 상태에서 timeoutSeconds 이상 머물러 있는 태스크 조회")
        void shouldFindRunningOlderThan() {
            // Given - 타임아웃된 RUNNING 태스크
            int limit = 10;
            long timeoutSeconds = 600L;
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
                            CrawlTaskStatus.RUNNING,
                            0,
                            now,
                            now);
            CrawlTask domain = CrawlTaskFixture.aRunningTask();

            given(queryDslRepository.findRunningOlderThan(limit, timeoutSeconds))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<CrawlTask> result = queryAdapter.findRunningOlderThan(limit, timeoutSeconds);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 타임아웃된 태스크 없을 때 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoRunningOlderThan() {
            // Given
            given(queryDslRepository.findRunningOlderThan(10, 600L)).willReturn(List.of());

            // When
            List<CrawlTask> result = queryAdapter.findRunningOlderThan(10, 600L);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
