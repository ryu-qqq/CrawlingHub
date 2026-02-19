package com.ryuqq.crawlinghub.application.task.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatisticsCriteria;
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
                            CrawlTaskType.META,
                            taskTypeCount,
                            CrawlTaskType.MINI_SHOP,
                            taskTypeCount);
            given(crawlTaskQueryPort.countByTaskType(statisticsCriteria)).willReturn(typeCounts);

            // When
            Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> result =
                    manager.countByTaskType(statisticsCriteria);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsKeys(CrawlTaskType.META, CrawlTaskType.MINI_SHOP);
            verify(crawlTaskQueryPort).countByTaskType(statisticsCriteria);
        }
    }
}
