package com.ryuqq.crawlinghub.adapter.in.scheduler.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverStuckCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverStuckCrawlTaskUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskScheduler 단위 테스트
 *
 * <p>스케줄러가 설정값을 올바르게 읽어 RecoverStuckCrawlTaskCommand를 생성하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskScheduler 단위 테스트")
class CrawlTaskSchedulerTest {

    @Mock private RecoverStuckCrawlTaskUseCase recoverStuckUseCase;

    private CrawlTaskScheduler sut;

    @BeforeEach
    void setUp() {
        SchedulerProperties properties = buildSchedulerProperties();
        sut = new CrawlTaskScheduler(recoverStuckUseCase, properties);
    }

    private SchedulerProperties buildSchedulerProperties() {
        // CrawlTask.recoverStuck 설정값: batchSize=30, timeoutSeconds=600
        SchedulerProperties.RecoverStuck recoverStuck =
                new SchedulerProperties.RecoverStuck(true, "0 * * * * *", "Asia/Seoul", 30, 600L);
        SchedulerProperties.CrawlTask crawlTask = new SchedulerProperties.CrawlTask(recoverStuck);

        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(
                        buildCrawlSchedulerOutbox(),
                        buildCrawlTaskOutbox(),
                        crawlTask,
                        buildCrawledRawProcessing(),
                        buildUserAgentHousekeeper(),
                        buildSyncOutbox());

        return new SchedulerProperties(jobs);
    }

    private SchedulerProperties.CrawlSchedulerOutbox buildCrawlSchedulerOutbox() {
        SchedulerProperties.ProcessPending processPending =
                new SchedulerProperties.ProcessPending(true, "0 * * * * *", "Asia/Seoul", 10, 5);
        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 10, 60L);
        return new SchedulerProperties.CrawlSchedulerOutbox(processPending, recoverTimeout);
    }

    private SchedulerProperties.CrawlTaskOutbox buildCrawlTaskOutbox() {
        SchedulerProperties.ProcessPending processPending =
                new SchedulerProperties.ProcessPending(true, "0 * * * * *", "Asia/Seoul", 10, 5);
        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 10, 60L);
        SchedulerProperties.RecoverFailed recoverFailed =
                new SchedulerProperties.RecoverFailed(true, "0 * * * * *", "Asia/Seoul", 10, 30);
        return new SchedulerProperties.CrawlTaskOutbox(
                processPending, recoverTimeout, recoverFailed);
    }

    private SchedulerProperties.CrawledRawProcessing buildCrawledRawProcessing() {
        SchedulerProperties.ProcessCrawledRaw miniShop =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 50);
        SchedulerProperties.ProcessCrawledRaw detail =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 30);
        SchedulerProperties.ProcessCrawledRaw option =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 20);
        return new SchedulerProperties.CrawledRawProcessing(miniShop, detail, option);
    }

    private SchedulerProperties.UserAgentHousekeeper buildUserAgentHousekeeper() {
        return new SchedulerProperties.UserAgentHousekeeper(true, 5000, 10, 20, 500L, 60000L, 10);
    }

    private SchedulerProperties.CrawledProductSyncOutbox buildSyncOutbox() {
        SchedulerProperties.CrawledProductSyncOutboxPublishPending publishPending =
                new SchedulerProperties.CrawledProductSyncOutboxPublishPending(
                        true, "0 * * * * *", "Asia/Seoul", 100, 3);
        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 50, 300L);
        SchedulerProperties.RecoverFailed recoverFailed =
                new SchedulerProperties.RecoverFailed(true, "0 * * * * *", "Asia/Seoul", 50, 60);
        return new SchedulerProperties.CrawledProductSyncOutbox(
                publishPending, recoverTimeout, recoverFailed);
    }

    @Nested
    @DisplayName("recoverStuckTasks 메서드 테스트")
    class RecoverStuckTasksTest {

        @Test
        @DisplayName("[성공] 설정값을 읽어 RecoverStuckCrawlTaskCommand를 생성하고 UseCase를 호출한다")
        void shouldCallRecoverStuckUseCaseWithCorrectCommand() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(5, 5, 0);
            given(recoverStuckUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.recoverStuckTasks();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<RecoverStuckCrawlTaskCommand> captor =
                    forClass(RecoverStuckCrawlTaskCommand.class);
            verify(recoverStuckUseCase).execute(captor.capture());

            RecoverStuckCrawlTaskCommand captured = captor.getValue();
            // 설정값: batchSize=30, timeoutSeconds=600
            assertThat(captured.batchSize()).isEqualTo(30);
            assertThat(captured.timeoutSeconds()).isEqualTo(600L);
        }

        @Test
        @DisplayName("[성공] UseCase가 빈 결과를 반환해도 정상 처리한다")
        void shouldHandleEmptyResult() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.empty();
            given(recoverStuckUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.recoverStuckTasks();

            // Then
            assertThat(result.total()).isEqualTo(0);
            assertThat(result.success()).isEqualTo(0);
            assertThat(result.failed()).isEqualTo(0);
        }
    }
}
