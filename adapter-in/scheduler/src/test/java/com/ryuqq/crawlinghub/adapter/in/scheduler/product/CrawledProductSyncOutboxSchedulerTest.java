package com.ryuqq.crawlinghub.adapter.in.scheduler.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.PublishPendingSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverFailedProductSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverTimeoutProductSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.port.in.command.PublishPendingSyncOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.command.RecoverFailedProductSyncOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.command.RecoverTimeoutProductSyncOutboxUseCase;
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
 * CrawledProductSyncOutboxScheduler 단위 테스트
 *
 * <p>스케줄러가 설정값을 올바르게 읽어 UseCase 커맨드를 생성하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductSyncOutboxScheduler 단위 테스트")
class CrawledProductSyncOutboxSchedulerTest {

    @Mock private PublishPendingSyncOutboxUseCase publishPendingUseCase;
    @Mock private RecoverTimeoutProductSyncOutboxUseCase recoverTimeoutUseCase;
    @Mock private RecoverFailedProductSyncOutboxUseCase recoverFailedUseCase;

    private CrawledProductSyncOutboxScheduler sut;

    @BeforeEach
    void setUp() {
        // 스케줄러 프로퍼티를 직접 생성하여 설정
        SchedulerProperties properties = buildSchedulerProperties();
        sut =
                new CrawledProductSyncOutboxScheduler(
                        publishPendingUseCase,
                        recoverTimeoutUseCase,
                        recoverFailedUseCase,
                        properties);
    }

    /**
     * 테스트용 SchedulerProperties 빌드
     *
     * <p>SchedulerProperties 레코드는 중첩 구조이므로 직접 생성합니다.
     */
    private SchedulerProperties buildSchedulerProperties() {
        SchedulerProperties.CrawledProductSyncOutboxPublishPending publishPending =
                new SchedulerProperties.CrawledProductSyncOutboxPublishPending(
                        true, "0 * * * * *", "Asia/Seoul", 100, 3);

        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 50, 300L);

        SchedulerProperties.RecoverFailed recoverFailed =
                new SchedulerProperties.RecoverFailed(true, "0 * * * * *", "Asia/Seoul", 50, 60);

        SchedulerProperties.CrawledProductSyncOutbox syncOutbox =
                new SchedulerProperties.CrawledProductSyncOutbox(
                        publishPending, recoverTimeout, recoverFailed);

        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(
                        buildCrawlSchedulerOutbox(),
                        buildCrawlTaskOutbox(),
                        buildCrawlTask(),
                        buildCrawledRawProcessing(),
                        buildUserAgentHousekeeper(),
                        syncOutbox);

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

    private SchedulerProperties.CrawlTask buildCrawlTask() {
        SchedulerProperties.RecoverStuck recoverStuck =
                new SchedulerProperties.RecoverStuck(true, "0 * * * * *", "Asia/Seoul", 10, 120L);
        return new SchedulerProperties.CrawlTask(recoverStuck);
    }

    private SchedulerProperties.CrawledRawProcessing buildCrawledRawProcessing() {
        SchedulerProperties.ProcessCrawledRaw miniShop =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 50);
        SchedulerProperties.ProcessCrawledRaw detail =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 50);
        SchedulerProperties.ProcessCrawledRaw option =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 50);
        return new SchedulerProperties.CrawledRawProcessing(miniShop, detail, option);
    }

    private SchedulerProperties.UserAgentHousekeeper buildUserAgentHousekeeper() {
        return new SchedulerProperties.UserAgentHousekeeper(true, 5000, 10, 20, 500L, 60000L, 10);
    }

    @Nested
    @DisplayName("publishPendingOutboxes 메서드 테스트")
    class PublishPendingOutboxesTest {

        @Test
        @DisplayName("[성공] 설정값을 읽어 PublishPendingSyncOutboxCommand를 생성하고 UseCase를 호출한다")
        void shouldCallPublishPendingUseCaseWithCorrectCommand() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(10, 10, 0);
            given(publishPendingUseCase.execute(org.mockito.ArgumentMatchers.any()))
                    .willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.publishPendingOutboxes();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<PublishPendingSyncOutboxCommand> captor =
                    forClass(PublishPendingSyncOutboxCommand.class);
            verify(publishPendingUseCase).execute(captor.capture());

            PublishPendingSyncOutboxCommand captured = captor.getValue();
            // 설정값: batchSize=100, maxRetryCount=3
            assertThat(captured.batchSize()).isEqualTo(100);
            assertThat(captured.maxRetryCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("recoverTimeoutOutboxes 메서드 테스트")
    class RecoverTimeoutOutboxesTest {

        @Test
        @DisplayName("[성공] 설정값을 읽어 RecoverTimeoutProductSyncOutboxCommand를 생성하고 UseCase를 호출한다")
        void shouldCallRecoverTimeoutUseCaseWithCorrectCommand() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(5, 5, 0);
            given(recoverTimeoutUseCase.execute(org.mockito.ArgumentMatchers.any()))
                    .willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.recoverTimeoutOutboxes();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<RecoverTimeoutProductSyncOutboxCommand> captor =
                    forClass(RecoverTimeoutProductSyncOutboxCommand.class);
            verify(recoverTimeoutUseCase).execute(captor.capture());

            RecoverTimeoutProductSyncOutboxCommand captured = captor.getValue();
            // 설정값: batchSize=50, timeoutSeconds=300
            assertThat(captured.batchSize()).isEqualTo(50);
            assertThat(captured.timeoutSeconds()).isEqualTo(300L);
        }
    }

    @Nested
    @DisplayName("recoverFailedOutboxes 메서드 테스트")
    class RecoverFailedOutboxesTest {

        @Test
        @DisplayName("[성공] 설정값을 읽어 RecoverFailedProductSyncOutboxCommand를 생성하고 UseCase를 호출한다")
        void shouldCallRecoverFailedUseCaseWithCorrectCommand() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(3, 3, 0);
            given(recoverFailedUseCase.execute(org.mockito.ArgumentMatchers.any()))
                    .willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.recoverFailedOutboxes();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<RecoverFailedProductSyncOutboxCommand> captor =
                    forClass(RecoverFailedProductSyncOutboxCommand.class);
            verify(recoverFailedUseCase).execute(captor.capture());

            RecoverFailedProductSyncOutboxCommand captured = captor.getValue();
            // 설정값: batchSize=50, delaySeconds=60
            assertThat(captured.batchSize()).isEqualTo(50);
            assertThat(captured.delaySeconds()).isEqualTo(60);
        }
    }
}
