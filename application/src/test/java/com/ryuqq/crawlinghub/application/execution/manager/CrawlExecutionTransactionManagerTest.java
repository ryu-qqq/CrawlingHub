package com.ryuqq.crawlinghub.application.execution.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.application.execution.port.out.command.CrawlExecutionPersistencePort;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlExecutionTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlExecutionTransactionManager 테스트")
class CrawlExecutionTransactionManagerTest {

    @Mock private CrawlExecutionPersistencePort crawlExecutionPersistencePort;

    @Mock private ClockHolder clockHolder;

    @InjectMocks private CrawlExecutionTransactionManager manager;

    @BeforeEach
    void setUp() {
        Clock fixedClock = FixedClock.aDefaultClock();
        org.mockito.Mockito.lenient().when(clockHolder.getClock()).thenReturn(fixedClock);
    }

    @Nested
    @DisplayName("startAndPersist() 테스트")
    class StartAndPersist {

        @Test
        @DisplayName("[성공] CrawlExecution 시작 및 저장 → RUNNING 상태")
        void shouldStartExecutionAndPersist() {
            // Given
            CrawlTaskId taskId = CrawlTaskIdFixture.anAssignedId();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            SellerId sellerId = SellerIdFixture.anAssignedId();
            CrawlExecutionId expectedId = CrawlExecutionIdFixture.anAssignedId();

            given(crawlExecutionPersistencePort.persist(any(CrawlExecution.class)))
                    .willReturn(expectedId);

            // When
            CrawlExecution result = manager.startAndPersist(taskId, schedulerId, sellerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(CrawlExecutionStatus.RUNNING);
            verify(crawlExecutionPersistencePort).persist(any(CrawlExecution.class));
        }
    }

    @Nested
    @DisplayName("completeWithSuccess() 테스트")
    class CompleteWithSuccess {

        @Test
        @DisplayName("[성공] CrawlExecution 성공 완료")
        void shouldCompleteWithSuccess() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            String responseBody = "{\"success\": true}";
            Integer httpStatusCode = 200;
            CrawlExecutionId expectedId = CrawlExecutionIdFixture.anAssignedId();

            given(crawlExecutionPersistencePort.persist(execution)).willReturn(expectedId);

            // When
            CrawlExecutionId result =
                    manager.completeWithSuccess(execution, responseBody, httpStatusCode);

            // Then
            assertThat(result).isEqualTo(expectedId);
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            verify(crawlExecutionPersistencePort).persist(execution);
        }
    }

    @Nested
    @DisplayName("completeWithFailure() 테스트")
    class CompleteWithFailure {

        @Test
        @DisplayName("[성공] CrawlExecution 실패 완료 (HTTP 에러)")
        void shouldCompleteWithFailure() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            Integer httpStatusCode = 500;
            String errorMessage = "Internal Server Error";
            CrawlExecutionId expectedId = CrawlExecutionIdFixture.anAssignedId();

            given(crawlExecutionPersistencePort.persist(execution)).willReturn(expectedId);

            // When
            CrawlExecutionId result =
                    manager.completeWithFailure(execution, httpStatusCode, errorMessage);

            // Then
            assertThat(result).isEqualTo(expectedId);
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.FAILED);
            verify(crawlExecutionPersistencePort).persist(execution);
        }

        @Test
        @DisplayName("[성공] CrawlExecution 실패 완료 (응답 본문 포함)")
        void shouldCompleteWithFailureAndResponseBody() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            String responseBody = "{\"error\": \"rate limited\"}";
            Integer httpStatusCode = 429;
            String errorMessage = "Rate Limited";
            CrawlExecutionId expectedId = CrawlExecutionIdFixture.anAssignedId();

            given(crawlExecutionPersistencePort.persist(execution)).willReturn(expectedId);

            // When
            CrawlExecutionId result =
                    manager.completeWithFailure(
                            execution, responseBody, httpStatusCode, errorMessage);

            // Then
            assertThat(result).isEqualTo(expectedId);
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("completeWithTimeout() 테스트")
    class CompleteWithTimeout {

        @Test
        @DisplayName("[성공] CrawlExecution 타임아웃 완료")
        void shouldCompleteWithTimeout() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            String errorMessage = "Request timed out after 30 seconds";
            CrawlExecutionId expectedId = CrawlExecutionIdFixture.anAssignedId();

            given(crawlExecutionPersistencePort.persist(execution)).willReturn(expectedId);

            // When
            CrawlExecutionId result = manager.completeWithTimeout(execution, errorMessage);

            // Then
            assertThat(result).isEqualTo(expectedId);
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.TIMEOUT);
            verify(crawlExecutionPersistencePort).persist(execution);
        }
    }
}
