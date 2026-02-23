package com.ryuqq.crawlinghub.application.task.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.RetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.internal.CrawlTaskCommandFacade;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskRetryException;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RetryCrawlTaskService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetryCrawlTaskService 테스트")
class RetryCrawlTaskServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");

    @Mock private CrawlTaskReadManager readManager;

    @Mock private CrawlTaskCommandFactory commandFactory;

    @Mock private CrawlTaskAssembler assembler;

    @Mock private CrawlTaskCommandFacade coordinator;

    @Mock private TimeProvider timeProvider;

    @InjectMocks private RetryCrawlTaskService service;

    @Nested
    @DisplayName("retry() 크롤 태스크 재시도 테스트")
    class Retry {

        @Test
        @DisplayName("[성공] 실패한 Task 재시도 시 CrawlTaskResult 반환")
        void shouldRetryTaskAndReturnResult() {
            // Given
            Long crawlTaskId = 1L;
            RetryCrawlTaskCommand command = new RetryCrawlTaskCommand(crawlTaskId);
            CrawlTask failedTask = CrawlTaskFixture.aFailedTask();
            CrawlTaskBundle retryBundle = org.mockito.Mockito.mock(CrawlTaskBundle.class);
            CrawlTaskResult expectedResult =
                    new CrawlTaskResult(
                            1L,
                            1L,
                            1L,
                            "https://example.com/api",
                            "https://example.com",
                            "/api",
                            Map.of(),
                            "RETRY",
                            "META",
                            1,
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            given(readManager.findById(any(CrawlTaskId.class))).willReturn(Optional.of(failedTask));
            given(timeProvider.now()).willReturn(FIXED_INSTANT);
            given(commandFactory.createRetryBundle(failedTask)).willReturn(retryBundle);
            given(assembler.toResult(failedTask)).willReturn(expectedResult);

            // When
            CrawlTaskResult result = service.retry(command);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            then(readManager).should().findById(CrawlTaskId.of(crawlTaskId));
            then(timeProvider).should().now();
            then(commandFactory).should().createRetryBundle(failedTask);
            then(coordinator).should().retry(failedTask, retryBundle);
            then(assembler).should().toResult(failedTask);
        }

        @Test
        @DisplayName("[성공] 타임아웃된 Task 재시도 시 CrawlTaskResult 반환")
        void shouldRetryTimeoutTaskAndReturnResult() {
            // Given
            Long crawlTaskId = 2L;
            RetryCrawlTaskCommand command = new RetryCrawlTaskCommand(crawlTaskId);
            CrawlTask timeoutTask = CrawlTaskFixture.aTimeoutTask();
            CrawlTaskBundle retryBundle = org.mockito.Mockito.mock(CrawlTaskBundle.class);
            CrawlTaskResult expectedResult =
                    new CrawlTaskResult(
                            2L,
                            1L,
                            1L,
                            "https://example.com/api",
                            "https://example.com",
                            "/api",
                            Map.of(),
                            "RETRY",
                            "META",
                            1,
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            given(readManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(timeoutTask));
            given(timeProvider.now()).willReturn(FIXED_INSTANT);
            given(commandFactory.createRetryBundle(timeoutTask)).willReturn(retryBundle);
            given(assembler.toResult(timeoutTask)).willReturn(expectedResult);

            // When
            CrawlTaskResult result = service.retry(command);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            then(readManager).should().findById(CrawlTaskId.of(crawlTaskId));
            then(coordinator).should().retry(timeoutTask, retryBundle);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 Task ID로 재시도 시 CrawlTaskNotFoundException 발생")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            Long crawlTaskId = 999L;
            RetryCrawlTaskCommand command = new RetryCrawlTaskCommand(crawlTaskId);

            given(readManager.findById(any(CrawlTaskId.class))).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.retry(command))
                    .isInstanceOf(CrawlTaskNotFoundException.class);

            then(timeProvider).should(never()).now();
            then(commandFactory).should(never()).createRetryBundle(any());
            then(coordinator).should(never()).retry(any(), any());
        }

        @Test
        @DisplayName("[실패] 최대 재시도 횟수 초과 Task 재시도 시 CrawlTaskRetryException 발생")
        void shouldThrowExceptionWhenMaxRetryExceeded() {
            // Given
            Long crawlTaskId = 3L;
            RetryCrawlTaskCommand command = new RetryCrawlTaskCommand(crawlTaskId);
            CrawlTask maxRetryTask = CrawlTaskFixture.aFailedTaskWithMaxRetry();

            given(readManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(maxRetryTask));
            given(timeProvider.now()).willReturn(FIXED_INSTANT);

            // When & Then
            assertThatThrownBy(() -> service.retry(command))
                    .isInstanceOf(CrawlTaskRetryException.class);

            then(commandFactory).should(never()).createRetryBundle(any());
            then(coordinator).should(never()).retry(any(), any());
        }

        @Test
        @DisplayName("[실패] 재시도 불가 상태(SUCCESS) Task 재시도 시 CrawlTaskRetryException 발생")
        void shouldThrowExceptionWhenTaskNotRetryable() {
            // Given
            Long crawlTaskId = 4L;
            RetryCrawlTaskCommand command = new RetryCrawlTaskCommand(crawlTaskId);
            CrawlTask successTask = CrawlTaskFixture.aSuccessTask();

            given(readManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(successTask));
            given(timeProvider.now()).willReturn(FIXED_INSTANT);

            // When & Then
            assertThatThrownBy(() -> service.retry(command))
                    .isInstanceOf(CrawlTaskRetryException.class);

            then(commandFactory).should(never()).createRetryBundle(any());
            then(coordinator).should(never()).retry(any(), any());
        }
    }
}
