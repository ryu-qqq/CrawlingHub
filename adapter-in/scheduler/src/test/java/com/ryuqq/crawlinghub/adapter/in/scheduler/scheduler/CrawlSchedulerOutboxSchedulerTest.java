package com.ryuqq.crawlinghub.adapter.in.scheduler.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.schedule.dto.command.ProcessPendingSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RecoverTimeoutSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.ProcessPendingSchedulerOutboxUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RecoverTimeoutSchedulerOutboxUseCase;
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
 * CrawlSchedulerOutboxScheduler 단위 테스트
 *
 * <p>스케줄러가 설정값을 올바르게 읽어 UseCase 커맨드를 생성하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerOutboxScheduler 단위 테스트")
class CrawlSchedulerOutboxSchedulerTest {

    @Mock private ProcessPendingSchedulerOutboxUseCase processPendingOutboxUseCase;
    @Mock private RecoverTimeoutSchedulerOutboxUseCase recoverTimeoutOutboxUseCase;

    private CrawlSchedulerOutboxScheduler sut;

    @BeforeEach
    void setUp() {
        SchedulerProperties properties = buildSchedulerProperties();
        sut =
                new CrawlSchedulerOutboxScheduler(
                        processPendingOutboxUseCase, recoverTimeoutOutboxUseCase, properties);
    }

    private SchedulerProperties buildSchedulerProperties() {
        // CrawlSchedulerOutbox 설정: batchSize=20, delaySeconds=10, timeoutSeconds=180
        SchedulerProperties.ProcessPending processPending =
                new SchedulerProperties.ProcessPending(true, "0 * * * * *", "Asia/Seoul", 20, 10);
        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 15, 180L);

        SchedulerProperties.CrawlSchedulerOutbox crawlSchedulerOutbox =
                new SchedulerProperties.CrawlSchedulerOutbox(processPending, recoverTimeout);

        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(
                        crawlSchedulerOutbox,
                        buildCrawlTaskOutbox(),
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
        @DisplayName("[성공] 설정값을 읽어 ProcessPendingSchedulerOutboxCommand를 생성하고 UseCase를 호출한다")
        void shouldCallProcessPendingUseCaseWithCorrectCommand() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(20, 20, 0);
            given(processPendingOutboxUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.processPendingOutboxes();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<ProcessPendingSchedulerOutboxCommand> captor =
                    forClass(ProcessPendingSchedulerOutboxCommand.class);
            verify(processPendingOutboxUseCase).execute(captor.capture());

            ProcessPendingSchedulerOutboxCommand captured = captor.getValue();
            // 설정값: batchSize=20, delaySeconds=10
            assertThat(captured.batchSize()).isEqualTo(20);
            assertThat(captured.delaySeconds()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("recoverTimeoutOutboxes 메서드 테스트")
    class RecoverTimeoutOutboxesTest {

        @Test
        @DisplayName("[성공] 설정값을 읽어 RecoverTimeoutSchedulerOutboxCommand를 생성하고 UseCase를 호출한다")
        void shouldCallRecoverTimeoutUseCaseWithCorrectCommand() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(5, 5, 0);
            given(recoverTimeoutOutboxUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.recoverTimeoutOutboxes();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<RecoverTimeoutSchedulerOutboxCommand> captor =
                    forClass(RecoverTimeoutSchedulerOutboxCommand.class);
            verify(recoverTimeoutOutboxUseCase).execute(captor.capture());

            RecoverTimeoutSchedulerOutboxCommand captured = captor.getValue();
            // 설정값: batchSize=15, timeoutSeconds=180
            assertThat(captured.batchSize()).isEqualTo(15);
            assertThat(captured.timeoutSeconds()).isEqualTo(180L);
        }
    }
}
