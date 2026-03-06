package com.ryuqq.crawlinghub.application.schedule.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerHistoryIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerOutBoxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerOutBoxReadManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: QueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerOutBoxReadManager 테스트")
class CrawlSchedulerOutBoxReadManagerTest {

    @Mock private CrawlSchedulerOutBoxQueryPort outBoxQueryPort;

    @InjectMocks private CrawlSchedulerOutBoxReadManager manager;

    @Nested
    @DisplayName("findByHistoryId() 테스트")
    class FindByHistoryId {

        @Test
        @DisplayName("[성공] 히스토리 ID로 아웃박스 조회 성공")
        void shouldFindOutBoxByHistoryId() {
            // Given
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            given(outBoxQueryPort.findByHistoryId(historyId)).willReturn(Optional.of(outBox));

            // When
            Optional<CrawlSchedulerOutBox> result = manager.findByHistoryId(historyId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(outBox);
            then(outBoxQueryPort).should().findByHistoryId(historyId);
        }

        @Test
        @DisplayName("[성공] 히스토리 ID에 해당하는 아웃박스 없으면 빈 Optional 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            given(outBoxQueryPort.findByHistoryId(historyId)).willReturn(Optional.empty());

            // When
            Optional<CrawlSchedulerOutBox> result = manager.findByHistoryId(historyId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatus() 테스트")
    class FindByStatus {

        @Test
        @DisplayName("[성공] 상태별 아웃박스 목록 조회")
        void shouldFindOutBoxesByStatus() {
            // Given
            CrawlSchedulerOubBoxStatus status = CrawlSchedulerOubBoxStatus.PENDING;
            int limit = 10;
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            given(outBoxQueryPort.findByStatus(status, limit)).willReturn(List.of(outBox));

            // When
            List<CrawlSchedulerOutBox> result = manager.findByStatus(status, limit);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(outBox);
        }

        @Test
        @DisplayName("[성공] 해당 상태 아웃박스 없으면 빈 목록 반환")
        void shouldReturnEmptyListWhenNoOutBoxesFound() {
            // Given
            CrawlSchedulerOubBoxStatus status = CrawlSchedulerOubBoxStatus.COMPLETED;
            int limit = 10;
            given(outBoxQueryPort.findByStatus(status, limit)).willReturn(List.of());

            // When
            List<CrawlSchedulerOutBox> result = manager.findByStatus(status, limit);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findPendingOlderThan() 테스트")
    class FindPendingOlderThan {

        @Test
        @DisplayName("[성공] 지연 시간 이상 경과한 PENDING 아웃박스 조회")
        void shouldFindPendingSchedulerOutBoxesOlderThan() {
            // Given
            int limit = 5;
            int delaySeconds = 30;
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            given(outBoxQueryPort.findPendingOlderThan(limit, delaySeconds))
                    .willReturn(List.of(outBox));

            // When
            List<CrawlSchedulerOutBox> result = manager.findPendingOlderThan(limit, delaySeconds);

            // Then
            assertThat(result).hasSize(1);
            then(outBoxQueryPort).should().findPendingOlderThan(limit, delaySeconds);
        }
    }

    @Nested
    @DisplayName("findStaleProcessing() 테스트")
    class FindStaleProcessing {

        @Test
        @DisplayName("[성공] 타임아웃된 PROCESSING 아웃박스 조회")
        void shouldFindStaleProcessingSchedulerOutBoxes() {
            // Given
            int limit = 10;
            long timeoutSeconds = 300L;
            List<CrawlSchedulerOutBox> staleOutBoxes =
                    List.of(CrawlSchedulerOutBoxFixture.aPendingOutBox());
            given(outBoxQueryPort.findStaleProcessing(limit, timeoutSeconds))
                    .willReturn(staleOutBoxes);

            // When
            List<CrawlSchedulerOutBox> result = manager.findStaleProcessing(limit, timeoutSeconds);

            // Then
            assertThat(result).hasSize(1);
            then(outBoxQueryPort).should().findStaleProcessing(limit, timeoutSeconds);
        }

        @Test
        @DisplayName("[성공] 타임아웃된 아웃박스 없으면 빈 목록 반환")
        void shouldReturnEmptyListWhenNoStaleOutBoxes() {
            // Given
            int limit = 10;
            long timeoutSeconds = 300L;
            given(outBoxQueryPort.findStaleProcessing(limit, timeoutSeconds)).willReturn(List.of());

            // When
            List<CrawlSchedulerOutBox> result = manager.findStaleProcessing(limit, timeoutSeconds);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
