package com.ryuqq.crawlinghub.application.execution.internal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.application.common.metric.CrawlHubMetrics;
import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper.CrawlContextMapper;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.CrawlResultProcessor;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.CrawlResultProcessorProvider;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.ProcessingResult;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.BorrowedUserAgent;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.exception.RetryableExecutionException;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskExecutionCoordinator 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskExecutionCoordinator 테스트")
class CrawlTaskExecutionCoordinatorTest {

    @Mock private ExecutionCommandFacade commandFacade;

    @Mock private CrawlingUserAgentCoordinator userAgentCoordinator;

    @Mock private CrawlingProcessor crawlingProcessor;

    @Mock private CrawlResultProcessorProvider processorProvider;

    @Mock private FollowUpTaskCreator followUpTaskCreator;

    @Mock private CrawlContextMapper crawlContextMapper;

    @Mock private CrawlHubMetrics metrics;

    @Mock private CrawlResultProcessor mockProcessor;

    @InjectMocks private CrawlTaskExecutionCoordinator coordinator;

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName(
                "[성공] 크롤링 성공 → borrow + prepare + crawl + return + completeExecution +"
                        + " processResult")
        void shouldCompleteWithSuccessWhenCrawlingSucceeds() {
            // Given
            CrawlTask task = CrawlTaskFixture.aPublishedTask();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 100L, 200L, "MINI_SHOP", "https://example.com");
            CrawlExecution execution = CrawlExecutionFixture.forNew();
            CrawlTaskExecutionBundle bundle =
                    CrawlTaskExecutionBundle.of(task, execution, command, Instant.now());
            CrawlResult successResult = CrawlResult.success("{\"data\": []}", 200);
            ProcessingResult processingResult = ProcessingResult.completed(10, 10);

            BorrowedUserAgent agent = createBorrowedAgent();
            CrawlContext crawlContext = createCrawlContext();
            given(userAgentCoordinator.borrow()).willReturn(agent);
            given(
                            crawlContextMapper.toCrawlContext(
                                    any(CrawlTask.class), any(BorrowedUserAgent.class)))
                    .willReturn(crawlContext);
            given(crawlingProcessor.executeCrawling(any(CrawlTaskExecutionBundle.class)))
                    .willReturn(successResult);
            given(processorProvider.getProcessor(any(CrawlTaskType.class)))
                    .willReturn(mockProcessor);
            given(mockProcessor.process(any(CrawlResult.class), any(CrawlTask.class)))
                    .willReturn(processingResult);

            // When
            coordinator.execute(bundle);

            // Then
            then(userAgentCoordinator).should().borrow();
            then(commandFacade).should(times(2)).persist(any(CrawlTaskExecutionBundle.class));
            then(crawlingProcessor).should().executeCrawling(any(CrawlTaskExecutionBundle.class));
            then(userAgentCoordinator)
                    .should()
                    .returnAgent(anyLong(), anyBoolean(), anyInt(), anyInt());
            then(followUpTaskCreator).should(never()).executeBatch(any());
        }

        @Test
        @DisplayName(
                "[실패] 크롤링 실패 (HTTP 500) → borrow + prepare + crawl + return +"
                        + " completeExecution(failure)")
        void shouldCompleteWithFailureWhenCrawlingFails() {
            // Given
            CrawlTask task = CrawlTaskFixture.aPublishedTask();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 100L, 200L, "MINI_SHOP", "https://example.com");
            CrawlExecution execution = CrawlExecutionFixture.forNew();
            CrawlTaskExecutionBundle bundle =
                    CrawlTaskExecutionBundle.of(task, execution, command, Instant.now());
            CrawlResult failResult = CrawlResult.failure(500, "Internal Server Error");

            BorrowedUserAgent agent = createBorrowedAgent();
            CrawlContext crawlContext = createCrawlContext();
            given(userAgentCoordinator.borrow()).willReturn(agent);
            given(
                            crawlContextMapper.toCrawlContext(
                                    any(CrawlTask.class), any(BorrowedUserAgent.class)))
                    .willReturn(crawlContext);
            given(crawlingProcessor.executeCrawling(any(CrawlTaskExecutionBundle.class)))
                    .willReturn(failResult);

            // When
            coordinator.execute(bundle);

            // Then
            then(userAgentCoordinator).should().borrow();
            then(commandFacade).should(times(2)).persist(any(CrawlTaskExecutionBundle.class));
            then(userAgentCoordinator)
                    .should()
                    .returnAgent(anyLong(), anyBoolean(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("[안전 실패] 크롤링 중 예외 → safeCompleteWithFailure + returnAgent (예외 전파 없음)")
        void shouldHandleFailureSafelyWhenCrawlingThrows() {
            // Given
            CrawlTask task = CrawlTaskFixture.aPublishedTask();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 100L, 200L, "MINI_SHOP", "https://example.com");
            CrawlExecution execution = CrawlExecutionFixture.forNew();
            CrawlTaskExecutionBundle bundle =
                    CrawlTaskExecutionBundle.of(task, execution, command, Instant.now());

            BorrowedUserAgent agent = createBorrowedAgent();
            CrawlContext crawlContext = createCrawlContext();
            given(userAgentCoordinator.borrow()).willReturn(agent);
            given(
                            crawlContextMapper.toCrawlContext(
                                    any(CrawlTask.class), any(BorrowedUserAgent.class)))
                    .willReturn(crawlContext);
            given(crawlingProcessor.executeCrawling(any(CrawlTaskExecutionBundle.class)))
                    .willThrow(new RuntimeException("Connection timeout"));

            // When — 예외가 전파되지 않음
            assertThatCode(() -> coordinator.execute(bundle)).doesNotThrowAnyException();

            // Then
            then(userAgentCoordinator).should().borrow();
            then(commandFacade).should(times(2)).persist(any(CrawlTaskExecutionBundle.class));
            then(userAgentCoordinator)
                    .should()
                    .returnAgent(anyLong(), anyBoolean(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("[재시도] CircuitBreakerOpen → RetryableExecutionException (Task는 PUBLISHED 유지)")
        void shouldThrowRetryableWhenCircuitBreakerOpen() {
            // Given
            CrawlTask task = CrawlTaskFixture.aPublishedTask();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 100L, 200L, "MINI_SHOP", "https://example.com");
            CrawlExecution execution = CrawlExecutionFixture.forNew();
            CrawlTaskExecutionBundle bundle =
                    CrawlTaskExecutionBundle.of(task, execution, command, Instant.now());

            given(userAgentCoordinator.borrow()).willThrow(new CircuitBreakerOpenException(15.0));

            // When & Then — RetryableExecutionException으로 전환되어 전파
            assertThatThrownBy(() -> coordinator.execute(bundle))
                    .isInstanceOf(RetryableExecutionException.class);

            // prepareExecution이 호출되지 않아야 함 (Task는 PUBLISHED 유지)
            then(commandFacade).should(never()).persist(any());
            then(crawlingProcessor).should(never()).executeCrawling(any());
        }

        @Test
        @DisplayName(
                "[재시도] NoAvailableUserAgent → RetryableExecutionException (Task는 PUBLISHED 유지)")
        void shouldThrowRetryableWhenNoAvailableUserAgent() {
            // Given
            CrawlTask task = CrawlTaskFixture.aPublishedTask();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 100L, 200L, "MINI_SHOP", "https://example.com");
            CrawlExecution execution = CrawlExecutionFixture.forNew();
            CrawlTaskExecutionBundle bundle =
                    CrawlTaskExecutionBundle.of(task, execution, command, Instant.now());

            given(userAgentCoordinator.borrow()).willThrow(new NoAvailableUserAgentException());

            // When & Then — RetryableExecutionException으로 전환되어 전파
            assertThatThrownBy(() -> coordinator.execute(bundle))
                    .isInstanceOf(RetryableExecutionException.class);

            // prepareExecution이 호출되지 않아야 함 (Task는 PUBLISHED 유지)
            then(commandFacade).should(never()).persist(any());
            then(crawlingProcessor).should(never()).executeCrawling(any());
        }

        private CrawlContext createCrawlContext() {
            return new CrawlContext(
                    1L,
                    100L,
                    200L,
                    CrawlTaskType.MINI_SHOP,
                    "https://example.com",
                    1L,
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                    "session-token-123",
                    null,
                    null);
        }

        private BorrowedUserAgent createBorrowedAgent() {
            return new BorrowedUserAgent(
                    1L,
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                    "session-token-123",
                    null,
                    null,
                    0);
        }
    }
}
