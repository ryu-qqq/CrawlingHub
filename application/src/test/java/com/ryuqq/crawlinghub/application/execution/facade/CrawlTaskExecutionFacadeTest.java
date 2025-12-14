package com.ryuqq.crawlinghub.application.execution.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.application.crawl.processor.CrawlResultProcessorProvider;
import com.ryuqq.crawlinghub.application.execution.dto.ExecutionContext;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.manager.CrawlExecutionTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.CreateCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskExecutionFacade 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskExecutionFacade 테스트")
class CrawlTaskExecutionFacadeTest {

    @Mock private CrawlTaskReadManager crawlTaskReadManager;

    @Mock private CrawlTaskTransactionManager crawlTaskTransactionManager;

    @Mock private CrawlExecutionTransactionManager crawlExecutionManager;

    @Mock private CrawlResultProcessorProvider processorProvider;

    @Mock private CreateCrawlTaskUseCase createCrawlTaskUseCase;

    @Mock private ClockHolder clockHolder;

    @InjectMocks private CrawlTaskExecutionFacade facade;

    @BeforeEach
    void setUp() {
        Clock fixedClock = FixedClock.aDefaultClock();
        org.mockito.Mockito.lenient().when(clockHolder.getClock()).thenReturn(fixedClock);
    }

    @Nested
    @DisplayName("prepareExecution() 테스트")
    class PrepareExecution {

        @Test
        @DisplayName("[성공] 실행 준비 → ExecutionContext 반환")
        void shouldPrepareExecutionSuccessfully() {
            // Given
            Long taskId = 1L;
            Long schedulerId = 100L;
            Long sellerId = 200L;
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(
                            taskId, schedulerId, sellerId, "MINI_SHOP", "https://example.com");

            // PUBLISHED 상태에서 markAsRunning() 호출 가능
            CrawlTask task = CrawlTaskFixture.aPublishedTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(task));
            given(crawlTaskTransactionManager.persist(task))
                    .willReturn(CrawlTaskIdFixture.anAssignedId());
            given(
                            crawlExecutionManager.startAndPersist(
                                    any(CrawlTaskId.class),
                                    any(CrawlSchedulerId.class),
                                    any(SellerId.class)))
                    .willReturn(execution);

            // When
            ExecutionContext result = facade.prepareExecution(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.crawlTask()).isEqualTo(task);
            assertThat(result.execution()).isEqualTo(execution);
            verify(crawlTaskReadManager).findById(any(CrawlTaskId.class));
            verify(crawlTaskTransactionManager).persist(task);
        }

        @Test
        @DisplayName("[실패] CrawlTask 미존재 → CrawlTaskNotFoundException")
        void shouldThrowWhenTaskNotFound() {
            // Given
            Long taskId = 999L;
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(
                            taskId, 100L, 200L, "MINI_SHOP", "https://example.com");

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> facade.prepareExecution(command))
                    .isInstanceOf(CrawlTaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("completeWithFailure() 테스트")
    class CompleteWithFailure {

        @Test
        @DisplayName("[성공] 실패 처리 → 상태 FAILED로 변경")
        void shouldCompleteWithFailure() {
            // Given
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            ExecutionContext context = new ExecutionContext(task, execution);
            Integer httpStatusCode = 500;
            String errorMessage = "Internal Server Error";

            given(crawlTaskTransactionManager.persist(task))
                    .willReturn(CrawlTaskIdFixture.anAssignedId());

            // When
            facade.completeWithFailure(context, httpStatusCode, errorMessage);

            // Then
            verify(crawlExecutionManager)
                    .completeWithFailure(execution, httpStatusCode, errorMessage);
            verify(crawlTaskTransactionManager).persist(task);
        }
    }

    @Nested
    @DisplayName("completeWithTimeout() 테스트")
    class CompleteWithTimeout {

        @Test
        @DisplayName("[성공] 타임아웃 처리")
        void shouldCompleteWithTimeout() {
            // Given
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            ExecutionContext context = new ExecutionContext(task, execution);
            String errorMessage = "Request timed out after 30 seconds";

            given(crawlTaskTransactionManager.persist(task))
                    .willReturn(CrawlTaskIdFixture.anAssignedId());

            // When
            facade.completeWithTimeout(context, errorMessage);

            // Then
            verify(crawlExecutionManager).completeWithTimeout(execution, errorMessage);
            verify(crawlTaskTransactionManager).persist(task);
        }
    }
}
