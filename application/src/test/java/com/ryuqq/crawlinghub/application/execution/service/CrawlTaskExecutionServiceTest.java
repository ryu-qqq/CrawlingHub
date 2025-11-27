package com.ryuqq.crawlinghub.application.execution.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.application.crawl.component.Crawler;
import com.ryuqq.crawlinghub.application.crawl.component.CrawlerProvider;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlContext;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.execution.dto.ExecutionContext;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.facade.CrawlTaskExecutionFacade;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CacheStatus;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecordUserAgentResultCommand;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.ConsumeUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecordUserAgentResultUseCase;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskExecutionService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskExecutionService 테스트")
class CrawlTaskExecutionServiceTest {

    @Mock private CrawlTaskExecutionFacade crawlTaskExecutionFacade;

    @Mock private CrawlerProvider crawlerProvider;

    @Mock private ConsumeUserAgentUseCase consumeUserAgentUseCase;

    @Mock private RecordUserAgentResultUseCase recordUserAgentResultUseCase;

    @Mock private Crawler mockCrawler;

    @InjectMocks private CrawlTaskExecutionService service;

    @Nested
    @DisplayName("execute() 크롤 태스크 실행 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 크롤링 성공 시 completeWithSuccess 호출")
        void shouldCompleteWithSuccessWhenCrawlingSucceeds() {
            // Given
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            ExecutionContext context = new ExecutionContext(task, execution);
            CachedUserAgent userAgent = createReadyUserAgent();
            CrawlResult successResult = CrawlResult.success("{\"data\": []}", 200);

            given(crawlTaskExecutionFacade.prepareExecution(command)).willReturn(context);
            given(consumeUserAgentUseCase.execute()).willReturn(userAgent);
            given(crawlerProvider.getCrawler(any(CrawlTaskType.class))).willReturn(mockCrawler);
            given(mockCrawler.crawl(any(CrawlContext.class))).willReturn(successResult);

            // When
            service.execute(command);

            // Then
            then(crawlTaskExecutionFacade).should().prepareExecution(command);
            then(consumeUserAgentUseCase).should().execute();
            then(crawlerProvider).should().getCrawler(any(CrawlTaskType.class));
            then(mockCrawler).should().crawl(any(CrawlContext.class));
            then(recordUserAgentResultUseCase)
                    .should()
                    .execute(any(RecordUserAgentResultCommand.class));
            then(crawlTaskExecutionFacade).should().completeWithSuccess(context, successResult);
        }

        @Test
        @DisplayName("[성공] 크롤링 실패(HTTP 에러) 시 completeWithFailure 호출")
        void shouldCompleteWithFailureWhenCrawlingFails() {
            // Given
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            ExecutionContext context = new ExecutionContext(task, execution);
            CachedUserAgent userAgent = createReadyUserAgent();
            CrawlResult failResult = CrawlResult.failure(500, "Internal Server Error");

            given(crawlTaskExecutionFacade.prepareExecution(command)).willReturn(context);
            given(consumeUserAgentUseCase.execute()).willReturn(userAgent);
            given(crawlerProvider.getCrawler(any(CrawlTaskType.class))).willReturn(mockCrawler);
            given(mockCrawler.crawl(any(CrawlContext.class))).willReturn(failResult);

            // When
            service.execute(command);

            // Then
            then(crawlTaskExecutionFacade)
                    .should()
                    .completeWithFailure(context, 500, "Internal Server Error");
            then(crawlTaskExecutionFacade).should(never()).completeWithSuccess(any(), any());
        }

        @Test
        @DisplayName("[성공] 429 Rate Limit 시 UserAgent 실패 기록 및 completeWithFailure 호출")
        void shouldRecordFailureAndCompleteWithFailureWhenRateLimited() {
            // Given
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            ExecutionContext context = new ExecutionContext(task, execution);
            CachedUserAgent userAgent = createReadyUserAgent();
            CrawlResult rateLimitResult = CrawlResult.failure(429, "Rate limited (429)");

            given(crawlTaskExecutionFacade.prepareExecution(command)).willReturn(context);
            given(consumeUserAgentUseCase.execute()).willReturn(userAgent);
            given(crawlerProvider.getCrawler(any(CrawlTaskType.class))).willReturn(mockCrawler);
            given(mockCrawler.crawl(any(CrawlContext.class))).willReturn(rateLimitResult);

            // When
            service.execute(command);

            // Then
            then(recordUserAgentResultUseCase)
                    .should()
                    .execute(any(RecordUserAgentResultCommand.class));
            then(crawlTaskExecutionFacade)
                    .should()
                    .completeWithFailure(context, 429, "Rate limited (429)");
        }

        @Test
        @DisplayName("[실패] 크롤링 중 예외 발생 시 UserAgent 실패 기록 후 예외 재전파")
        void shouldRecordFailureAndRethrowExceptionWhenCrawlingThrows() {
            // Given
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            ExecutionContext context = new ExecutionContext(task, execution);
            CachedUserAgent userAgent = createReadyUserAgent();

            given(crawlTaskExecutionFacade.prepareExecution(command)).willReturn(context);
            given(consumeUserAgentUseCase.execute()).willReturn(userAgent);
            given(crawlerProvider.getCrawler(any(CrawlTaskType.class))).willReturn(mockCrawler);
            given(mockCrawler.crawl(any(CrawlContext.class)))
                    .willThrow(new RuntimeException("Connection timeout"));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Connection timeout");

            then(recordUserAgentResultUseCase)
                    .should()
                    .execute(any(RecordUserAgentResultCommand.class));
            then(crawlTaskExecutionFacade)
                    .should()
                    .completeWithFailure(context, null, "Connection timeout");
        }

        @Test
        @DisplayName("[성공] 다양한 TaskType에 대해 적절한 Crawler 선택")
        void shouldSelectCorrectCrawlerForTaskType() {
            // Given
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(
                            1L, 1L, 1L, "DETAIL", "https://example.com/api/product/123");
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            ExecutionContext context = new ExecutionContext(task, execution);
            CachedUserAgent userAgent = createReadyUserAgent();
            CrawlResult successResult = CrawlResult.success("{\"product\": {}}", 200);

            given(crawlTaskExecutionFacade.prepareExecution(command)).willReturn(context);
            given(consumeUserAgentUseCase.execute()).willReturn(userAgent);
            given(crawlerProvider.getCrawler(any(CrawlTaskType.class))).willReturn(mockCrawler);
            given(mockCrawler.crawl(any(CrawlContext.class))).willReturn(successResult);

            // When
            service.execute(command);

            // Then
            then(crawlerProvider).should().getCrawler(any(CrawlTaskType.class));
        }

        private CachedUserAgent createReadyUserAgent() {
            return new CachedUserAgent(
                    1L,
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                    "session-token-123",
                    Instant.now().plusSeconds(3600),
                    80,
                    80,
                    Instant.now(),
                    Instant.now().plusSeconds(60),
                    100,
                    CacheStatus.READY,
                    null);
        }
    }
}
