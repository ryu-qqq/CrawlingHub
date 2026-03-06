package com.ryuqq.crawlinghub.adapter.in.scheduler.product;

import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.product.port.in.command.RefreshStaleCrawledProductsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductRefreshScheduler 단위 테스트
 *
 * <p>스케줄러가 설정된 batchSize로 UseCase를 호출하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductRefreshScheduler 단위 테스트")
class CrawledProductRefreshSchedulerTest {

    @Mock private RefreshStaleCrawledProductsUseCase useCase;

    private CrawledProductRefreshScheduler sut;

    @BeforeEach
    void setUp() {
        SchedulerProperties properties = buildSchedulerProperties();
        sut = new CrawledProductRefreshScheduler(useCase, properties);
    }

    private SchedulerProperties buildSchedulerProperties() {
        SchedulerProperties.RefreshStale refreshStale =
                new SchedulerProperties.RefreshStale(
                        true, "0 0 10,14,18 * * *", "Asia/Seoul", 3000);
        SchedulerProperties.ProductRefresh productRefresh =
                new SchedulerProperties.ProductRefresh(refreshStale);

        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(
                        buildCrawlSchedulerOutbox(),
                        buildCrawlTaskOutbox(),
                        buildCrawlTask(),
                        buildCrawledRawProcessing(),
                        buildUserAgentHousekeeper(),
                        buildSyncOutbox(),
                        productRefresh);

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
    @DisplayName("refreshStale 메서드 테스트")
    class RefreshStaleTest {

        @Test
        @DisplayName("[성공] 설정된 batchSize(3000)로 UseCase를 호출한다")
        void shouldCallUseCaseWithConfiguredBatchSize() {
            // When
            sut.refreshStale();

            // Then
            verify(useCase).execute(3000);
        }
    }
}
