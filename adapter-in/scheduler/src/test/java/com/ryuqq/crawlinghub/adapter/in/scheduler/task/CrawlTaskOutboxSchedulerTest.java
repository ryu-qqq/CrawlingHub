package com.ryuqq.crawlinghub.adapter.in.scheduler.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.ProcessPendingCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverFailedCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverTimeoutCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.port.in.command.ProcessPendingCrawlTaskOutboxUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverFailedCrawlTaskOutboxUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverTimeoutCrawlTaskOutboxUseCase;
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
 * CrawlTaskOutboxScheduler 단위 테스트
 *
 * <p>스케줄러가 설정값을 올바르게 읽어 각 UseCase에 올바른 커맨드를 전달하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskOutboxScheduler 단위 테스트")
class CrawlTaskOutboxSchedulerTest {

    @Mock private ProcessPendingCrawlTaskOutboxUseCase processPendingUseCase;
    @Mock private RecoverTimeoutCrawlTaskOutboxUseCase recoverTimeoutUseCase;
    @Mock private RecoverFailedCrawlTaskOutboxUseCase recoverFailedUseCase;

    private CrawlTaskOutboxScheduler sut;

    @BeforeEach
    void setUp() {
        SchedulerProperties properties = buildSchedulerProperties();
        sut =
                new CrawlTaskOutboxScheduler(
                        processPendingUseCase,
                        recoverTimeoutUseCase,
                        recoverFailedUseCase,
                        properties);
    }

    private SchedulerProperties buildSchedulerProperties() {
        // CrawlTaskOutbox 설정값을 명확한 숫자로 지정
        SchedulerProperties.ProcessPending processPending =
                new SchedulerProperties.ProcessPending(true, "0 * * * * *", "Asia/Seoul", 25, 15);
        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 10, 240L);
        SchedulerProperties.RecoverFailed recoverFailed =
                new SchedulerProperties.RecoverFailed(true, "0 * * * * *", "Asia/Seoul", 12, 90);

        SchedulerProperties.CrawlTaskOutbox crawlTaskOutbox =
                new SchedulerProperties.CrawlTaskOutbox(
                        processPending, recoverTimeout, recoverFailed);

        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(
                        buildCrawlSchedulerOutbox(),
                        crawlTaskOutbox,
                        buildCrawlTask(),
                        buildCrawledRawProcessing(),
                        buildUserAgentHousekeeper(),
                        buildSyncOutbox(),
                        buildProductRefresh());

        return new SchedulerProperties(jobs);
    }

    private SchedulerProperties.ProductRefresh buildProductRefresh() {
        SchedulerProperties.RefreshStale refreshStale =
                new SchedulerProperties.RefreshStale(
                        true, "0 0 10,14,18 * * *", "Asia/Seoul", 3000);
        return new SchedulerProperties.ProductRefresh(refreshStale);
    }

    private SchedulerProperties.CrawlSchedulerOutbox buildCrawlSchedulerOutbox() {
        SchedulerProperties.ProcessPending processPending =
                new SchedulerProperties.ProcessPending(true, "0 * * * * *", "Asia/Seoul", 10, 5);
        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 10, 60L);
        return new SchedulerProperties.CrawlSchedulerOutbox(processPending, recoverTimeout);
    }

    private SchedulerProperties.CrawlTask buildCrawlTask() {
        SchedulerProperties.RecoverStuck recoverStuck =
                new SchedulerProperties.RecoverStuck(true, "0 * * * * *", "Asia/Seoul", 10, 120L);
        return new SchedulerProperties.CrawlTask(recoverStuck);
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
    @DisplayName("processPendingOutboxes 메서드 테스트")
    class ProcessPendingOutboxesTest {

        @Test
        @DisplayName("[성공] 설정값을 읽어 ProcessPendingCrawlTaskOutboxCommand를 생성하고 UseCase를 호출한다")
        void shouldCallProcessPendingUseCaseWithCorrectCommand() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(25, 25, 0);
            given(processPendingUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.processPendingOutboxes();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<ProcessPendingCrawlTaskOutboxCommand> captor =
                    forClass(ProcessPendingCrawlTaskOutboxCommand.class);
            verify(processPendingUseCase).execute(captor.capture());

            ProcessPendingCrawlTaskOutboxCommand captured = captor.getValue();
            // 설정값: batchSize=25, delaySeconds=15
            assertThat(captured.batchSize()).isEqualTo(25);
            assertThat(captured.delaySeconds()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("recoverTimeoutOutboxes 메서드 테스트")
    class RecoverTimeoutOutboxesTest {

        @Test
        @DisplayName("[성공] 설정값을 읽어 RecoverTimeoutCrawlTaskOutboxCommand를 생성하고 UseCase를 호출한다")
        void shouldCallRecoverTimeoutUseCaseWithCorrectCommand() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(10, 10, 0);
            given(recoverTimeoutUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.recoverTimeoutOutboxes();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<RecoverTimeoutCrawlTaskOutboxCommand> captor =
                    forClass(RecoverTimeoutCrawlTaskOutboxCommand.class);
            verify(recoverTimeoutUseCase).execute(captor.capture());

            RecoverTimeoutCrawlTaskOutboxCommand captured = captor.getValue();
            // 설정값: batchSize=10, timeoutSeconds=240
            assertThat(captured.batchSize()).isEqualTo(10);
            assertThat(captured.timeoutSeconds()).isEqualTo(240L);
        }
    }

    @Nested
    @DisplayName("recoverFailedOutboxes 메서드 테스트")
    class RecoverFailedOutboxesTest {

        @Test
        @DisplayName("[성공] 설정값을 읽어 RecoverFailedCrawlTaskOutboxCommand를 생성하고 UseCase를 호출한다")
        void shouldCallRecoverFailedUseCaseWithCorrectCommand() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(12, 11, 1);
            given(recoverFailedUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.recoverFailedOutboxes();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<RecoverFailedCrawlTaskOutboxCommand> captor =
                    forClass(RecoverFailedCrawlTaskOutboxCommand.class);
            verify(recoverFailedUseCase).execute(captor.capture());

            RecoverFailedCrawlTaskOutboxCommand captured = captor.getValue();
            // 설정값: batchSize=12, delaySeconds=90
            assertThat(captured.batchSize()).isEqualTo(12);
            assertThat(captured.delaySeconds()).isEqualTo(90);
        }
    }
}
