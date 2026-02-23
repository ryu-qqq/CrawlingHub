package com.ryuqq.crawlinghub.application.execution.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.factory.command.CrawlExecutionCommandFactory;
import com.ryuqq.crawlinghub.application.execution.internal.CrawlTaskExecutionCoordinator;
import com.ryuqq.crawlinghub.application.execution.service.command.CrawlTaskExecutionService;
import com.ryuqq.crawlinghub.application.execution.validator.CrawlTaskExecutionValidator;
import com.ryuqq.crawlinghub.domain.execution.exception.RetryableExecutionException;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.time.Instant;
import java.util.Optional;
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
 * <p>Validator/Factory/Coordinator mock 기반 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskExecutionService 테스트")
class CrawlTaskExecutionServiceTest {

    @Mock private CrawlTaskExecutionValidator validator;

    @Mock private CrawlExecutionCommandFactory commandFactory;

    @Mock private CrawlTaskExecutionCoordinator coordinator;

    @InjectMocks private CrawlTaskExecutionService service;

    @Nested
    @DisplayName("execute() 크롤 태스크 실행 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 검증 통과 → Factory Bundle 생성 → Coordinator.execute 호출")
        void shouldCreateBundleAndDelegateToCoordinator() {
            // Given
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");
            CrawlTask task = CrawlTaskFixture.aPublishedTask();
            CrawlTaskExecutionBundle bundle =
                    CrawlTaskExecutionBundle.of(
                            task, CrawlExecutionFixture.forNew(), command, Instant.now());

            given(validator.validateAndGet(1L)).willReturn(Optional.of(task));
            given(commandFactory.createExecutionBundle(task, command)).willReturn(bundle);

            // When
            service.execute(command);

            // Then
            then(validator).should().validateAndGet(1L);
            then(commandFactory).should().createExecutionBundle(task, command);
            then(coordinator).should().execute(bundle);
        }

        @Test
        @DisplayName("[멱등성] 이미 처리된 Task인 경우 정상 종료 (Coordinator 호출하지 않음)")
        void shouldSkipExecutionWhenTaskAlreadyProcessed() {
            // Given
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");

            given(validator.validateAndGet(1L)).willReturn(Optional.empty());

            // When
            service.execute(command);

            // Then
            then(validator).should().validateAndGet(1L);
            then(coordinator).should(never()).execute(any());
        }

        @Test
        @DisplayName("[실패] Validator 인프라 오류 시 RetryableExecutionException 전파")
        void shouldThrowRetryableExceptionWhenValidatorFailsWithDbError() {
            // Given
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");

            given(validator.validateAndGet(1L))
                    .willThrow(
                            new org.springframework.dao.DataAccessResourceFailureException(
                                    "DB 커넥션 실패"));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(RetryableExecutionException.class)
                    .hasMessageContaining("prepareExecution 인프라 오류")
                    .hasCauseInstanceOf(
                            org.springframework.dao.DataAccessResourceFailureException.class);
        }

        @Test
        @DisplayName("[실패] Validator 비즈니스 오류 시 예외 전파")
        void shouldPropagateBusinessExceptionFromValidator() {
            // Given
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");

            given(validator.validateAndGet(1L)).willThrow(new RuntimeException("비즈니스 예외"));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("비즈니스 예외");
        }
    }
}
