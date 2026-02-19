package com.ryuqq.crawlinghub.domain.execution.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.domain.execution.exception.InvalidCrawlExecutionStateException;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlExecution Aggregate Root 단위 테스트
 *
 * <p>테스트 대상:
 *
 * <ul>
 *   <li>실행 시작 {@code start()} - RUNNING 상태, ID 미할당
 *   <li>성공 완료 {@code completeWithSuccess()} - SUCCESS 상태 전환
 *   <li>실패 완료 {@code completeWithFailure()} - FAILED 상태 전환
 *   <li>타임아웃 완료 {@code completeWithTimeout()} - TIMEOUT 상태 전환
 *   <li>상태 확인 메서드 - isRunning, isSuccess, isFailure, isCompleted
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlExecution Aggregate Root 테스트")
class CrawlExecutionTest {

    private static final Instant FIXED_INSTANT = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("start() 실행 시작 테스트")
    class Start {

        @Test
        @DisplayName("실행 시작 시 RUNNING 상태, ID 미할당")
        void shouldStartExecutionWithRunningStatusAndUnassignedId() {
            // when
            CrawlExecution execution =
                    CrawlExecution.start(
                            CrawlTaskIdFixture.anAssignedId(),
                            CrawlSchedulerIdFixture.anAssignedId(),
                            SellerIdFixture.anAssignedId(),
                            FIXED_INSTANT);

            // then
            assertThat(execution.getId()).isNotNull();
            assertThat(execution.getId().isAssigned()).isFalse();
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.RUNNING);
            assertThat(execution.isRunning()).isTrue();
            assertThat(execution.isCompleted()).isFalse();
            assertThat(execution.getResult()).isNotNull();
            assertThat(execution.getDuration()).isNotNull();
            assertThat(execution.getDuration().isRunning()).isTrue();
        }

        @Test
        @DisplayName("실행 시작 시 createdAt이 설정됨")
        void shouldSetCreatedAt() {
            // when
            CrawlExecution execution = CrawlExecutionFixture.forNew();

            // then
            assertThat(execution.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("실행 시작 시 CrawlTaskId, CrawlSchedulerId, SellerId가 설정됨")
        void shouldSetRelatedIds() {
            // when
            CrawlExecution execution = CrawlExecutionFixture.forNew();

            // then
            assertThat(execution.getCrawlTaskId()).isNotNull();
            assertThat(execution.getCrawlSchedulerId()).isNotNull();
            assertThat(execution.getSellerId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("reconstitute() 영속성 복원 테스트")
    class Reconstitute {

        @Test
        @DisplayName("기존 데이터로 CrawlExecution 복원 성공")
        void shouldRestoreExecutionFromExistingData() {
            // when
            CrawlExecution execution = CrawlExecutionFixture.aSuccessExecution();

            // then
            assertThat(execution.getId().isAssigned()).isTrue();
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            assertThat(execution.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("completeWithSuccess() 성공 완료 테스트")
    class CompleteWithSuccess {

        @Test
        @DisplayName("RUNNING → SUCCESS 전환 성공")
        void shouldTransitionFromRunningToSuccess() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            String responseBody = "{\"data\":\"test\"}";
            Integer httpStatusCode = 200;

            // when
            execution.completeWithSuccess(responseBody, httpStatusCode, FIXED_INSTANT);

            // then
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            assertThat(execution.isSuccess()).isTrue();
            assertThat(execution.isCompleted()).isTrue();
            assertThat(execution.isRunning()).isFalse();
            assertThat(execution.getResult().responseBody()).isEqualTo(responseBody);
            assertThat(execution.getResult().httpStatusCode()).isEqualTo(httpStatusCode);
            assertThat(execution.getResult().isSuccess()).isTrue();
            assertThat(execution.getDuration().isCompleted()).isTrue();
        }

        @Test
        @DisplayName("RUNNING이 아닌 상태에서 성공 완료 시 예외 발생")
        void shouldThrowExceptionWhenNotInRunningStatus() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aSuccessExecution();

            // when & then
            assertThatThrownBy(() -> execution.completeWithSuccess("{}", 200, FIXED_INSTANT))
                    .isInstanceOf(InvalidCrawlExecutionStateException.class);
        }
    }

    @Nested
    @DisplayName("completeWithFailure() 실패 완료 테스트")
    class CompleteWithFailure {

        @Test
        @DisplayName("RUNNING → FAILED 전환 성공 (응답 본문 없음)")
        void shouldTransitionFromRunningToFailed() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            Integer httpStatusCode = 500;
            String errorMessage = "Internal Server Error";

            // when
            execution.completeWithFailure(httpStatusCode, errorMessage, FIXED_INSTANT);

            // then
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(execution.isFailure()).isTrue();
            assertThat(execution.isCompleted()).isTrue();
            assertThat(execution.getResult().httpStatusCode()).isEqualTo(httpStatusCode);
            assertThat(execution.getResult().errorMessage()).isEqualTo(errorMessage);
            assertThat(execution.getResult().hasError()).isTrue();
        }

        @Test
        @DisplayName("RUNNING → FAILED 전환 성공 (응답 본문 포함)")
        void shouldTransitionFromRunningToFailedWithBody() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            String responseBody = "{\"error\":\"Bad Request\"}";
            Integer httpStatusCode = 400;
            String errorMessage = "Bad Request";

            // when
            execution.completeWithFailure(
                    responseBody, httpStatusCode, errorMessage, FIXED_INSTANT);

            // then
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(execution.getResult().responseBody()).isEqualTo(responseBody);
            assertThat(execution.getResult().httpStatusCode()).isEqualTo(httpStatusCode);
            assertThat(execution.getResult().errorMessage()).isEqualTo(errorMessage);
            assertThat(execution.getResult().isClientError()).isTrue();
        }

        @Test
        @DisplayName("RUNNING이 아닌 상태에서 실패 완료 시 예외 발생")
        void shouldThrowExceptionWhenNotInRunningStatus() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aFailedExecution();

            // when & then
            assertThatThrownBy(() -> execution.completeWithFailure(500, "Error", FIXED_INSTANT))
                    .isInstanceOf(InvalidCrawlExecutionStateException.class);
        }
    }

    @Nested
    @DisplayName("completeWithTimeout() 타임아웃 완료 테스트")
    class CompleteWithTimeout {

        @Test
        @DisplayName("RUNNING → TIMEOUT 전환 성공")
        void shouldTransitionFromRunningToTimeout() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            String errorMessage = "Request timed out after 30000ms";

            // when
            execution.completeWithTimeout(errorMessage, FIXED_INSTANT);

            // then
            assertThat(execution.getStatus()).isEqualTo(CrawlExecutionStatus.TIMEOUT);
            assertThat(execution.isFailure()).isTrue();
            assertThat(execution.isCompleted()).isTrue();
            assertThat(execution.getResult().errorMessage()).isEqualTo(errorMessage);
            assertThat(execution.getResult().httpStatusCode()).isNull();
        }

        @Test
        @DisplayName("RUNNING이 아닌 상태에서 타임아웃 완료 시 예외 발생")
        void shouldThrowExceptionWhenNotInRunningStatus() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aTimeoutExecution();

            // when & then
            assertThatThrownBy(() -> execution.completeWithTimeout("Timeout", FIXED_INSTANT))
                    .isInstanceOf(InvalidCrawlExecutionStateException.class);
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드 테스트")
    class StatusChecks {

        @Test
        @DisplayName("isRunning()은 RUNNING 상태일 때 true 반환")
        void shouldReturnTrueForIsRunningWhenRunning() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();

            // then
            assertThat(execution.isRunning()).isTrue();
            assertThat(execution.isSuccess()).isFalse();
            assertThat(execution.isFailure()).isFalse();
            assertThat(execution.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("isSuccess()는 SUCCESS 상태일 때 true 반환")
        void shouldReturnTrueForIsSuccessWhenSuccess() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aSuccessExecution();

            // then
            assertThat(execution.isSuccess()).isTrue();
            assertThat(execution.isRunning()).isFalse();
            assertThat(execution.isFailure()).isFalse();
            assertThat(execution.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isFailure()는 FAILED 상태일 때 true 반환")
        void shouldReturnTrueForIsFailureWhenFailed() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aFailedExecution();

            // then
            assertThat(execution.isFailure()).isTrue();
            assertThat(execution.isSuccess()).isFalse();
            assertThat(execution.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isFailure()는 TIMEOUT 상태일 때 true 반환")
        void shouldReturnTrueForIsFailureWhenTimeout() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aTimeoutExecution();

            // then
            assertThat(execution.isFailure()).isTrue();
            assertThat(execution.isSuccess()).isFalse();
            assertThat(execution.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isRateLimited()는 HTTP 429일 때 true 반환")
        void shouldReturnTrueForIsRateLimitedWhen429() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aRateLimitedExecution();

            // then
            assertThat(execution.isRateLimited()).isTrue();
            assertThat(execution.isFailure()).isTrue();
        }

        @Test
        @DisplayName("isRateLimited()는 result가 null이면 false 반환")
        void shouldReturnFalseForIsRateLimitedWhenResultIsNull() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aSuccessExecution();

            // then
            assertThat(execution.isRateLimited()).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter 메서드 테스트")
    class GetterMethods {

        @Test
        @DisplayName("getId()는 CrawlExecutionId 반환")
        void shouldReturnExecutionId() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.anExecutionWithId(123L);

            // then
            assertThat(execution.getId().value()).isEqualTo(123L);
        }

        @Test
        @DisplayName("getCrawlTaskId()는 CrawlTaskId 반환")
        void shouldReturnCrawlTaskId() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();

            // then
            assertThat(execution.getCrawlTaskId()).isEqualTo(CrawlTaskIdFixture.anAssignedId());
        }

        @Test
        @DisplayName("getCrawlSchedulerId()는 CrawlSchedulerId 반환")
        void shouldReturnCrawlSchedulerId() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();

            // then
            assertThat(execution.getCrawlSchedulerId())
                    .isEqualTo(CrawlSchedulerIdFixture.anAssignedId());
        }

        @Test
        @DisplayName("getSellerId()는 SellerId 반환")
        void shouldReturnSellerId() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();

            // then
            assertThat(execution.getSellerId()).isEqualTo(SellerIdFixture.anAssignedId());
        }

        @Test
        @DisplayName("getResult()는 CrawlExecutionResult 반환")
        void shouldReturnResult() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aSuccessExecution();

            // then
            assertThat(execution.getResult()).isNotNull();
            assertThat(execution.getResult().isSuccess()).isTrue();
        }

        @Test
        @DisplayName("getDuration()은 ExecutionDuration 반환")
        void shouldReturnDuration() {
            // given
            CrawlExecution execution = CrawlExecutionFixture.aSuccessExecution();

            // then
            assertThat(execution.getDuration()).isNotNull();
        }
    }
}
