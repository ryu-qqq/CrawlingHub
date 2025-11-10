package com.ryuqq.crawlinghub.domain.task;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Task Aggregate Root 단위 테스트
 *
 * <p>테스트 범위:
 * <ul>
 *   <li>Static Factory 메서드: forNew, of, forMeta, forMiniShop, forProductDetail, forProductOption, reconstitute</li>
 *   <li>상태 전환: publish, startProcessing, completeSuccessfully, failWithError</li>
 *   <li>Retry 로직: canRetry, incrementRetry (MAX_RETRY_COUNT=3)</li>
 *   <li>Timeout 처리: isTimeout (10분 기준)</li>
 *   <li>Query 메서드: hasStatus, isCompleted, isFailed</li>
 *   <li>Validation: 필수 필드 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("Task Aggregate Root 단위 테스트")
class TaskTest {

    // 고정된 Clock (테스트 안정성)
    private static final Clock FIXED_CLOCK = Clock.fixed(
        LocalDateTime.of(2025, 11, 7, 15, 0, 0).atZone(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    );

    // 공통 테스트 데이터
    private static final MustitSellerId SELLER_ID = MustitSellerId.of(12345L);
    private static final SellerName SELLER_NAME = SellerName.of("LIKEASTAR");
    private static final LocalDateTime SCHEDULED_AT = LocalDateTime.now(FIXED_CLOCK);

    @Nested
    @DisplayName("Static Factory 메서드 테스트")
    class StaticFactoryTests {

        @Test
        @DisplayName("forNew()로 신규 Task 생성 성공 (ID 없음)")
        void shouldCreateNewTaskWithoutId() {
            // Given
            String url = "https://example.com/api";
            String idempotencyKey = "META_LIKEASTAR";

            // When
            Task task = Task.forNew(
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                RequestUrl.of(url),
                0,
                idempotencyKey,
                100L,  // crawlScheduleId
                TriggerType.AUTO,
                SCHEDULED_AT
            );

            // Then
            assertThat(task).isNotNull();
            assertThat(task.getIdValue()).isNull();  // forNew()는 ID 없음
            assertThat(task.getSellerIdValue()).isEqualTo(SELLER_ID.value());
            assertThat(task.getSellerName()).isEqualTo(SELLER_NAME);
            assertThat(task.getTaskType()).isEqualTo(TaskType.META);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);
            assertThat(task.getRetryCount()).isZero();
            assertThat(task.getIdempotencyKey()).isEqualTo(idempotencyKey);
            assertThat(task.getCrawlScheduleId()).isEqualTo(100L);
            assertThat(task.getTriggerType()).isEqualTo(TriggerType.AUTO);
        }

        @Test
        @DisplayName("of()로 기존 Task 생성 성공 (ID 있음)")
        void shouldCreateExistingTaskWithId() {
            // Given
            TaskId taskId = TaskId.of(1L);
            String url = "https://example.com/api";
            String idempotencyKey = "MINI_SHOP_LIKEASTAR_0_10";

            // When
            Task task = Task.of(
                taskId,
                SELLER_ID,
                SELLER_NAME,
                TaskType.MINI_SHOP,
                RequestUrl.of(url),
                0,
                idempotencyKey,
                SCHEDULED_AT
            );

            // Then
            assertThat(task).isNotNull();
            assertThat(task.getIdValue()).isEqualTo(taskId.value());
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);
            assertThat(task.getCrawlScheduleId()).isNull();  // of()는 null
            assertThat(task.getTriggerType()).isEqualTo(TriggerType.MANUAL);  // of()는 MANUAL
        }

        @Test
        @DisplayName("of() 호출 시 null TaskId는 예외 발생")
        void shouldThrowExceptionWhenOfWithNullId() {
            // When & Then
            assertThatThrownBy(() -> Task.of(
                null,  // null TaskId
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                RequestUrl.of("https://example.com/api"),
                0,
                "META_LIKEASTAR",
                SCHEDULED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task ID는 필수입니다");
        }

        @Test
        @DisplayName("forMeta()로 META Task 생성 성공")
        void shouldCreateMetaTask() {
            // When
            Task task = Task.forMeta(
                SELLER_ID,
                SELLER_NAME,
                100L,  // crawlScheduleId
                TriggerType.AUTO,
                SCHEDULED_AT
            );

            // Then
            assertThat(task).isNotNull();
            assertThat(task.getTaskType()).isEqualTo(TaskType.META);
            assertThat(task.getPageNumber()).isZero();  // META는 pageNo=0
            assertThat(task.getIdempotencyKey()).isEqualTo("META_LIKEASTAR");
            assertThat(task.getRequestUrlValue())
                .contains("pageNo=0")
                .contains("pageSize=1")
                .contains("sellerId=" + SELLER_NAME.getValue());
        }

        @Test
        @DisplayName("forMiniShop()로 MINI_SHOP Task 생성 성공")
        void shouldCreateMiniShopTask() {
            // Given
            int pageNo = 2;
            int pageSize = 50;

            // When
            Task task = Task.forMiniShop(
                SELLER_ID,
                SELLER_NAME,
                pageNo,
                pageSize,
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT
            );

            // Then
            assertThat(task).isNotNull();
            assertThat(task.getTaskType()).isEqualTo(TaskType.MINI_SHOP);
            assertThat(task.getPageNumber()).isEqualTo(pageNo);
            assertThat(task.getIdempotencyKey()).isEqualTo("MINI_SHOP_LIKEASTAR_2_50");
            assertThat(task.getRequestUrlValue())
                .contains("pageNo=2")
                .contains("pageSize=50")
                .contains("sellerId=" + SELLER_NAME.getValue());
        }

        @Test
        @DisplayName("forProductDetail()로 PRODUCT_DETAIL Task 생성 성공")
        void shouldCreateProductDetailTask() {
            // Given
            Long itemNo = 12345L;

            // When
            Task task = Task.forProductDetail(
                SELLER_ID,
                SELLER_NAME,
                itemNo,
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT
            );

            // Then
            assertThat(task).isNotNull();
            assertThat(task.getTaskType()).isEqualTo(TaskType.PRODUCT_DETAIL);
            assertThat(task.getIdempotencyKey()).isEqualTo("PRODUCT_DETAIL_LIKEASTAR_12345");
            assertThat(task.getRequestUrlValue())
                .contains("/searchitem/12345");
        }

        @Test
        @DisplayName("forProductOption()로 PRODUCT_OPTION Task 생성 성공")
        void shouldCreateProductOptionTask() {
            // Given
            Long itemNo = 12345L;

            // When
            Task task = Task.forProductOption(
                SELLER_ID,
                SELLER_NAME,
                itemNo,
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT
            );

            // Then
            assertThat(task).isNotNull();
            assertThat(task.getTaskType()).isEqualTo(TaskType.PRODUCT_OPTION);
            assertThat(task.getIdempotencyKey()).isEqualTo("PRODUCT_OPTION_LIKEASTAR_12345");
            assertThat(task.getRequestUrlValue())
                .contains("/searchitem/12345/option");
        }

        @Test
        @DisplayName("reconstitute()로 DB 상태 재구성 성공")
        void shouldReconstituteDatabaseState() {
            // Given: DB에서 조회한 상태 시뮬레이션
            TaskId taskId = TaskId.of(1L);
            LocalDateTime startedAt = LocalDateTime.now(FIXED_CLOCK).minusMinutes(5);
            LocalDateTime createdAt = LocalDateTime.now(FIXED_CLOCK).minusHours(1);

            // When
            Task task = Task.reconstitute(
                taskId,
                SELLER_ID,
                SELLER_NAME,
                TaskType.MINI_SHOP,
                TaskStatus.RUNNING,  // DB 저장된 상태
                RequestUrl.of("https://example.com/api"),
                2,  // pageNumber
                1,  // retryCount
                "MINI_SHOP_LIKEASTAR_2_50",
                100L,  // crawlScheduleId
                TriggerType.AUTO,
                SCHEDULED_AT,
                startedAt,
                null,  // completedAt
                createdAt,
                LocalDateTime.now(FIXED_CLOCK)
            );

            // Then
            assertThat(task).isNotNull();
            assertThat(task.getIdValue()).isEqualTo(taskId.value());
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
            assertThat(task.getRetryCount()).isEqualTo(1);
            assertThat(task.getStartedAt()).isEqualTo(startedAt);
            assertThat(task.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("reconstitute() 호출 시 null TaskId는 예외 발생")
        void shouldThrowExceptionWhenReconstituteWithNullId() {
            // When & Then
            assertThatThrownBy(() -> Task.reconstitute(
                null,  // null TaskId
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.WAITING,
                RequestUrl.of("https://example.com/api"),
                0,
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                null,
                null,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }
    }

    @Nested
    @DisplayName("필수 필드 검증 테스트")
    class ValidationTests {

        @ParameterizedTest
        @NullSource
        @DisplayName("null sellerId로 Task 생성 시 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNull(MustitSellerId nullSellerId) {
            // When & Then
            assertThatThrownBy(() -> Task.forNew(
                nullSellerId,
                SELLER_NAME,
                TaskType.META,
                RequestUrl.of("https://example.com/api"),
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 ID는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null sellerName로 Task 생성 시 예외 발생")
        void shouldThrowExceptionWhenSellerNameIsNull(SellerName nullSellerName) {
            // When & Then
            assertThatThrownBy(() -> Task.forNew(
                SELLER_ID,
                nullSellerName,
                TaskType.META,
                RequestUrl.of("https://example.com/api"),
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null taskType로 Task 생성 시 예외 발생")
        void shouldThrowExceptionWhenTaskTypeIsNull(TaskType nullTaskType) {
            // When & Then
            assertThatThrownBy(() -> Task.forNew(
                SELLER_ID,
                SELLER_NAME,
                nullTaskType,
                RequestUrl.of("https://example.com/api"),
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("작업 유형은 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null requestUrl로 Task 생성 시 예외 발생")
        void shouldThrowExceptionWhenRequestUrlIsNull(RequestUrl nullRequestUrl) {
            // When & Then
            assertThatThrownBy(() -> Task.forNew(
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                nullRequestUrl,
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("요청 URL은 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null idempotencyKey로 Task 생성 시 예외 발생")
        void shouldThrowExceptionWhenIdempotencyKeyIsNull(String nullIdempotencyKey) {
            // When & Then
            assertThatThrownBy(() -> Task.forNew(
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                RequestUrl.of("https://example.com/api"),
                0,
                nullIdempotencyKey,
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("멱등성 키는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null scheduledAt로 Task 생성 시 예외 발생")
        void shouldThrowExceptionWhenScheduledAtIsNull(LocalDateTime nullScheduledAt) {
            // When & Then
            assertThatThrownBy(() -> Task.forNew(
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                RequestUrl.of("https://example.com/api"),
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                nullScheduledAt
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약 시간은 필수입니다");
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트")
    class StateTransitionTests {

        @Test
        @DisplayName("publish() - WAITING → PUBLISHED 상태 전환 성공")
        void shouldTransitionFromWaitingToPublished() {
            // Given
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);

            // When
            task.publish();

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PUBLISHED);
        }

        @Test
        @DisplayName("publish() - WAITING이 아닌 상태에서 호출 시 예외 발생")
        void shouldThrowExceptionWhenPublishFromNonWaitingStatus() {
            // Given: PUBLISHED 상태의 Task
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            task.publish();  // WAITING → PUBLISHED
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PUBLISHED);

            // When & Then
            assertThatThrownBy(task::publish)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WAITING 상태에서만 발행할 수 있습니다");
        }

        @Test
        @DisplayName("startProcessing() - PUBLISHED → RUNNING 상태 전환 성공")
        void shouldTransitionFromPublishedToRunning() {
            // Given
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            task.publish();  // WAITING → PUBLISHED
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PUBLISHED);
            assertThat(task.getStartedAt()).isNull();

            // When
            task.startProcessing();

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
            assertThat(task.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("startProcessing() - RETRY → RUNNING 상태 전환 성공")
        void shouldTransitionFromRetryToRunning() {
            // Given: RETRY 상태의 Task (시뮬레이션)
            Task task = Task.reconstitute(
                TaskId.of(1L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.RETRY,  // RETRY 상태
                RequestUrl.of("https://example.com/api"),
                0,
                1,  // retryCount
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                null,  // startedAt
                null,  // completedAt
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When
            task.startProcessing();

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
            assertThat(task.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("startProcessing() - PUBLISHED/RETRY가 아닌 상태에서 호출 시 예외 발생")
        void shouldThrowExceptionWhenStartProcessingFromInvalidStatus() {
            // Given: WAITING 상태의 Task
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);

            // When & Then
            assertThatThrownBy(task::startProcessing)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PUBLISHED 또는 RETRY 상태에서만 시작할 수 있습니다");
        }

        @Test
        @DisplayName("completeSuccessfully() - RUNNING → SUCCESS 상태 전환 성공")
        void shouldTransitionFromRunningToSuccess() {
            // Given
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            task.publish();
            task.startProcessing();  // WAITING → PUBLISHED → RUNNING
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
            assertThat(task.getCompletedAt()).isNull();

            // When
            task.completeSuccessfully();

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.SUCCESS);
            assertThat(task.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("completeSuccessfully() - RUNNING이 아닌 상태에서 호출 시 예외 발생")
        void shouldThrowExceptionWhenCompleteSuccessfullyFromNonRunningStatus() {
            // Given: WAITING 상태의 Task
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);

            // When & Then
            assertThatThrownBy(task::completeSuccessfully)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RUNNING 상태에서만 완료할 수 있습니다");
        }

        @Test
        @DisplayName("failWithError() - RUNNING → RETRY 상태 전환 성공 (retryCount < 3)")
        void shouldTransitionFromRunningToRetryWhenCanRetry() {
            // Given
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            task.publish();
            task.startProcessing();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
            assertThat(task.getRetryCount()).isZero();

            // When
            task.failWithError("Network timeout");

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RETRY);
            assertThat(task.getRetryCount()).isEqualTo(1);
            assertThat(task.getCompletedAt()).isNull();  // RETRY는 completedAt 없음
        }

        @Test
        @DisplayName("failWithError() - RUNNING → FAILED 상태 전환 성공 (retryCount >= 3)")
        void shouldTransitionFromRunningToFailedWhenCannotRetry() {
            // Given: retryCount가 이미 3인 Task (시뮬레이션)
            Task task = Task.reconstitute(
                TaskId.of(1L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.RUNNING,
                RequestUrl.of("https://example.com/api"),
                0,
                3,  // retryCount = 3 (MAX)
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                LocalDateTime.now(FIXED_CLOCK),
                null,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When
            task.failWithError("Permanent error");

            // Then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
            assertThat(task.getRetryCount()).isEqualTo(3);  // 증가하지 않음
            assertThat(task.getCompletedAt()).isNotNull();  // FAILED는 completedAt 설정
        }

        @Test
        @DisplayName("failWithError() - RUNNING이 아닌 상태에서 호출 시 예외 발생")
        void shouldThrowExceptionWhenFailWithErrorFromNonRunningStatus() {
            // Given: WAITING 상태의 Task
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);

            // When & Then
            assertThatThrownBy(() -> task.failWithError("Error"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RUNNING 상태에서만 실패 처리할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("Retry 로직 테스트")
    class RetryTests {

        @Test
        @DisplayName("canRetry() - retryCount < 3이면 true 반환")
        void shouldReturnTrueWhenRetryCountLessThanThree() {
            // Given
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            task.publish();
            task.startProcessing();
            assertThat(task.getRetryCount()).isZero();

            // When
            boolean canRetry = task.canRetry();

            // Then
            assertThat(canRetry).isTrue();
        }

        @Test
        @DisplayName("canRetry() - retryCount >= 3이면 false 반환")
        void shouldReturnFalseWhenRetryCountEqualsOrGreaterThanThree() {
            // Given: retryCount = 3인 Task
            Task task = Task.reconstitute(
                TaskId.of(1L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.RUNNING,
                RequestUrl.of("https://example.com/api"),
                0,
                3,  // MAX_RETRY_COUNT
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                LocalDateTime.now(FIXED_CLOCK),
                null,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When
            boolean canRetry = task.canRetry();

            // Then
            assertThat(canRetry).isFalse();
        }

        @Test
        @DisplayName("incrementRetry() - retryCount 증가 성공")
        void shouldIncrementRetryCount() {
            // Given
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            assertThat(task.getRetryCount()).isZero();

            // When
            task.incrementRetry();

            // Then
            assertThat(task.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("incrementRetry() - retryCount 최대 3회까지 증가")
        void shouldIncrementRetryCountUpToMaximum() {
            // Given
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);

            // When
            task.incrementRetry();  // 0 → 1
            task.incrementRetry();  // 1 → 2
            task.incrementRetry();  // 2 → 3

            // Then
            assertThat(task.getRetryCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Timeout 처리 테스트")
    class TimeoutTests {

        @Test
        @DisplayName("isTimeout() - RUNNING 상태에서 10분 초과 시 true 반환")
        void shouldReturnTrueWhenRunningOverTenMinutes() {
            // Given: 11분 전에 시작한 RUNNING Task
            Clock pastClock = Clock.fixed(
                LocalDateTime.now(FIXED_CLOCK).minusMinutes(11).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
            );

            Task task = Task.reconstitute(
                TaskId.of(1L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.RUNNING,
                RequestUrl.of("https://example.com/api"),
                0,
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                LocalDateTime.now(pastClock),  // 11분 전에 시작
                null,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When
            boolean isTimeout = task.isTimeout();

            // Then
            assertThat(isTimeout).isTrue();
        }

        @Test
        @DisplayName("isTimeout() - RUNNING 상태에서 10분 이내면 false 반환 (최근 startedAt)")
        void shouldReturnFalseWhenRunningWithinTenMinutes() {
            // Given: 현재 시간에 가까운 startedAt (타임아웃 아님)
            Task task = Task.reconstitute(
                TaskId.of(1L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.RUNNING,
                RequestUrl.of("https://example.com/api"),
                0,
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                LocalDateTime.now().minusMinutes(5),  // 5분 전 시작 (타임아웃 아님)
                null,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
            );

            // When
            boolean isTimeout = task.isTimeout();

            // Then
            assertThat(isTimeout).isFalse();
        }

        @Test
        @DisplayName("isTimeout() - RUNNING이 아닌 상태에서는 false 반환")
        void shouldReturnFalseWhenNotRunningStatus() {
            // Given: WAITING 상태의 Task
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);

            // When
            boolean isTimeout = task.isTimeout();

            // Then
            assertThat(isTimeout).isFalse();
        }

        @Test
        @DisplayName("isTimeout() - startedAt이 null이면 false 반환")
        void shouldReturnFalseWhenStartedAtIsNull() {
            // Given: RUNNING이지만 startedAt이 null인 Task (비정상 상태 시뮬레이션)
            Task task = Task.reconstitute(
                TaskId.of(1L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.RUNNING,
                RequestUrl.of("https://example.com/api"),
                0,
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                null,  // startedAt = null
                null,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When
            boolean isTimeout = task.isTimeout();

            // Then
            assertThat(isTimeout).isFalse();
        }
    }

    @Nested
    @DisplayName("Query 메서드 테스트")
    class QueryTests {

        @ParameterizedTest
        @EnumSource(TaskStatus.class)
        @DisplayName("hasStatus() - 모든 TaskStatus에 대해 정확한 비교")
        void shouldCheckStatusCorrectly(TaskStatus status) {
            // Given
            Task task = Task.reconstitute(
                TaskId.of(1L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                status,
                RequestUrl.of("https://example.com/api"),
                0,
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                null,
                null,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When
            boolean hasStatus = task.hasStatus(status);

            // Then
            assertThat(hasStatus).isTrue();
        }

        @Test
        @DisplayName("isCompleted() - SUCCESS 상태에서만 true 반환")
        void shouldReturnTrueOnlyForSuccessStatus() {
            // Given: SUCCESS 상태
            Task successTask = Task.reconstitute(
                TaskId.of(1L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.SUCCESS,
                RequestUrl.of("https://example.com/api"),
                0,
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When & Then
            assertThat(successTask.isCompleted()).isTrue();

            // Given: FAILED 상태
            Task failedTask = Task.reconstitute(
                TaskId.of(2L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.FAILED,
                RequestUrl.of("https://example.com/api"),
                0,
                3,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When & Then
            assertThat(failedTask.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("isFailed() - FAILED 상태에서만 true 반환")
        void shouldReturnTrueOnlyForFailedStatus() {
            // Given: FAILED 상태
            Task failedTask = Task.reconstitute(
                TaskId.of(1L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.FAILED,
                RequestUrl.of("https://example.com/api"),
                0,
                3,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When & Then
            assertThat(failedTask.isFailed()).isTrue();

            // Given: SUCCESS 상태
            Task successTask = Task.reconstitute(
                TaskId.of(2L),
                SELLER_ID,
                SELLER_NAME,
                TaskType.META,
                TaskStatus.SUCCESS,
                RequestUrl.of("https://example.com/api"),
                0,
                0,
                "META_LIKEASTAR",
                100L,
                TriggerType.AUTO,
                SCHEDULED_AT,
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK),
                LocalDateTime.now(FIXED_CLOCK)
            );

            // When & Then
            assertThat(successTask.isFailed()).isFalse();
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegratedScenarioTests {

        @Test
        @DisplayName("정상 흐름: WAITING → PUBLISHED → RUNNING → SUCCESS")
        void shouldCompleteNormalLifecycle() {
            // 1. Task 생성 (WAITING)
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.WAITING);
            assertThat(task.getRetryCount()).isZero();

            // 2. 발행 (WAITING → PUBLISHED)
            task.publish();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PUBLISHED);

            // 3. 처리 시작 (PUBLISHED → RUNNING)
            task.startProcessing();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
            assertThat(task.getStartedAt()).isNotNull();

            // 4. 성공 완료 (RUNNING → SUCCESS)
            task.completeSuccessfully();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.SUCCESS);
            assertThat(task.getCompletedAt()).isNotNull();
            assertThat(task.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("Retry 흐름: RUNNING → RETRY → RUNNING → SUCCESS")
        void shouldCompleteRetryLifecycle() {
            // 1. Task 생성 및 RUNNING까지 진행
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            task.publish();
            task.startProcessing();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);

            // 2. 첫 번째 실패 (RUNNING → RETRY)
            task.failWithError("Temporary network error");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RETRY);
            assertThat(task.getRetryCount()).isEqualTo(1);

            // 3. 재시도 (RETRY → RUNNING)
            task.startProcessing();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);

            // 4. 성공 완료 (RUNNING → SUCCESS)
            task.completeSuccessfully();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.SUCCESS);
            assertThat(task.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("최종 실패 흐름: 3회 Retry 후 FAILED")
        void shouldFailAfterThreeRetries() {
            // 1. Task 생성 및 RUNNING까지 진행
            Task task = Task.forMeta(SELLER_ID, SELLER_NAME, 100L, TriggerType.AUTO, SCHEDULED_AT);
            task.publish();
            task.startProcessing();

            // 2. 첫 번째 실패 (retryCount: 0 → 1)
            task.failWithError("Error 1");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RETRY);
            assertThat(task.getRetryCount()).isEqualTo(1);

            // 3. 두 번째 실패 (retryCount: 1 → 2)
            task.startProcessing();
            task.failWithError("Error 2");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RETRY);
            assertThat(task.getRetryCount()).isEqualTo(2);

            // 4. 세 번째 실패 (retryCount: 2 → 3)
            task.startProcessing();
            task.failWithError("Error 3");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.RETRY);
            assertThat(task.getRetryCount()).isEqualTo(3);

            // 5. 네 번째 실패 → FAILED (더 이상 재시도 불가)
            task.startProcessing();
            task.failWithError("Final error");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
            assertThat(task.getRetryCount()).isEqualTo(3);  // 증가하지 않음
            assertThat(task.getCompletedAt()).isNotNull();
            assertThat(task.isFailed()).isTrue();
        }
    }
}
