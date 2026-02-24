package com.ryuqq.crawlinghub.application.task.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskReadManager 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskReadManager 테스트")
class CrawlTaskReadManagerTest {

    @Mock private CrawlTaskQueryPort crawlTaskQueryPort;
    @Mock private CrawlTask crawlTask;
    @Mock private CrawlTaskCriteria criteria;
    @Mock private CrawlTaskStatisticsCriteria statisticsCriteria;
    @Mock private CrawlTaskQueryPort.TaskTypeCount taskTypeCount;

    private CrawlTaskReadManager manager;

    @BeforeEach
    void setUp() {
        manager = new CrawlTaskReadManager(crawlTaskQueryPort);
    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindById {

        @Test
        @DisplayName("[성공] ID로 CrawlTask 조회")
        void shouldDelegateToQueryPort() {
            // Given
            CrawlTaskId crawlTaskId = CrawlTaskId.of(1L);
            given(crawlTaskQueryPort.findById(crawlTaskId)).willReturn(Optional.of(crawlTask));

            // When
            Optional<CrawlTask> result = manager.findById(crawlTaskId);

            // Then
            assertThat(result).isPresent().contains(crawlTask);
            verify(crawlTaskQueryPort).findById(crawlTaskId);
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 경우 empty 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlTaskId crawlTaskId = CrawlTaskId.of(999L);
            given(crawlTaskQueryPort.findById(crawlTaskId)).willReturn(Optional.empty());

            // When
            Optional<CrawlTask> result = manager.findById(crawlTaskId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByScheduleIdAndStatusIn() 테스트")
    class ExistsByScheduleIdAndStatusIn {

        @Test
        @DisplayName("[성공] Schedule ID와 상태로 존재 확인")
        void shouldDelegateToQueryPort() {
            // Given
            CrawlSchedulerId crawlSchedulerId = CrawlSchedulerId.of(1L);
            List<CrawlTaskStatus> statuses =
                    List.of(CrawlTaskStatus.WAITING, CrawlTaskStatus.RUNNING);
            given(crawlTaskQueryPort.existsByScheduleIdAndStatusIn(crawlSchedulerId, statuses))
                    .willReturn(true);

            // When
            boolean result = manager.existsByScheduleIdAndStatusIn(crawlSchedulerId, statuses);

            // Then
            assertThat(result).isTrue();
            verify(crawlTaskQueryPort).existsByScheduleIdAndStatusIn(crawlSchedulerId, statuses);
        }
    }

    @Nested
    @DisplayName("findByCriteria() 테스트")
    class FindByCriteria {

        @Test
        @DisplayName("[성공] 조건으로 CrawlTask 목록 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(crawlTaskQueryPort.findByCriteria(criteria)).willReturn(List.of(crawlTask));

            // When
            List<CrawlTask> result = manager.findByCriteria(criteria);

            // Then
            assertThat(result).hasSize(1).contains(crawlTask);
            verify(crawlTaskQueryPort).findByCriteria(criteria);
        }
    }

    @Nested
    @DisplayName("countByCriteria() 테스트")
    class CountByCriteria {

        @Test
        @DisplayName("[성공] 조건으로 CrawlTask 개수 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(crawlTaskQueryPort.countByCriteria(criteria)).willReturn(50L);

            // When
            long result = manager.countByCriteria(criteria);

            // Then
            assertThat(result).isEqualTo(50L);
            verify(crawlTaskQueryPort).countByCriteria(criteria);
        }
    }

    @Nested
    @DisplayName("countByStatus() 테스트")
    class CountByStatus {

        @Test
        @DisplayName("[성공] 상태별 CrawlTask 개수 조회")
        void shouldDelegateToQueryPort() {
            // Given
            Map<CrawlTaskStatus, Long> statusCounts =
                    Map.of(
                            CrawlTaskStatus.WAITING, 10L,
                            CrawlTaskStatus.RUNNING, 5L,
                            CrawlTaskStatus.SUCCESS, 100L);
            given(crawlTaskQueryPort.countByStatus(statisticsCriteria)).willReturn(statusCounts);

            // When
            Map<CrawlTaskStatus, Long> result = manager.countByStatus(statisticsCriteria);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(CrawlTaskStatus.WAITING)).isEqualTo(10L);
            assertThat(result.get(CrawlTaskStatus.RUNNING)).isEqualTo(5L);
            assertThat(result.get(CrawlTaskStatus.SUCCESS)).isEqualTo(100L);
            verify(crawlTaskQueryPort).countByStatus(statisticsCriteria);
        }
    }

    @Nested
    @DisplayName("countByTaskType() 테스트")
    class CountByTaskType {

        @Test
        @DisplayName("[성공] 태스크 유형별 통계 조회")
        void shouldDelegateToQueryPort() {
            // Given
            Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> typeCounts =
                    Map.of(
                            CrawlTaskType.SEARCH,
                            taskTypeCount,
                            CrawlTaskType.MINI_SHOP,
                            taskTypeCount);
            given(crawlTaskQueryPort.countByTaskType(statisticsCriteria)).willReturn(typeCounts);

            // When
            Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> result =
                    manager.countByTaskType(statisticsCriteria);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsKeys(CrawlTaskType.SEARCH, CrawlTaskType.MINI_SHOP);
            verify(crawlTaskQueryPort).countByTaskType(statisticsCriteria);
        }
    }

    @Nested
    @DisplayName("findLatestBySellerId() 테스트")
    class FindLatestBySellerId {

        @Test
        @DisplayName("[성공] 셀러별 최근 태스크 조회")
        void shouldDelegateToQueryPort() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            given(crawlTaskQueryPort.findLatestBySellerId(sellerId))
                    .willReturn(Optional.of(crawlTask));

            // When
            Optional<CrawlTask> result = manager.findLatestBySellerId(sellerId);

            // Then
            assertThat(result).isPresent().contains(crawlTask);
            verify(crawlTaskQueryPort).findLatestBySellerId(sellerId);
        }

        @Test
        @DisplayName("[성공] 셀러별 최근 태스크 없을 때 empty 반환")
        void shouldReturnEmptyWhenNoTask() {
            // Given
            SellerId sellerId = SellerId.of(999L);
            given(crawlTaskQueryPort.findLatestBySellerId(sellerId)).willReturn(Optional.empty());

            // When
            Optional<CrawlTask> result = manager.findLatestBySellerId(sellerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findRecentBySellerId() 테스트")
    class FindRecentBySellerId {

        @Test
        @DisplayName("[성공] 셀러별 최근 N개 태스크 조회")
        void shouldDelegateToQueryPort() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            int limit = 5;
            given(crawlTaskQueryPort.findRecentBySellerId(sellerId, limit))
                    .willReturn(List.of(crawlTask));

            // When
            List<CrawlTask> result = manager.findRecentBySellerId(sellerId, limit);

            // Then
            assertThat(result).hasSize(1).contains(crawlTask);
            verify(crawlTaskQueryPort).findRecentBySellerId(sellerId, limit);
        }
    }

    @Nested
    @DisplayName("findRunningOlderThan() 테스트")
    class FindRunningOlderThan {

        @Test
        @DisplayName("[성공] RUNNING 상태 고아 태스크 조회")
        void shouldDelegateToQueryPort() {
            // Given
            int limit = 10;
            long timeoutSeconds = 300L;
            given(crawlTaskQueryPort.findRunningOlderThan(limit, timeoutSeconds))
                    .willReturn(List.of(crawlTask));

            // When
            List<CrawlTask> result = manager.findRunningOlderThan(limit, timeoutSeconds);

            // Then
            assertThat(result).hasSize(1).contains(crawlTask);
            verify(crawlTaskQueryPort).findRunningOlderThan(limit, timeoutSeconds);
        }

        @Test
        @DisplayName("[성공] 고아 태스크 없을 때 빈 목록 반환")
        void shouldReturnEmptyWhenNoOrphanTasks() {
            // Given
            given(crawlTaskQueryPort.findRunningOlderThan(10, 300L)).willReturn(List.of());

            // When
            List<CrawlTask> result = manager.findRunningOlderThan(10, 300L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn() 테스트")
    class ExistsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn {

        @Test
        @DisplayName("[성공] 스케줄러 ID, 태스크 유형, 엔드포인트, 상태로 존재 확인")
        void shouldDelegateToQueryPort() {
            // Given
            CrawlSchedulerId crawlSchedulerId = CrawlSchedulerId.of(1L);
            CrawlTaskType taskType = CrawlTaskType.SEARCH;
            String endpointPath = "/api/search";
            String endpointQueryParams = "{\"keyword\":\"test\"}";
            List<CrawlTaskStatus> statuses =
                    List.of(CrawlTaskStatus.WAITING, CrawlTaskStatus.PUBLISHED);

            given(
                            crawlTaskQueryPort.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                                    crawlSchedulerId,
                                    taskType,
                                    endpointPath,
                                    endpointQueryParams,
                                    statuses))
                    .willReturn(true);

            // When
            boolean result =
                    manager.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                            crawlSchedulerId,
                            taskType,
                            endpointPath,
                            endpointQueryParams,
                            statuses);

            // Then
            assertThat(result).isTrue();
            verify(crawlTaskQueryPort)
                    .existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                            crawlSchedulerId,
                            taskType,
                            endpointPath,
                            endpointQueryParams,
                            statuses);
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 경우 false 반환")
        void shouldReturnFalseWhenNotExists() {
            // Given
            CrawlSchedulerId crawlSchedulerId = CrawlSchedulerId.of(2L);
            CrawlTaskType taskType = CrawlTaskType.DETAIL;
            String endpointPath = "/api/detail/12345";
            String endpointQueryParams = "{}";
            List<CrawlTaskStatus> statuses = List.of(CrawlTaskStatus.RUNNING);

            given(
                            crawlTaskQueryPort.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                                    crawlSchedulerId,
                                    taskType,
                                    endpointPath,
                                    endpointQueryParams,
                                    statuses))
                    .willReturn(false);

            // When
            boolean result =
                    manager.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                            crawlSchedulerId,
                            taskType,
                            endpointPath,
                            endpointQueryParams,
                            statuses);

            // Then
            assertThat(result).isFalse();
        }
    }
}
