package com.ryuqq.crawlinghub.domain.task;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlTask Domain 단위 테스트
 *
 * <p>테스트 범위:
 * <ul>
 *   <li>생성 테스트: forNew, of, reconstitute</li>
 *   <li>상태 전이 테스트: WAITING → PUBLISHED → RUNNING → SUCCESS/FAILED/RETRY</li>
 *   <li>재시도 로직 테스트: canRetry, incrementRetry, MAX_RETRY_COUNT(3)</li>
 *   <li>타임아웃 테스트: isTimeout (10분 기준)</li>
 *   <li>멱등성 키 테스트</li>
 *   <li>예외 케이스 테스트</li>
 *   <li>Law of Demeter 준수 테스트</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("CrawlTask Domain 단위 테스트")
class CrawlTaskTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTests {

        @Test
        @DisplayName("유효한 입력으로 신규 CrawlTask 생성 성공")
        void shouldCreateNewTaskWithValidInputs() {
            // Given
            MustitSellerId sellerId = MustitSellerId.of(100L);
            TaskType taskType = TaskType.MINI_SHOP;
            RequestUrl requestUrl = RequestUrlFixture.create();
            Integer pageNumber = 1;
            String idempotencyKey = "unique-key-123";
            LocalDateTime scheduledAt = LocalDateTime.now();

            // When
            CrawlTask task = CrawlTask.forNew(
                sellerId,
                taskType,
                requestUrl,
                pageNumber,
                idempotencyKey,
                scheduledAt
            );

            // Then
            assertThat(task).isNotNull();
            assertThat(task.getIdValue()).isNull(); // 신규 생성이므로 ID 없음
            assertThat(task.getSellerIdValue()).isEqualTo(100L);
            assertThat(task.getTaskType()).isEqualTo(TaskType.MINI_SHOP);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING); // 초기 상태
            assertThat(task.getPageNumber()).isEqualTo(1);
            assertThat(task.getIdempotencyKey()).isEqualTo("unique-key-123");
            assertThat(task.getRetryCount()).isEqualTo(0); // 초기 재시도 횟수
            assertThat(task.getStartedAt()).isNull();
            assertThat(task.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("ID를 가진 CrawlTask 생성 성공 (of)")
        void shouldCreateTaskWithId() {
            // Given
            CrawlTaskId taskId = CrawlTaskId.of(1L);
            MustitSellerId sellerId = MustitSellerId.of(100L);
            TaskType taskType = TaskType.MINI_SHOP;
            RequestUrl requestUrl = RequestUrlFixture.create();
            Integer pageNumber = 1;
            String idempotencyKey = "unique-key-123";
            LocalDateTime scheduledAt = LocalDateTime.now();

            // When
            CrawlTask task = CrawlTask.of(
                taskId,
                sellerId,
                taskType,
                requestUrl,
                pageNumber,
                idempotencyKey,
                scheduledAt
            );

            // Then
            assertThat(task.getIdValue()).isEqualTo(1L);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);
        }

        @Test
        @DisplayName("DB reconstitute로 모든 필드 포함 CrawlTask 생성 성공")
        void shouldReconstituteTaskFromDatabase() {
            // Given
            CrawlTaskId taskId = CrawlTaskId.of(1L);
            MustitSellerId sellerId = MustitSellerId.of(100L);
            TaskType taskType = TaskType.MINI_SHOP;
            TaskStatus status = TaskStatus.RUNNING;
            RequestUrl requestUrl = RequestUrlFixture.create();
            Integer pageNumber = 1;
            Integer retryCount = 2;
            String idempotencyKey = "unique-key-123";
            LocalDateTime scheduledAt = LocalDateTime.now().minusHours(1);
            LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
            LocalDateTime createdAt = LocalDateTime.now().minusHours(2);
            LocalDateTime updatedAt = LocalDateTime.now();

            // When
            CrawlTask task = CrawlTask.reconstitute(
                taskId,
                sellerId,
                taskType,
                status,
                requestUrl,
                pageNumber,
                retryCount,
                idempotencyKey,
                scheduledAt,
                startedAt,
                null,
                createdAt,
                updatedAt
            );

            // Then
            assertThat(task.getIdValue()).isEqualTo(1L);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
            assertThat(task.getRetryCount()).isEqualTo(2);
            assertThat(task.getStartedAt()).isEqualTo(startedAt);
            assertThat(task.getCompletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("상태 전이 테스트")
    class StatusTransitionTests {

        @Test
        @DisplayName("WAITING 상태에서 publish() 호출 시 PUBLISHED 상태로 전이")
        void shouldTransitionFromWaitingToPublished() {
            // Given
            CrawlTask task = CrawlTaskFixture.createWaiting();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);

            // When
            task.publish();

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PUBLISHED);
        }

        @Test
        @DisplayName("WAITING이 아닌 상태에서 publish() 호출 시 예외 발생")
        void shouldThrowExceptionWhenPublishingNonWaitingTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.createPublished();

            // When & Then
            assertThatThrownBy(task::publish)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WAITING 상태에서만 발행할 수 있습니다");
        }

        @Test
        @DisplayName("PUBLISHED 상태에서 startProcessing() 호출 시 RUNNING 상태로 전이")
        void shouldTransitionFromPublishedToRunning() {
            // Given
            CrawlTask task = CrawlTaskFixture.createPublished();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PUBLISHED);

            // When
            task.startProcessing();

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
            assertThat(task.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("RETRY 상태에서 startProcessing() 호출 시 RUNNING 상태로 전이")
        void shouldTransitionFromRetryToRunning() {
            // Given
            CrawlTask task = CrawlTaskFixture.createRetry(1);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RETRY);

            // When
            task.startProcessing();

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
            assertThat(task.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("PUBLISHED/RETRY가 아닌 상태에서 startProcessing() 호출 시 예외 발생")
        void shouldThrowExceptionWhenStartingNonPublishedTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.createWaiting();

            // When & Then
            assertThatThrownBy(task::startProcessing)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PUBLISHED 또는 RETRY 상태에서만 시작할 수 있습니다");
        }

        @Test
        @DisplayName("RUNNING 상태에서 completeSuccessfully() 호출 시 SUCCESS 상태로 전이")
        void shouldTransitionFromRunningToSuccess() {
            // Given
            CrawlTask task = CrawlTaskFixture.createRunning();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);

            // When
            task.completeSuccessfully();

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.SUCCESS);
            assertThat(task.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("RUNNING이 아닌 상태에서 completeSuccessfully() 호출 시 예외 발생")
        void shouldThrowExceptionWhenCompletingNonRunningTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.createPublished();

            // When & Then
            assertThatThrownBy(task::completeSuccessfully)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RUNNING 상태에서만 완료할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("실패 및 재시도 테스트")
    class FailureAndRetryTests {

        @Test
        @DisplayName("재시도 가능한 상태에서 failWithError() 호출 시 RETRY 상태로 전이 및 retryCount 증가")
        void shouldTransitionToRetryWhenFailureAndCanRetry() {
            // Given
            CrawlTask task = CrawlTaskFixture.createRunning();
            assertThat(task.getRetryCount()).isEqualTo(0);
            assertThat(task.canRetry()).isTrue();

            // When
            task.failWithError("Connection timeout");

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RETRY);
            assertThat(task.getRetryCount()).isEqualTo(1);
            assertThat(task.getCompletedAt()).isNull(); // RETRY 상태는 아직 완료되지 않음
        }

        @Test
        @DisplayName("재시도 불가능한 상태에서 failWithError() 호출 시 FAILED 상태로 전이")
        void shouldTransitionToFailedWhenFailureAndCannotRetry() {
            // Given
            CrawlTask task = CrawlTaskFixture.createWaiting();
            task.publish();
            task.startProcessing();
            task.failWithError("First failure");
            task.startProcessing(); // RETRY → RUNNING
            task.failWithError("Second failure");
            task.startProcessing(); // RETRY → RUNNING
            task.failWithError("Third failure");
            task.startProcessing(); // RETRY → RUNNING
            assertThat(task.getRetryCount()).isEqualTo(3);
            assertThat(task.canRetry()).isFalse();

            // When
            task.failWithError("Max retries exceeded");

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
            assertThat(task.getCompletedAt()).isNotNull(); // FAILED 상태는 완료됨
        }

        @Test
        @DisplayName("RUNNING이 아닌 상태에서 failWithError() 호출 시 예외 발생")
        void shouldThrowExceptionWhenFailingNonRunningTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.createPublished();

            // When & Then
            assertThatThrownBy(() -> task.failWithError("Error"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RUNNING 상태에서만 실패 처리할 수 있습니다");
        }

        @Test
        @DisplayName("재시도 횟수가 MAX_RETRY_COUNT(3) 미만일 때 canRetry() 는 true 반환")
        void shouldReturnTrueWhenRetryCountBelowMax() {
            // Given
            CrawlTask task0 = CrawlTaskFixture.createRunning();
            CrawlTask task1 = CrawlTaskFixture.createRetry(1);
            CrawlTask task2 = CrawlTaskFixture.createRetry(2);

            // When & Then
            assertThat(task0.canRetry()).isTrue(); // retryCount = 0
            assertThat(task1.canRetry()).isTrue(); // retryCount = 1
            assertThat(task2.canRetry()).isTrue(); // retryCount = 2
        }

        @Test
        @DisplayName("재시도 횟수가 MAX_RETRY_COUNT(3)에 도달하면 canRetry() 는 false 반환")
        void shouldReturnFalseWhenRetryCountReachedMax() {
            // Given
            CrawlTask task = CrawlTaskFixture.createFailed(3);

            // When & Then
            assertThat(task.canRetry()).isFalse(); // retryCount = 3
        }

        @Test
        @DisplayName("재시도 가능 시 incrementRetry() 호출하면 retryCount 증가")
        void shouldIncrementRetryCountWhenCanRetry() {
            // Given
            CrawlTask task = CrawlTaskFixture.createRunning();
            assertThat(task.getRetryCount()).isEqualTo(0);

            // When
            task.incrementRetry();

            // Then
            assertThat(task.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("재시도 불가능 시 incrementRetry() 호출하면 예외 발생")
        void shouldThrowExceptionWhenIncrementingRetryBeyondMax() {
            // Given
            CrawlTask task = CrawlTaskFixture.createFailed(3);
            assertThat(task.canRetry()).isFalse();

            // When & Then
            assertThatThrownBy(task::incrementRetry)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최대 재시도 횟수를 초과했습니다");
        }
    }

    @Nested
    @DisplayName("타임아웃 테스트")
    class TimeoutTests {

        @Test
        @DisplayName("RUNNING 상태에서 10분 초과 시 isTimeout() 는 true 반환")
        void shouldReturnTrueWhenRunningTaskExceeds10Minutes() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startedAt = now.minusMinutes(11); // 11분 전 시작
            CrawlTask task = CrawlTaskFixture.createCustom(
                1L,
                100L,
                TaskType.MINI_SHOP,
                RequestUrlFixture.create(),
                1
            );
            task.publish();
            task.startProcessing();

            // When
            // Note: 실제로는 Clock을 주입해서 테스트하는 것이 더 정확하지만,
            // 현재 CrawlTask는 Clock.systemDefaultZone()을 사용하므로
            // reconstitute로 startedAt을 과거로 설정한 task로 테스트
            CrawlTask timeoutTask = CrawlTask.reconstitute(
                CrawlTaskId.of(1L),
                MustitSellerId.of(100L),
                TaskType.MINI_SHOP,
                TaskStatus.RUNNING,
                RequestUrlFixture.create(),
                1,
                0,
                "key",
                now.minusHours(1),
                startedAt,
                null,
                now.minusHours(2),
                now
            );

            // Then
            assertThat(timeoutTask.isTimeout()).isTrue();
        }

        @Test
        @DisplayName("RUNNING 상태에서 10분 미만일 때 isTimeout() 는 false 반환")
        void shouldReturnFalseWhenRunningTaskWithin10Minutes() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startedAt = now.minusMinutes(5); // 5분 전 시작
            CrawlTask task = CrawlTask.reconstitute(
                CrawlTaskId.of(1L),
                MustitSellerId.of(100L),
                TaskType.MINI_SHOP,
                TaskStatus.RUNNING,
                RequestUrlFixture.create(),
                1,
                0,
                "key",
                now.minusHours(1),
                startedAt,
                null,
                now.minusHours(2),
                now
            );

            // When & Then
            assertThat(task.isTimeout()).isFalse();
        }

        @Test
        @DisplayName("startedAt이 null일 때 isTimeout() 는 false 반환")
        void shouldReturnFalseWhenStartedAtIsNull() {
            // Given
            CrawlTask task = CrawlTaskFixture.createWaiting();
            assertThat(task.getStartedAt()).isNull();

            // When & Then
            assertThat(task.isTimeout()).isFalse();
        }

        @Test
        @DisplayName("RUNNING이 아닌 상태일 때 isTimeout() 는 false 반환")
        void shouldReturnFalseWhenNotRunning() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startedAt = now.minusMinutes(15); // 15분 전 시작했지만
            CrawlTask task = CrawlTask.reconstitute(
                CrawlTaskId.of(1L),
                MustitSellerId.of(100L),
                TaskType.MINI_SHOP,
                TaskStatus.SUCCESS, // 이미 SUCCESS 상태
                RequestUrlFixture.create(),
                1,
                0,
                "key",
                now.minusHours(1),
                startedAt,
                now,
                now.minusHours(2),
                now
            );

            // When & Then
            assertThat(task.isTimeout()).isFalse();
        }
    }

    @Nested
    @DisplayName("상태 조회 테스트")
    class StatusQueryTests {

        @Test
        @DisplayName("hasStatus()는 현재 상태와 일치하면 true 반환")
        void shouldReturnTrueWhenStatusMatches() {
            // Given
            CrawlTask task = CrawlTaskFixture.createWaiting();

            // When & Then
            assertThat(task.hasStatus(TaskStatus.WAITING)).isTrue();
            assertThat(task.hasStatus(TaskStatus.PUBLISHED)).isFalse();
        }

        @Test
        @DisplayName("isCompleted()는 SUCCESS 상태일 때 true 반환")
        void shouldReturnTrueWhenTaskIsCompleted() {
            // Given
            CrawlTask successTask = CrawlTaskFixture.createSuccess();

            // When & Then
            assertThat(successTask.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isCompleted()는 미완료 상태일 때 false 반환")
        void shouldReturnFalseWhenTaskIsNotCompleted() {
            // Given
            CrawlTask waitingTask = CrawlTaskFixture.createWaiting();
            CrawlTask runningTask = CrawlTaskFixture.createRunning();

            // When & Then
            assertThat(waitingTask.isCompleted()).isFalse();
            assertThat(runningTask.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("isFailed()는 FAILED 상태일 때 true 반환")
        void shouldReturnTrueWhenTaskIsFailed() {
            // Given
            CrawlTask task = CrawlTaskFixture.createFailed(3);

            // When & Then
            assertThat(task.isFailed()).isTrue();
        }

        @Test
        @DisplayName("isFailed()는 FAILED가 아닌 상태일 때 false 반환")
        void shouldReturnFalseWhenTaskIsNotFailed() {
            // Given
            CrawlTask successTask = CrawlTaskFixture.createSuccess();
            CrawlTask runningTask = CrawlTaskFixture.createRunning();

            // When & Then
            assertThat(successTask.isFailed()).isFalse();
            assertThat(runningTask.isFailed()).isFalse();
        }
    }

    @Nested
    @DisplayName("멱등성 키 테스트")
    class IdempotencyTests {

        @Test
        @DisplayName("동일한 멱등성 키를 가진 두 작업은 equals()로 비교 시 ID가 같으면 동일")
        void shouldBeEqualWhenSameIdAndIdempotencyKey() {
            // Given
            CrawlTask task1 = CrawlTaskFixture.createWithId(1L);
            CrawlTask task2 = CrawlTaskFixture.createWithId(1L);

            // When & Then
            assertThat(task1).isEqualTo(task2); // ID 기반 동등성
        }

        @Test
        @DisplayName("다른 ID를 가진 작업은 멱등성 키가 같아도 다름")
        void shouldNotBeEqualWhenDifferentId() {
            // Given
            CrawlTask task1 = CrawlTaskFixture.createWithId(1L);
            CrawlTask task2 = CrawlTaskFixture.createWithId(2L);

            // When & Then
            assertThat(task1).isNotEqualTo(task2);
        }

        @Test
        @DisplayName("멱등성 키는 생성 시 설정되고 변경되지 않음")
        void shouldPreserveIdempotencyKey() {
            // Given
            String idempotencyKey = "unique-key-abc-123";
            CrawlTask task = CrawlTask.forNew(
                MustitSellerId.of(100L),
                TaskType.MINI_SHOP,
                RequestUrlFixture.create(),
                1,
                idempotencyKey,
                LocalDateTime.now()
            );

            // When
            task.publish();
            task.startProcessing();

            // Then
            assertThat(task.getIdempotencyKey()).isEqualTo(idempotencyKey);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("셀러 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlTask.forNew(
                    null,
                    TaskType.MINI_SHOP,
                    RequestUrlFixture.create(),
                    1,
                    "key",
                    LocalDateTime.now()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 ID는 필수입니다");
        }

        @Test
        @DisplayName("작업 유형이 null이면 예외 발생")
        void shouldThrowExceptionWhenTaskTypeIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlTask.forNew(
                    MustitSellerId.of(100L),
                    null,
                    RequestUrlFixture.create(),
                    1,
                    "key",
                    LocalDateTime.now()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("작업 유형은 필수입니다");
        }

        @Test
        @DisplayName("요청 URL이 null이면 예외 발생")
        void shouldThrowExceptionWhenRequestUrlIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlTask.forNew(
                    MustitSellerId.of(100L),
                    TaskType.MINI_SHOP,
                    null,
                    1,
                    "key",
                    LocalDateTime.now()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("요청 URL은 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("멱등성 키가 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenIdempotencyKeyIsNullOrBlank(String invalidKey) {
            // When & Then
            assertThatThrownBy(() ->
                CrawlTask.forNew(
                    MustitSellerId.of(100L),
                    TaskType.MINI_SHOP,
                    RequestUrlFixture.create(),
                    1,
                    invalidKey,
                    LocalDateTime.now()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("멱등성 키는 필수입니다");
        }

        @Test
        @DisplayName("예약 시간이 null이면 예외 발생")
        void shouldThrowExceptionWhenScheduledAtIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlTask.forNew(
                    MustitSellerId.of(100L),
                    TaskType.MINI_SHOP,
                    RequestUrlFixture.create(),
                    1,
                    "key",
                    null
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약 시간은 필수입니다");
        }

        @Test
        @DisplayName("of() 메서드에서 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInOf() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlTask.of(
                    null,
                    MustitSellerId.of(100L),
                    TaskType.MINI_SHOP,
                    RequestUrlFixture.create(),
                    1,
                    "key",
                    LocalDateTime.now()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlTask ID는 필수입니다");
        }

        @Test
        @DisplayName("reconstitute() 메서드에서 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInReconstitute() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlTask.reconstitute(
                    null,
                    MustitSellerId.of(100L),
                    TaskType.MINI_SHOP,
                    TaskStatus.WAITING,
                    RequestUrlFixture.create(),
                    1,
                    0,
                    "key",
                    LocalDateTime.now(),
                    null,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }
    }

    @Nested
    @DisplayName("불변성 검증 테스트")
    class InvariantTests {

        @Test
        @DisplayName("신규 작업 생성 시 초기 상태는 WAITING")
        void shouldHaveWaitingStatusWhenNewTaskCreated() {
            // Given & When
            CrawlTask task = CrawlTaskFixture.create();

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);
        }

        @Test
        @DisplayName("신규 작업 생성 시 재시도 횟수는 0")
        void shouldHaveZeroRetryCountWhenNewTaskCreated() {
            // Given & When
            CrawlTask task = CrawlTaskFixture.create();

            // Then
            assertThat(task.getRetryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("작업 시작 전에는 startedAt이 null")
        void shouldHaveNullStartedAtBeforeStarting() {
            // Given & When
            CrawlTask task = CrawlTaskFixture.createPublished();

            // Then
            assertThat(task.getStartedAt()).isNull();
        }

        @Test
        @DisplayName("작업 완료 전에는 completedAt이 null")
        void shouldHaveNullCompletedAtBeforeCompletion() {
            // Given & When
            CrawlTask task = CrawlTaskFixture.createRunning();

            // Then
            assertThat(task.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("재시도 횟수는 MAX_RETRY_COUNT(3)를 초과할 수 없음")
        void shouldNotExceedMaxRetryCount() {
            // Given
            CrawlTask task = CrawlTaskFixture.createRunning();

            // When
            task.failWithError("Error 1");
            task.startProcessing();
            task.failWithError("Error 2");
            task.startProcessing();
            task.failWithError("Error 3");
            task.startProcessing();
            task.failWithError("Error 4");

            // Then
            assertThat(task.getRetryCount()).isEqualTo(3);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()는 ID를 직접 노출하지 않고 값만 반환")
        void shouldReturnIdValueWithoutExposingIdObject() {
            // Given
            CrawlTask task = CrawlTaskFixture.createWithId(100L);

            // When
            Long idValue = task.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(100L);
        }

        @Test
        @DisplayName("getSellerIdValue()는 셀러 ID를 직접 노출하지 않고 값만 반환")
        void shouldReturnSellerIdValueWithoutExposingSellerIdObject() {
            // Given
            CrawlTask task = CrawlTaskFixture.createWithSellerId(200L);

            // When
            Long sellerIdValue = task.getSellerIdValue();

            // Then
            assertThat(sellerIdValue).isEqualTo(200L);
        }

        @Test
        @DisplayName("getRequestUrlValue()는 RequestUrl 객체를 직접 노출하지 않고 값만 반환")
        void shouldReturnRequestUrlValueWithoutExposingRequestUrlObject() {
            // Given
            CrawlTask task = CrawlTaskFixture.create();

            // When
            String urlValue = task.getRequestUrlValue();

            // Then
            assertThat(urlValue).isNotNull();
            assertThat(urlValue).isNotEmpty();
        }

        @Test
        @DisplayName("equals()는 ID 기반으로 동작하며 객체 체이닝 없음")
        void shouldImplementEqualsBasedOnIdWithoutChaining() {
            // Given
            CrawlTask task1 = CrawlTaskFixture.createWithId(1L);
            CrawlTask task2 = CrawlTaskFixture.createWithId(1L);
            CrawlTask task3 = CrawlTaskFixture.createWithId(2L);

            // When & Then
            assertThat(task1).isEqualTo(task2);
            assertThat(task1).isNotEqualTo(task3);
        }

        @Test
        @DisplayName("hashCode()는 ID 기반으로 동작하며 객체 체이닝 없음")
        void shouldImplementHashCodeBasedOnIdWithoutChaining() {
            // Given
            CrawlTask task1 = CrawlTaskFixture.createWithId(1L);
            CrawlTask task2 = CrawlTaskFixture.createWithId(1L);

            // When & Then
            assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("페이지 번호가 0일 때도 정상 생성")
        void shouldCreateTaskWithZeroPageNumber() {
            // Given
            Integer pageNumber = 0;

            // When
            CrawlTask task = CrawlTask.forNew(
                MustitSellerId.of(100L),
                TaskType.MINI_SHOP,
                RequestUrlFixture.create(),
                pageNumber,
                "key",
                LocalDateTime.now()
            );

            // Then
            assertThat(task.getPageNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("페이지 번호가 매우 큰 값일 때도 정상 생성")
        void shouldCreateTaskWithLargePageNumber() {
            // Given
            Integer pageNumber = Integer.MAX_VALUE;

            // When
            CrawlTask task = CrawlTask.forNew(
                MustitSellerId.of(100L),
                TaskType.MINI_SHOP,
                RequestUrlFixture.create(),
                pageNumber,
                "key",
                LocalDateTime.now()
            );

            // Then
            assertThat(task.getPageNumber()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("멱등성 키가 매우 긴 문자열일 때도 정상 생성")
        void shouldCreateTaskWithVeryLongIdempotencyKey() {
            // Given
            String longKey = "a".repeat(1000);

            // When
            CrawlTask task = CrawlTask.forNew(
                MustitSellerId.of(100L),
                TaskType.MINI_SHOP,
                RequestUrlFixture.create(),
                1,
                longKey,
                LocalDateTime.now()
            );

            // Then
            assertThat(task.getIdempotencyKey()).hasSize(1000);
        }

        @Test
        @DisplayName("scheduledAt이 과거 시간일 때도 정상 생성")
        void shouldCreateTaskWithPastScheduledTime() {
            // Given
            LocalDateTime pastTime = LocalDateTime.now().minusYears(1);

            // When
            CrawlTask task = CrawlTask.forNew(
                MustitSellerId.of(100L),
                TaskType.MINI_SHOP,
                RequestUrlFixture.create(),
                1,
                "key",
                pastTime
            );

            // Then
            assertThat(task.getScheduledAt()).isEqualTo(pastTime);
        }

        @Test
        @DisplayName("scheduledAt이 미래 시간일 때도 정상 생성")
        void shouldCreateTaskWithFutureScheduledTime() {
            // Given
            LocalDateTime futureTime = LocalDateTime.now().plusYears(1);

            // When
            CrawlTask task = CrawlTask.forNew(
                MustitSellerId.of(100L),
                TaskType.MINI_SHOP,
                RequestUrlFixture.create(),
                1,
                "key",
                futureTime
            );

            // Then
            assertThat(task.getScheduledAt()).isEqualTo(futureTime);
        }
    }
}
