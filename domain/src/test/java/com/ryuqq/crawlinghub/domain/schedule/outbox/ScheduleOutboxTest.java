package com.ryuqq.crawlinghub.domain.schedule.outbox;

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
 * ScheduleOutbox Domain 모델 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("ScheduleOutbox Domain 모델 단위 테스트")
class ScheduleOutboxTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("EventBridge 등록을 위한 Outbox 생성 성공 (PENDING/PENDING 상태, 재시도 0회, maxRetries=3)")
        void shouldCreateForEventBridgeRegistration() {
            // Given
            Long sellerId = 12345L;
            String payload = "{\"schedule\":\"0 0 * * * ?\"}";
            String idemKey = "schedule-reg-12345-v1";

            // When
            ScheduleOutbox outbox = ScheduleOutbox.forEventBridgeRegistration(sellerId, payload, idemKey);

            // Then
            assertThat(outbox).isNotNull();
            assertThat(outbox.getId()).isNull(); // 저장 전에는 ID 없음
            assertThat(outbox.getOpId()).isNull(); // 처리 시작 전에는 opId 없음
            assertThat(outbox.getSellerId()).isEqualTo(12345L);
            assertThat(outbox.getIdemKey()).isEqualTo("schedule-reg-12345-v1");
            assertThat(outbox.getDomain()).isEqualTo("SELLER_CRAWL_SCHEDULE");
            assertThat(outbox.getEventType()).isEqualTo("EVENTBRIDGE_REGISTER");
            assertThat(outbox.getBizKey()).isEqualTo("schedule-12345");
            assertThat(outbox.getPayload()).isEqualTo("{\"schedule\":\"0 0 * * * ?\"}");
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.PENDING);
            assertThat(outbox.getWalState()).isEqualTo(ScheduleOutbox.WriteAheadState.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
            assertThat(outbox.getMaxRetries()).isEqualTo(3);
            assertThat(outbox.getTimeoutMillis()).isEqualTo(60000L);
            assertThat(outbox.getCompletedAt()).isNull();
            assertThat(outbox.getCreatedAt()).isNotNull();
            assertThat(outbox.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("EventBridge 수정을 위한 Outbox 생성 성공 (eventType=EVENTBRIDGE_UPDATE)")
        void shouldCreateForEventBridgeUpdate() {
            // Given
            Long sellerId = 12345L;
            String payload = "{\"schedule\":\"0 0 12 * * ?\"}";
            String idemKey = "schedule-upd-12345-v2";

            // When
            ScheduleOutbox outbox = ScheduleOutbox.forEventBridgeUpdate(sellerId, payload, idemKey);

            // Then
            assertThat(outbox).isNotNull();
            assertThat(outbox.getEventType()).isEqualTo("EVENTBRIDGE_UPDATE");
            assertThat(outbox.getSellerId()).isEqualTo(12345L);
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("DB reconstitute로 Outbox 복원 성공")
        void shouldReconstituteFromDatabase() {
            // Given
            Long id = 100L;
            String opId = "op-123-uuid";
            Long sellerId = 12345L;
            String idemKey = "schedule-reg-12345-v1";
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime updatedAt = LocalDateTime.now();

            // When
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                id, opId, sellerId, idemKey,
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "schedule-12345",
                "{\"schedule\":\"0 0 * * * ?\"}", null,
                ScheduleOutbox.OperationState.IN_PROGRESS,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 1, 3, 60000L, null, createdAt, updatedAt
            );

            // Then
            assertThat(outbox).isNotNull();
            assertThat(outbox.getId()).isEqualTo(100L);
            assertThat(outbox.getOpId()).isEqualTo("op-123-uuid");
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.IN_PROGRESS);
            assertThat(outbox.getRetryCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("sellerId가 null이면 등록용 Outbox 생성 시 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNullForRegistration() {
            // When & Then
            assertThatThrownBy(() -> ScheduleOutbox.forEventBridgeRegistration(
                null, "{\"schedule\":\"0 0 * * * ?\"}", "idemKey"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Seller ID는 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("payload가 null/빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenPayloadIsNullOrBlank(String invalidPayload) {
            // When & Then
            assertThatThrownBy(() -> ScheduleOutbox.forEventBridgeRegistration(
                12345L, invalidPayload, "idemKey"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payload는 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("idemKey가 null/빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenIdemKeyIsNullOrBlank(String invalidIdemKey) {
            // When & Then
            assertThatThrownBy(() -> ScheduleOutbox.forEventBridgeRegistration(
                12345L, "{\"schedule\":\"0 0 * * * ?\"}", invalidIdemKey
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Idempotency Key는 필수입니다");
        }

        @Test
        @DisplayName("DB reconstitute 시 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullForReconstitute() {
            // When & Then
            assertThatThrownBy(() -> ScheduleOutbox.reconstitute(
                null, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.PENDING,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 0, 3, 60000L, null, LocalDateTime.now(), LocalDateTime.now()
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }
    }

    @Nested
    @DisplayName("상태 전이 테스트")
    class StatusTransitionTests {

        @Test
        @DisplayName("PENDING → IN_PROGRESS 전이 성공 (startProcessing 호출 시 opId 자동 생성)")
        void shouldTransitionFromPendingToInProgress() {
            // Given: PENDING 상태의 Outbox
            ScheduleOutbox outbox = ScheduleOutbox.forEventBridgeRegistration(
                12345L, "{\"schedule\":\"0 0 * * * ?\"}", "idemKey"
            );

            // When: 처리 시작
            outbox.startProcessing();

            // Then: IN_PROGRESS 상태 + opId 생성
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.IN_PROGRESS);
            assertThat(outbox.getOpId()).isNotNull();
            assertThat(outbox.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 startProcessing 호출 시 예외 발생")
        void shouldThrowExceptionWhenStartProcessingFromNonPending() {
            // Given: IN_PROGRESS 상태의 Outbox
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.IN_PROGRESS,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 0, 3, 60000L, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When & Then
            assertThatThrownBy(outbox::startProcessing)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING 상태에서만 처리 시작 가능합니다");
        }

        @Test
        @DisplayName("markCompleted 호출 시 COMPLETED/COMPLETED 상태 전이 + completedAt 기록")
        void shouldMarkCompletedSuccessfully() {
            // Given: IN_PROGRESS 상태의 Outbox
            ScheduleOutbox outbox = ScheduleOutbox.forEventBridgeRegistration(
                12345L, "{\"schedule\":\"0 0 * * * ?\"}", "idemKey"
            );
            outbox.startProcessing();

            // When: 완료 처리
            outbox.markCompleted();

            // Then: COMPLETED 상태 + completedAt 기록
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.COMPLETED);
            assertThat(outbox.getWalState()).isEqualTo(ScheduleOutbox.WriteAheadState.COMPLETED);
            assertThat(outbox.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("recordFailure 호출 시 FAILED 상태 전이 + retryCount 증가")
        void shouldRecordFailureSuccessfully() {
            // Given: IN_PROGRESS 상태의 Outbox
            ScheduleOutbox outbox = ScheduleOutbox.forEventBridgeRegistration(
                12345L, "{\"schedule\":\"0 0 * * * ?\"}", "idemKey"
            );
            outbox.startProcessing();

            // When: 실패 기록
            outbox.recordFailure("네트워크 오류");

            // Then: FAILED 상태 + retryCount 증가
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.FAILED);
            assertThat(outbox.getErrorMessage()).isEqualTo("네트워크 오류");
            assertThat(outbox.getRetryCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("재시도 로직 테스트")
    class RetryTests {

        @Test
        @DisplayName("FAILED 상태이고 retryCount < maxRetries이면 재시도 가능")
        void shouldAllowRetryWhenFailedAndBelowMaxRetries() {
            // Given: FAILED 상태 + retryCount=2
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.FAILED,
                ScheduleOutbox.WriteAheadState.PENDING,
                "에러", 2, 3, 60000L, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When & Then
            assertThat(outbox.canRetry()).isTrue();
        }

        @Test
        @DisplayName("FAILED 상태이지만 retryCount >= maxRetries이면 재시도 불가")
        void shouldNotAllowRetryWhenExceededMaxRetries() {
            // Given: FAILED 상태 + retryCount=3 (maxRetries=3)
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.FAILED,
                ScheduleOutbox.WriteAheadState.PENDING,
                "에러", 3, 3, 60000L, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When & Then
            assertThat(outbox.canRetry()).isFalse();
        }

        @Test
        @DisplayName("COMPLETED 상태이면 재시도 불가")
        void shouldNotAllowRetryWhenCompleted() {
            // Given: COMPLETED 상태
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.COMPLETED,
                ScheduleOutbox.WriteAheadState.COMPLETED,
                null, 0, 3, 60000L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
            );

            // When & Then
            assertThat(outbox.canRetry()).isFalse();
        }

        @Test
        @DisplayName("resetForRetry 호출 시 FAILED → PENDING 전이 + errorMessage 제거")
        void shouldResetForRetrySuccessfully() {
            // Given: FAILED 상태 + 재시도 가능
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.FAILED,
                ScheduleOutbox.WriteAheadState.PENDING,
                "에러", 1, 3, 60000L, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When: 재시도 재설정
            outbox.resetForRetry();

            // Then: PENDING 상태 + errorMessage 제거
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.PENDING);
            assertThat(outbox.getErrorMessage()).isNull();
            assertThat(outbox.getRetryCount()).isEqualTo(1); // retryCount는 그대로 유지
        }

        @Test
        @DisplayName("재시도 불가능한 상태에서 resetForRetry 호출 시 예외 발생")
        void shouldThrowExceptionWhenResetForRetryNotAllowed() {
            // Given: FAILED 상태 + retryCount >= maxRetries
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.FAILED,
                ScheduleOutbox.WriteAheadState.PENDING,
                "에러", 3, 3, 60000L, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When & Then
            assertThatThrownBy(outbox::resetForRetry)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재시도 불가");
        }
    }

    @Nested
    @DisplayName("타임아웃 처리 테스트")
    class TimeoutTests {

        @Test
        @DisplayName("생성 후 60초(타임아웃) 경과하지 않으면 타임아웃 아님")
        void shouldNotBeTimeoutWithinThreshold() {
            // Given: 방금 생성된 Outbox (타임아웃 60초)
            ScheduleOutbox outbox = ScheduleOutbox.forEventBridgeRegistration(
                12345L, "{\"schedule\":\"0 0 * * * ?\"}", "idemKey"
            );

            // When & Then
            assertThat(outbox.isTimeout()).isFalse();
        }

        @Test
        @DisplayName("생성 후 60초(타임아웃) 경과하면 타임아웃")
        void shouldBeTimeoutAfterThreshold() {
            // Given: 70초 전에 생성된 Outbox (타임아웃 60초)
            LocalDateTime createdAt = LocalDateTime.now().minusSeconds(70);
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, null, 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.PENDING,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 0, 3, 60000L, null, createdAt, createdAt
            );

            // When & Then
            assertThat(outbox.isTimeout()).isTrue();
        }

        @Test
        @DisplayName("markTimeout 호출 시 FAILED 상태 전이 + 타임아웃 에러 메시지 기록")
        void shouldMarkTimeoutSuccessfully() {
            // Given: 타임아웃 발생한 Outbox
            LocalDateTime createdAt = LocalDateTime.now().minusSeconds(70);
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, null, 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.PENDING,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 0, 3, 60000L, null, createdAt, createdAt
            );

            // When: 타임아웃 처리
            outbox.markTimeout();

            // Then: FAILED 상태 + 타임아웃 메시지
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.FAILED);
            assertThat(outbox.getErrorMessage()).contains("타임아웃");
            assertThat(outbox.getErrorMessage()).contains("60000ms");
        }
    }

    @Nested
    @DisplayName("정리(Cleanup) 대상 판별 테스트")
    class CleanupTests {

        @Test
        @DisplayName("완료 후 24시간 경과하지 않으면 정리 대상 아님")
        void shouldNotBeOldEnoughWithinRetention() {
            // Given: 12시간 전에 완료된 Outbox (보관 기간 24시간)
            LocalDateTime completedAt = LocalDateTime.now().minusHours(12);
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.COMPLETED,
                ScheduleOutbox.WriteAheadState.COMPLETED,
                null, 0, 3, 60000L, completedAt, LocalDateTime.now().minusHours(13), LocalDateTime.now().minusHours(12)
            );

            // When & Then
            assertThat(outbox.isOldEnough(24)).isFalse();
        }

        @Test
        @DisplayName("완료 후 24시간 경과하면 정리 대상")
        void shouldBeOldEnoughAfterRetention() {
            // Given: 30시간 전에 완료된 Outbox (보관 기간 24시간)
            LocalDateTime completedAt = LocalDateTime.now().minusHours(30);
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.COMPLETED,
                ScheduleOutbox.WriteAheadState.COMPLETED,
                null, 0, 3, 60000L, completedAt, LocalDateTime.now().minusHours(31), LocalDateTime.now().minusHours(30)
            );

            // When & Then
            assertThat(outbox.isOldEnough(24)).isTrue();
        }

        @Test
        @DisplayName("completedAt이 null이면 정리 대상 아님")
        void shouldNotBeOldEnoughWhenCompletedAtIsNull() {
            // Given: PENDING 상태 (completedAt = null)
            ScheduleOutbox outbox = ScheduleOutbox.forEventBridgeRegistration(
                12345L, "{\"schedule\":\"0 0 * * * ?\"}", "idemKey"
            );

            // When & Then
            assertThat(outbox.isOldEnough(24)).isFalse();
        }
    }

    @Nested
    @DisplayName("equals/hashCode 테스트")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("같은 ID를 가진 두 ScheduleOutbox는 equals()가 true")
        void shouldBeEqualForSameId() {
            // Given: 같은 ID
            LocalDateTime now = LocalDateTime.now();
            ScheduleOutbox outbox1 = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idem1",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "biz1",
                "{}", null,
                ScheduleOutbox.OperationState.PENDING,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 0, 3, 60000L, null, now, now
            );
            ScheduleOutbox outbox2 = ScheduleOutbox.reconstitute(
                100L, "opId2", 67890L, "idem2",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_UPDATE", "biz2",
                "{}", null,
                ScheduleOutbox.OperationState.COMPLETED,
                ScheduleOutbox.WriteAheadState.COMPLETED,
                null, 0, 3, 60000L, null, now, now
            );

            // When & Then: ID만 같으면 equals
            assertThat(outbox1).isEqualTo(outbox2);
        }

        @Test
        @DisplayName("다른 ID를 가진 두 ScheduleOutbox는 equals()가 false")
        void shouldNotBeEqualForDifferentId() {
            // Given: 다른 ID
            LocalDateTime now = LocalDateTime.now();
            ScheduleOutbox outbox1 = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.PENDING,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 0, 3, 60000L, null, now, now
            );
            ScheduleOutbox outbox2 = ScheduleOutbox.reconstitute(
                200L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.PENDING,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 0, 3, 60000L, null, now, now
            );

            // When & Then
            assertThat(outbox1).isNotEqualTo(outbox2);
        }

        @Test
        @DisplayName("같은 ID를 가진 두 ScheduleOutbox는 같은 hashCode를 반환한다")
        void shouldReturnSameHashCodeForSameId() {
            // Given: 같은 ID
            LocalDateTime now = LocalDateTime.now();
            ScheduleOutbox outbox1 = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.PENDING,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 0, 3, 60000L, null, now, now
            );
            ScheduleOutbox outbox2 = ScheduleOutbox.reconstitute(
                100L, "opId2", 67890L, "idemKey2",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_UPDATE", "bizKey2",
                "{}", null,
                ScheduleOutbox.OperationState.COMPLETED,
                ScheduleOutbox.WriteAheadState.COMPLETED,
                null, 0, 3, 60000L, null, now, now
            );

            // When & Then
            assertThat(outbox1.hashCode()).isEqualTo(outbox2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 주요 필드를 포함한다")
        void shouldIncludeMainFieldsInToString() {
            // Given
            ScheduleOutbox outbox = ScheduleOutbox.reconstitute(
                100L, "opId", 12345L, "idemKey",
                "SELLER_CRAWL_SCHEDULE", "EVENTBRIDGE_REGISTER", "bizKey",
                "{}", null,
                ScheduleOutbox.OperationState.IN_PROGRESS,
                ScheduleOutbox.WriteAheadState.PENDING,
                null, 2, 3, 60000L, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When
            String result = outbox.toString();

            // Then
            assertThat(result).contains("id=100");
            assertThat(result).contains("sellerId=12345");
            assertThat(result).contains("idemKey='idemKey'");
            assertThat(result).contains("operationState=IN_PROGRESS");
            assertThat(result).contains("walState=PENDING");
            assertThat(result).contains("retryCount=2");
        }
    }

    @Nested
    @DisplayName("통합 시나리오: Outbox 생성 → 실패 → 재시도 → 성공")
    class IntegratedScenario {

        @Test
        @DisplayName("전체 Outbox 생명 주기 시나리오 (생성 → 처리 → 실패 → 재시도 → 성공 → 정리)")
        void shouldCompleteFullLifecycle() throws InterruptedException {
            // Given: 1. Outbox 생성 (PENDING)
            ScheduleOutbox outbox = ScheduleOutbox.forEventBridgeRegistration(
                12345L, "{\"schedule\":\"0 0 * * * ?\"}", "idemKey-lifecycle"
            );
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);

            // When: 2. 처리 시작 (PENDING → IN_PROGRESS)
            outbox.startProcessing();
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.IN_PROGRESS);
            assertThat(outbox.getOpId()).isNotNull();

            // Then: 3. 첫 번째 실패 (IN_PROGRESS → FAILED)
            outbox.recordFailure("네트워크 오류");
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.FAILED);
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.canRetry()).isTrue();

            // When: 4. 재시도 준비 (FAILED → PENDING)
            outbox.resetForRetry();
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.PENDING);
            assertThat(outbox.getErrorMessage()).isNull();

            // Then: 5. 두 번째 처리 시작
            outbox.startProcessing();
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.IN_PROGRESS);

            // When: 6. 성공 처리 (IN_PROGRESS → COMPLETED)
            outbox.markCompleted();
            assertThat(outbox.getOperationState()).isEqualTo(ScheduleOutbox.OperationState.COMPLETED);
            assertThat(outbox.getWalState()).isEqualTo(ScheduleOutbox.WriteAheadState.COMPLETED);
            assertThat(outbox.getCompletedAt()).isNotNull();

            // Then: 7. 정리 대상 확인 (24시간 미경과)
            assertThat(outbox.isOldEnough(24)).isFalse();

            // 8. (시뮬레이션) DB 저장 후 24시간 경과한 상태의 Outbox reconstitute
            // reconstitute()는 DB에서 조회한 상태를 재구성하므로 ID 필수
            ScheduleOutbox oldOutbox = ScheduleOutbox.reconstitute(
                1L, // DB에 저장 후 부여된 ID (시뮬레이션)
                outbox.getOpId(), outbox.getSellerId(), outbox.getIdemKey(),
                outbox.getDomain(), outbox.getEventType(), outbox.getBizKey(),
                outbox.getPayload(), outbox.getOutcomeJson(),
                outbox.getOperationState(), outbox.getWalState(),
                outbox.getErrorMessage(), outbox.getRetryCount(), outbox.getMaxRetries(),
                outbox.getTimeoutMillis(),
                LocalDateTime.now().minusHours(30), // 30시간 전에 완료
                outbox.getCreatedAt(), outbox.getUpdatedAt()
            );
            assertThat(oldOutbox.isOldEnough(24)).isTrue();
        }
    }
}
