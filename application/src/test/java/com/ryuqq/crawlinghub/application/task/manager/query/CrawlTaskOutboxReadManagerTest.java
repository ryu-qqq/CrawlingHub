package com.ryuqq.crawlinghub.application.task.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxReadManager;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskOutboxCriteria;
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
 * CrawlTaskOutboxReadManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: QueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskOutboxReadManager 테스트")
class CrawlTaskOutboxReadManagerTest {

    @Mock private CrawlTaskOutboxQueryPort outboxQueryPort;

    @InjectMocks private CrawlTaskOutboxReadManager manager;

    @Nested
    @DisplayName("findByCrawlTaskId() 테스트")
    class FindByCrawlTaskId {

        @Test
        @DisplayName("[성공] CrawlTask ID로 아웃박스 단건 조회 성공")
        void shouldFindOutboxByTaskId() {
            // Given
            CrawlTaskId taskId = CrawlTaskIdFixture.anAssignedId();
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();
            given(outboxQueryPort.findByCrawlTaskId(taskId)).willReturn(Optional.of(outbox));

            // When
            Optional<CrawlTaskOutbox> result = manager.findByCrawlTaskId(taskId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(outbox);
            then(outboxQueryPort).should().findByCrawlTaskId(taskId);
        }

        @Test
        @DisplayName("[성공] 해당 CrawlTask ID 아웃박스 없으면 빈 Optional 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlTaskId taskId = CrawlTaskIdFixture.anAssignedId();
            given(outboxQueryPort.findByCrawlTaskId(taskId)).willReturn(Optional.empty());

            // When
            Optional<CrawlTaskOutbox> result = manager.findByCrawlTaskId(taskId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCriteria() 테스트")
    class FindByCriteria {

        @Test
        @DisplayName("[성공] Criteria로 아웃박스 목록 조회")
        void shouldFindOutboxesByCriteria() {
            // Given
            CrawlTaskOutboxCriteria criteria =
                    org.mockito.Mockito.mock(CrawlTaskOutboxCriteria.class);
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();
            given(outboxQueryPort.findByCriteria(criteria)).willReturn(List.of(outbox));

            // When
            List<CrawlTaskOutbox> result = manager.findByCriteria(criteria);

            // Then
            assertThat(result).hasSize(1);
            then(outboxQueryPort).should().findByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 조건에 맞는 아웃박스 없으면 빈 목록 반환")
        void shouldReturnEmptyListWhenNoCriteriaMatches() {
            // Given
            CrawlTaskOutboxCriteria criteria =
                    org.mockito.Mockito.mock(CrawlTaskOutboxCriteria.class);
            given(outboxQueryPort.findByCriteria(criteria)).willReturn(List.of());

            // When
            List<CrawlTaskOutbox> result = manager.findByCriteria(criteria);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByCriteria() 테스트")
    class CountByCriteria {

        @Test
        @DisplayName("[성공] Criteria로 아웃박스 개수 조회")
        void shouldCountOutboxesByCriteria() {
            // Given
            CrawlTaskOutboxCriteria criteria =
                    org.mockito.Mockito.mock(CrawlTaskOutboxCriteria.class);
            given(outboxQueryPort.countByCriteria(criteria)).willReturn(5L);

            // When
            long result = manager.countByCriteria(criteria);

            // Then
            assertThat(result).isEqualTo(5L);
            then(outboxQueryPort).should().countByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 조건에 맞는 아웃박스 없으면 0 반환")
        void shouldReturnZeroWhenNoCriteriaMatches() {
            // Given
            CrawlTaskOutboxCriteria criteria =
                    org.mockito.Mockito.mock(CrawlTaskOutboxCriteria.class);
            given(outboxQueryPort.countByCriteria(criteria)).willReturn(0L);

            // When
            long result = manager.countByCriteria(criteria);

            // Then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("findPendingOlderThan() 테스트")
    class FindPendingOlderThan {

        @Test
        @DisplayName("[성공] 지연 시간 이상 경과한 PENDING 아웃박스 조회")
        void shouldFindPendingOutboxesOlderThan() {
            // Given
            int limit = 10;
            int delaySeconds = 30;
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();
            given(outboxQueryPort.findPendingOlderThan(limit, delaySeconds))
                    .willReturn(List.of(outbox));

            // When
            List<CrawlTaskOutbox> result = manager.findPendingOlderThan(limit, delaySeconds);

            // Then
            assertThat(result).hasSize(1);
            then(outboxQueryPort).should().findPendingOlderThan(limit, delaySeconds);
        }
    }

    @Nested
    @DisplayName("findStaleProcessing() 테스트")
    class FindStaleProcessing {

        @Test
        @DisplayName("[성공] 타임아웃된 PROCESSING 아웃박스 조회")
        void shouldFindStaleProcessingOutboxes() {
            // Given
            int limit = 10;
            long timeoutSeconds = 300L;
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();
            given(outboxQueryPort.findStaleProcessing(limit, timeoutSeconds))
                    .willReturn(List.of(outbox));

            // When
            List<CrawlTaskOutbox> result = manager.findStaleProcessing(limit, timeoutSeconds);

            // Then
            assertThat(result).hasSize(1);
            then(outboxQueryPort).should().findStaleProcessing(limit, timeoutSeconds);
        }
    }

    @Nested
    @DisplayName("findFailedOlderThan() 테스트")
    class FindFailedOlderThan {

        @Test
        @DisplayName("[성공] FAILED 후 지연 시간 이상 경과한 아웃박스 조회")
        void shouldFindFailedOutboxesOlderThan() {
            // Given
            int limit = 10;
            int delaySeconds = 60;
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aFailedOutbox();
            given(outboxQueryPort.findFailedOlderThan(limit, delaySeconds))
                    .willReturn(List.of(outbox));

            // When
            List<CrawlTaskOutbox> result = manager.findFailedOlderThan(limit, delaySeconds);

            // Then
            assertThat(result).hasSize(1);
            then(outboxQueryPort).should().findFailedOlderThan(limit, delaySeconds);
        }

        @Test
        @DisplayName("[성공] 재시도 대상 없으면 빈 목록 반환")
        void shouldReturnEmptyListWhenNoFailedOutboxes() {
            // Given
            int limit = 10;
            int delaySeconds = 60;
            given(outboxQueryPort.findFailedOlderThan(limit, delaySeconds)).willReturn(List.of());

            // When
            List<CrawlTaskOutbox> result = manager.findFailedOlderThan(limit, delaySeconds);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
