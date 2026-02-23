package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.ProcessPendingCrawledRawCommand;
import com.ryuqq.crawlinghub.application.product.internal.processor.CrawledRawProcessor;
import com.ryuqq.crawlinghub.application.product.internal.processor.CrawledRawProcessorProvider;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawReadManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawTransactionManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * ProcessPendingCrawledRawService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProcessPendingCrawledRawService 테스트")
class ProcessPendingCrawledRawServiceTest {

    @Mock private CrawledRawReadManager crawledRawReadManager;
    @Mock private CrawledRawTransactionManager crawledRawTransactionManager;
    @Mock private CrawledRawProcessorProvider crawledRawProcessorProvider;
    @Mock private CrawledRawProcessor mockProcessor;

    private ProcessPendingCrawledRawService service;

    @BeforeEach
    void setUp() {
        service =
                new ProcessPendingCrawledRawService(
                        crawledRawReadManager,
                        crawledRawTransactionManager,
                        crawledRawProcessorProvider);
    }

    @Nested
    @DisplayName("MINI_SHOP 타입 처리")
    class MiniShopProcessing {

        @Test
        @DisplayName("[성공] MINI_SHOP Raw 가공 성공")
        void shouldProcessMiniShopSuccessfully() {
            // Given
            CrawledRaw raw =
                    CrawledRaw.forNew(
                            1L,
                            100L,
                            10001L,
                            CrawlType.MINI_SHOP,
                            "{\"json\":true}",
                            Instant.now());
            given(crawledRawReadManager.findPendingByType(CrawlType.MINI_SHOP, 100))
                    .willReturn(List.of(raw));
            given(crawledRawProcessorProvider.getProcessor(CrawlType.MINI_SHOP))
                    .willReturn(mockProcessor);

            ProcessPendingCrawledRawCommand command =
                    ProcessPendingCrawledRawCommand.of(CrawlType.MINI_SHOP, 100);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(0);
            then(mockProcessor).should().process(raw);
            then(crawledRawTransactionManager)
                    .should()
                    .markAsProcessed(eq(raw), any(Instant.class));
        }

        @Test
        @DisplayName("[실패] 프로세서 처리 실패 시 FAILED 처리")
        void shouldMarkAsFailedOnProcessorError() {
            // Given
            CrawledRaw raw =
                    CrawledRaw.forNew(
                            1L, 100L, 10001L, CrawlType.MINI_SHOP, "invalid", Instant.now());
            given(crawledRawReadManager.findPendingByType(CrawlType.MINI_SHOP, 100))
                    .willReturn(List.of(raw));
            given(crawledRawProcessorProvider.getProcessor(CrawlType.MINI_SHOP))
                    .willReturn(mockProcessor);
            willThrow(new IllegalStateException("역직렬화 실패")).given(mockProcessor).process(raw);

            ProcessPendingCrawledRawCommand command =
                    ProcessPendingCrawledRawCommand.of(CrawlType.MINI_SHOP, 100);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(0);
            assertThat(result.failed()).isEqualTo(1);
            then(crawledRawTransactionManager)
                    .should()
                    .markAsFailed(eq(raw), anyString(), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("DETAIL 타입 처리")
    class DetailProcessing {

        @Test
        @DisplayName("[성공] DETAIL Raw 가공 성공")
        void shouldProcessDetailSuccessfully() {
            // Given
            CrawledRaw raw =
                    CrawledRaw.forNew(
                            1L, 100L, 10001L, CrawlType.DETAIL, "{\"json\":true}", Instant.now());
            given(crawledRawReadManager.findPendingByType(CrawlType.DETAIL, 100))
                    .willReturn(List.of(raw));
            given(crawledRawProcessorProvider.getProcessor(CrawlType.DETAIL))
                    .willReturn(mockProcessor);

            ProcessPendingCrawledRawCommand command =
                    ProcessPendingCrawledRawCommand.of(CrawlType.DETAIL, 100);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            then(mockProcessor).should().process(raw);
        }
    }

    @Nested
    @DisplayName("OPTION 타입 처리")
    class OptionProcessing {

        @Test
        @DisplayName("[성공] OPTION Raw 가공 성공")
        void shouldProcessOptionSuccessfully() {
            // Given
            CrawledRaw raw =
                    CrawledRaw.forNew(
                            1L, 100L, 10001L, CrawlType.OPTION, "[{\"json\":true}]", Instant.now());
            given(crawledRawReadManager.findPendingByType(CrawlType.OPTION, 100))
                    .willReturn(List.of(raw));
            given(crawledRawProcessorProvider.getProcessor(CrawlType.OPTION))
                    .willReturn(mockProcessor);

            ProcessPendingCrawledRawCommand command =
                    ProcessPendingCrawledRawCommand.of(CrawlType.OPTION, 100);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            then(mockProcessor).should().process(raw);
        }
    }

    @Nested
    @DisplayName("공통 시나리오")
    class CommonScenarios {

        @Test
        @DisplayName("[성공] PENDING Raw 없을 시 → 빈 결과 반환")
        void shouldReturnEmptyWhenNoPendingRaws() {
            // Given
            given(crawledRawReadManager.findPendingByType(CrawlType.MINI_SHOP, 100))
                    .willReturn(List.of());

            ProcessPendingCrawledRawCommand command =
                    ProcessPendingCrawledRawCommand.of(CrawlType.MINI_SHOP, 100);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(0);
            assertThat(result.success()).isEqualTo(0);
            assertThat(result.failed()).isEqualTo(0);
        }

        @Test
        @DisplayName("[성공] 복수 건 처리 - 일부 성공, 일부 실패")
        void shouldHandleMixedResults() {
            // Given — reconstitute로 서로 다른 ID 부여 (equals 구별 위해)
            Instant now = Instant.now();
            CrawledRaw raw1 =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(1L),
                            1L,
                            100L,
                            10001L,
                            CrawlType.MINI_SHOP,
                            "{\"valid\":true}",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);
            CrawledRaw raw2 =
                    CrawledRaw.reconstitute(
                            CrawledRawId.of(2L),
                            1L,
                            100L,
                            10002L,
                            CrawlType.MINI_SHOP,
                            "invalid",
                            RawDataStatus.PENDING,
                            null,
                            now,
                            null);

            given(crawledRawReadManager.findPendingByType(CrawlType.MINI_SHOP, 100))
                    .willReturn(List.of(raw1, raw2));
            given(crawledRawProcessorProvider.getProcessor(CrawlType.MINI_SHOP))
                    .willReturn(mockProcessor);
            willThrow(new IllegalStateException("역직렬화 실패")).given(mockProcessor).process(raw2);

            ProcessPendingCrawledRawCommand command =
                    ProcessPendingCrawledRawCommand.of(CrawlType.MINI_SHOP, 100);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
            then(crawledRawTransactionManager).should(times(1)).markAsProcessed(any(), any());
            then(crawledRawTransactionManager)
                    .should(times(1))
                    .markAsFailed(any(), anyString(), any());
        }
    }
}
