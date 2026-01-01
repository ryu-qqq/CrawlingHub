package com.ryuqq.crawlinghub.application.task.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.crawlinghub.application.task.manager.command.CrawlTaskTransactionManager;
import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskPersistencePort;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskTransactionManager 테스트")
class CrawlTaskTransactionManagerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.of("UTC"));

    @Mock private CrawlTaskPersistencePort crawlTaskPersistencePort;
    @Mock private CrawlTaskQueryPort crawlTaskQueryPort;

    @InjectMocks private CrawlTaskTransactionManager manager;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlTask 저장 → CrawlTaskId 반환")
        void shouldPersistTaskAndReturnId() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlTaskId expectedId = CrawlTaskIdFixture.anAssignedId();

            given(crawlTaskPersistencePort.persist(task)).willReturn(expectedId);

            // When
            CrawlTaskId result = manager.persist(task);

            // Then
            assertThat(result).isEqualTo(expectedId);
            verify(crawlTaskPersistencePort).persist(task);
        }

        @Test
        @DisplayName("[성공] RUNNING 상태 CrawlTask 저장")
        void shouldPersistRunningTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlTaskId expectedId = CrawlTaskIdFixture.anAssignedId();

            given(crawlTaskPersistencePort.persist(task)).willReturn(expectedId);

            // When
            CrawlTaskId result = manager.persist(task);

            // Then
            assertThat(result).isEqualTo(expectedId);
        }
    }

    @Nested
    @DisplayName("markAsPublished() 테스트")
    class MarkAsPublished {

        @Test
        @DisplayName("[성공] WAITING 상태 → PUBLISHED 상태로 변경")
        void shouldTransitionFromWaitingToPublished() {
            // Given
            CrawlTask waitingTask = CrawlTaskFixture.aWaitingTask();
            CrawlTaskId taskId = waitingTask.getId();

            given(crawlTaskQueryPort.findById(taskId)).willReturn(Optional.of(waitingTask));
            given(crawlTaskPersistencePort.persist(any(CrawlTask.class))).willReturn(taskId);

            // When
            manager.markAsPublished(taskId, FIXED_CLOCK);

            // Then
            ArgumentCaptor<CrawlTask> captor = ArgumentCaptor.forClass(CrawlTask.class);
            verify(crawlTaskPersistencePort).persist(captor.capture());

            CrawlTask savedTask = captor.getValue();
            assertThat(savedTask.getStatus()).isEqualTo(CrawlTaskStatus.PUBLISHED);
        }

        @Test
        @DisplayName("[성공] FAILED 상태 → RETRY → PUBLISHED 상태로 변경")
        void shouldTransitionFromFailedToPublishedViaRetry() {
            // Given
            CrawlTask failedTask = CrawlTaskFixture.aFailedTask();
            CrawlTaskId taskId = failedTask.getId();

            given(crawlTaskQueryPort.findById(taskId)).willReturn(Optional.of(failedTask));
            given(crawlTaskPersistencePort.persist(any(CrawlTask.class))).willReturn(taskId);

            // When
            manager.markAsPublished(taskId, FIXED_CLOCK);

            // Then
            ArgumentCaptor<CrawlTask> captor = ArgumentCaptor.forClass(CrawlTask.class);
            verify(crawlTaskPersistencePort).persist(captor.capture());

            CrawlTask savedTask = captor.getValue();
            assertThat(savedTask.getStatus()).isEqualTo(CrawlTaskStatus.PUBLISHED);
            assertThat(savedTask.getRetryCount().value()).isEqualTo(1);
        }

        @Test
        @DisplayName("[성공] TIMEOUT 상태 → RETRY → PUBLISHED 상태로 변경")
        void shouldTransitionFromTimeoutToPublishedViaRetry() {
            // Given
            CrawlTask timeoutTask = CrawlTaskFixture.aTimeoutTask();
            CrawlTaskId taskId = timeoutTask.getId();

            given(crawlTaskQueryPort.findById(taskId)).willReturn(Optional.of(timeoutTask));
            given(crawlTaskPersistencePort.persist(any(CrawlTask.class))).willReturn(taskId);

            // When
            manager.markAsPublished(taskId, FIXED_CLOCK);

            // Then
            ArgumentCaptor<CrawlTask> captor = ArgumentCaptor.forClass(CrawlTask.class);
            verify(crawlTaskPersistencePort).persist(captor.capture());

            CrawlTask savedTask = captor.getValue();
            assertThat(savedTask.getStatus()).isEqualTo(CrawlTaskStatus.PUBLISHED);
            assertThat(savedTask.getRetryCount().value()).isEqualTo(1);
        }

        @Test
        @DisplayName("[성공] RETRY 상태 → PUBLISHED 상태로 변경")
        void shouldTransitionFromRetryToPublished() {
            // Given
            CrawlTask retryTask = CrawlTaskFixture.aRetryTask();
            CrawlTaskId taskId = retryTask.getId();

            given(crawlTaskQueryPort.findById(taskId)).willReturn(Optional.of(retryTask));
            given(crawlTaskPersistencePort.persist(any(CrawlTask.class))).willReturn(taskId);

            // When
            manager.markAsPublished(taskId, FIXED_CLOCK);

            // Then
            ArgumentCaptor<CrawlTask> captor = ArgumentCaptor.forClass(CrawlTask.class);
            verify(crawlTaskPersistencePort).persist(captor.capture());

            CrawlTask savedTask = captor.getValue();
            assertThat(savedTask.getStatus()).isEqualTo(CrawlTaskStatus.PUBLISHED);
        }

        @Test
        @DisplayName("[실패] 최대 재시도 초과 시 예외 발생")
        void shouldThrowExceptionWhenMaxRetryExceeded() {
            // Given
            CrawlTask maxRetryTask = CrawlTaskFixture.aFailedTaskWithMaxRetry();
            CrawlTaskId taskId = maxRetryTask.getId();

            given(crawlTaskQueryPort.findById(taskId)).willReturn(Optional.of(maxRetryTask));

            // When & Then
            assertThatThrownBy(() -> manager.markAsPublished(taskId, FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재시도 횟수를 초과했습니다");
        }

        @Test
        @DisplayName("[실패] CrawlTask를 찾을 수 없는 경우 예외 발생")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            CrawlTaskId taskId = CrawlTaskIdFixture.anAssignedId();

            given(crawlTaskQueryPort.findById(taskId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> manager.markAsPublished(taskId, FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("CrawlTask를 찾을 수 없습니다");
        }
    }
}
