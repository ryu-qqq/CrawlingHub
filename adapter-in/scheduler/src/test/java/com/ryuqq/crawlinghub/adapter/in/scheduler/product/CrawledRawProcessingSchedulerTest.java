package com.ryuqq.crawlinghub.adapter.in.scheduler.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.ProcessPendingCrawledRawCommand;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessPendingCrawledRawUseCase;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
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
 * CrawledRawProcessingScheduler 단위 테스트
 *
 * <p>스케줄러가 각 CrawlType(MINI_SHOP, DETAIL, OPTION)에 맞는 커맨드를 생성하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledRawProcessingScheduler 단위 테스트")
class CrawledRawProcessingSchedulerTest {

    @Mock private ProcessPendingCrawledRawUseCase processPendingCrawledRawUseCase;

    private CrawledRawProcessingScheduler sut;

    @BeforeEach
    void setUp() {
        SchedulerProperties properties = buildSchedulerProperties();
        sut = new CrawledRawProcessingScheduler(processPendingCrawledRawUseCase, properties);
    }

    private SchedulerProperties buildSchedulerProperties() {
        // 각 CrawlType별 배치 사이즈를 다르게 설정하여 구분 가능하게 함
        SchedulerProperties.ProcessCrawledRaw miniShop =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 50);
        SchedulerProperties.ProcessCrawledRaw detail =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 30);
        SchedulerProperties.ProcessCrawledRaw option =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 20);

        SchedulerProperties.CrawledRawProcessing crawledRawProcessing =
                new SchedulerProperties.CrawledRawProcessing(miniShop, detail, option);

        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(
                        buildCrawlSchedulerOutbox(),
                        buildCrawlTaskOutbox(),
                        buildCrawlTask(),
                        crawledRawProcessing,
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
    @DisplayName("processMiniShop 메서드 테스트")
    class ProcessMiniShopTest {

        @Test
        @DisplayName("[성공] MINI_SHOP 타입과 설정된 배치 크기로 UseCase를 호출한다")
        void shouldCallUseCaseWithMiniShopCrawlType() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(50, 50, 0);
            given(processPendingCrawledRawUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.processMiniShop();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<ProcessPendingCrawledRawCommand> captor =
                    forClass(ProcessPendingCrawledRawCommand.class);
            verify(processPendingCrawledRawUseCase).execute(captor.capture());

            ProcessPendingCrawledRawCommand captured = captor.getValue();
            assertThat(captured.crawlType()).isEqualTo(CrawlType.MINI_SHOP);
            assertThat(captured.batchSize()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("processDetail 메서드 테스트")
    class ProcessDetailTest {

        @Test
        @DisplayName("[성공] DETAIL 타입과 설정된 배치 크기로 UseCase를 호출한다")
        void shouldCallUseCaseWithDetailCrawlType() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(30, 30, 0);
            given(processPendingCrawledRawUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.processDetail();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<ProcessPendingCrawledRawCommand> captor =
                    forClass(ProcessPendingCrawledRawCommand.class);
            verify(processPendingCrawledRawUseCase).execute(captor.capture());

            ProcessPendingCrawledRawCommand captured = captor.getValue();
            assertThat(captured.crawlType()).isEqualTo(CrawlType.DETAIL);
            assertThat(captured.batchSize()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("processOption 메서드 테스트")
    class ProcessOptionTest {

        @Test
        @DisplayName("[성공] OPTION 타입과 설정된 배치 크기로 UseCase를 호출한다")
        void shouldCallUseCaseWithOptionCrawlType() {
            // Given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(20, 18, 2);
            given(processPendingCrawledRawUseCase.execute(any())).willReturn(expected);

            // When
            SchedulerBatchProcessingResult result = sut.processOption();

            // Then
            assertThat(result).isEqualTo(expected);

            ArgumentCaptor<ProcessPendingCrawledRawCommand> captor =
                    forClass(ProcessPendingCrawledRawCommand.class);
            verify(processPendingCrawledRawUseCase).execute(captor.capture());

            ProcessPendingCrawledRawCommand captured = captor.getValue();
            assertThat(captured.crawlType()).isEqualTo(CrawlType.OPTION);
            assertThat(captured.batchSize()).isEqualTo(20);
        }
    }
}
