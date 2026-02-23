package com.ryuqq.crawlinghub.application.task.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverStuckCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RecoverStuckCrawlTaskService 단위 테스트
 *
 * <p>RUNNING 고아 CrawlTask 복구 서비스 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverStuckCrawlTaskService 테스트")
class RecoverStuckCrawlTaskServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");

    @Mock private CrawlTaskReadManager readManager;
    @Mock private CrawlTaskCommandManager commandManager;
    @Mock private TimeProvider timeProvider;

    @InjectMocks private RecoverStuckCrawlTaskService sut;

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 고아 Task가 없으면 empty 결과 반환")
        void shouldReturnEmptyWhenNoStuckTasks() {
            // Given
            RecoverStuckCrawlTaskCommand command = new RecoverStuckCrawlTaskCommand(10, 1800L);
            given(readManager.findRunningOlderThan(10, 1800L)).willReturn(List.of());

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
        }

        @Test
        @DisplayName("[성공] 재시도 가능한 고아 Task를 FAILED 후 RETRY 상태로 전환")
        void shouldRecoverRetryableStuckTask() {
            // Given
            RecoverStuckCrawlTaskCommand command = new RecoverStuckCrawlTaskCommand(10, 1800L);
            CrawlTask stuckTask = CrawlTaskFixture.aRunningTask();

            given(readManager.findRunningOlderThan(10, 1800L)).willReturn(List.of(stuckTask));
            given(timeProvider.now()).willReturn(FIXED_INSTANT);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            then(commandManager).should().persist(stuckTask);
        }

        @Test
        @DisplayName("[성공] 복수의 고아 Task를 모두 처리")
        void shouldProcessMultipleStuckTasks() {
            // Given
            RecoverStuckCrawlTaskCommand command = new RecoverStuckCrawlTaskCommand(10, 1800L);
            CrawlTask task1 = CrawlTaskFixture.aRunningTask();
            CrawlTask task2 = CrawlTaskFixture.aRunningTask();

            given(readManager.findRunningOlderThan(10, 1800L)).willReturn(List.of(task1, task2));
            given(timeProvider.now()).willReturn(FIXED_INSTANT);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(2);
        }

        @Test
        @DisplayName("[부분 실패] persist 예외 발생 시 실패 카운트 증가")
        void shouldCountFailedTasksWhenPersistThrows() {
            // Given
            // RUNNING 상태 태스크 2개를 사용 (markAsFailed()는 RUNNING 상태에서만 동작)
            RecoverStuckCrawlTaskCommand command = new RecoverStuckCrawlTaskCommand(10, 1800L);
            CrawlTask task1 = CrawlTaskFixture.aRunningTask();
            CrawlTask task2 = CrawlTaskFixture.aRunningTask();

            given(readManager.findRunningOlderThan(10, 1800L)).willReturn(List.of(task1, task2));
            given(timeProvider.now()).willReturn(FIXED_INSTANT);
            // task1 persist 성공, task2 persist 예외
            given(commandManager.persist(task1))
                    .willReturn(com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId.of(1L));
            willThrow(new RuntimeException("DB 오류")).given(commandManager).persist(task2);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
