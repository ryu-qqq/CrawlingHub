package com.ryuqq.crawlinghub.domain.change;

import com.ryuqq.crawlinghub.domain.product.ProductId;
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
 * ChangeDetection Domain 단위 테스트
 *
 * <p>테스트 범위:
 * <ul>
 *   <li>생성 테스트: forNew, of, reconstitute</li>
 *   <li>알림 전송 테스트: markAsSent, shouldNotify</li>
 *   <li>실패 처리 테스트: markAsFailed, MAX_RETRY_COUNT(3)</li>
 *   <li>중복 알림 방지 테스트: isDuplicateNotification (24시간)</li>
 *   <li>변경 메시지 생성 테스트: generateChangeMessage</li>
 *   <li>예외 케이스 테스트</li>
 *   <li>Law of Demeter 준수 테스트</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("ChangeDetection Domain 단위 테스트")
class ChangeDetectionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTests {

        @Test
        @DisplayName("유효한 입력으로 신규 ChangeDetection 생성 성공")
        void shouldCreateNewChangeDetectionWithValidInputs() {
            // Given
            ProductId productId = ProductId.of(100L);
            ChangeType changeType = ChangeType.PRICE;
            String previousHash = "hash-old-123";
            String currentHash = "hash-new-456";
            ChangeData changeDetails = ChangeDataFixture.create();

            // When
            ChangeDetection change = ChangeDetection.forNew(
                productId,
                changeType,
                previousHash,
                currentHash,
                changeDetails
            );

            // Then
            assertThat(change).isNotNull();
            assertThat(change.getIdValue()).isNull(); // 신규 생성이므로 ID 없음
            assertThat(change.getProductIdValue()).isEqualTo(100L);
            assertThat(change.getChangeType()).isEqualTo(ChangeType.PRICE);
            assertThat(change.getPreviousHash()).isEqualTo("hash-old-123");
            assertThat(change.getCurrentHash()).isEqualTo("hash-new-456");
            assertThat(change.getStatus()).isEqualTo(NotificationStatus.PENDING); // 초기 상태
            assertThat(change.getRetryCount()).isEqualTo(0); // 초기 재시도 횟수
            assertThat(change.getNotifiedAt()).isNull();
            assertThat(change.getFailureReason()).isNull();
        }

        @Test
        @DisplayName("ID를 가진 ChangeDetection 생성 성공 (of)")
        void shouldCreateChangeDetectionWithId() {
            // Given
            ChangeDetectionId changeId = ChangeDetectionId.of(1L);
            ProductId productId = ProductId.of(100L);
            ChangeType changeType = ChangeType.OPTION;
            String previousHash = "hash-old";
            String currentHash = "hash-new";
            ChangeData changeDetails = ChangeDataFixture.create();

            // When
            ChangeDetection change = ChangeDetection.of(
                changeId,
                productId,
                changeType,
                previousHash,
                currentHash,
                changeDetails
            );

            // Then
            assertThat(change.getIdValue()).isEqualTo(1L);
            assertThat(change.getStatus()).isEqualTo(NotificationStatus.PENDING);
        }

        @Test
        @DisplayName("DB reconstitute로 모든 필드 포함 ChangeDetection 생성 성공")
        void shouldReconstituteChangeDetectionFromDatabase() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.createSent();

            // When & Then
            assertThat(change.getIdValue()).isNotNull();
            assertThat(change.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(change.getNotifiedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("알림 전송 테스트")
    class NotificationTests {

        @Test
        @DisplayName("PENDING 상태에서 markAsSent() 호출 시 SENT 상태로 전이")
        void shouldTransitionToSentWhenMarkingAsSent() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();
            assertThat(change.getStatus()).isEqualTo(NotificationStatus.PENDING);

            // When
            change.markAsSent();

            // Then
            assertThat(change.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(change.getNotifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("SENT 상태에서 markAsSent() 호출 시 예외 발생")
        void shouldThrowExceptionWhenMarkingAlreadySentNotification() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.createSent();

            // When & Then
            assertThatThrownBy(change::markAsSent)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 전송된 알림입니다");
        }

        @Test
        @DisplayName("PENDING 상태일 때 shouldNotify() 는 true 반환")
        void shouldReturnTrueWhenPending() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();

            // When & Then
            assertThat(change.shouldNotify()).isTrue();
        }

        @Test
        @DisplayName("SENT 상태일 때 shouldNotify() 는 false 반환")
        void shouldReturnFalseWhenSent() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.createSent();

            // When & Then
            assertThat(change.shouldNotify()).isFalse();
        }

        @Test
        @DisplayName("FAILED 상태이고 재시도 횟수가 3회 미만이면 shouldNotify() 는 true 반환")
        void shouldReturnTrueWhenFailedButCanRetry() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.createFailed(2);

            // When & Then
            assertThat(change.shouldNotify()).isTrue();
        }

        @Test
        @DisplayName("FAILED 상태이고 재시도 횟수가 3회 이상이면 shouldNotify() 는 false 반환")
        void shouldReturnFalseWhenFailedAndCannotRetry() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.createFailed(3);

            // When & Then
            assertThat(change.shouldNotify()).isFalse();
        }
    }

    @Nested
    @DisplayName("실패 처리 테스트")
    class FailureHandlingTests {

        @Test
        @DisplayName("markAsFailed() 호출 시 FAILED 상태로 전이 및 retryCount 증가")
        void shouldTransitionToFailedAndIncrementRetryCount() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();
            assertThat(change.getRetryCount()).isEqualTo(0);

            // When
            change.markAsFailed("Connection timeout");

            // Then
            assertThat(change.getStatus()).isEqualTo(NotificationStatus.FAILED);
            assertThat(change.getRetryCount()).isEqualTo(1);
            assertThat(change.getFailureReason()).isEqualTo("Connection timeout");
        }

        @Test
        @DisplayName("재시도 가능 시 canRetry() 는 true 반환")
        void shouldReturnTrueWhenCanRetry() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.createFailed(2);

            // When & Then
            assertThat(change.canRetry()).isTrue();
        }

        @Test
        @DisplayName("재시도 횟수가 MAX_RETRY_COUNT(3)에 도달하면 canRetry() 는 false 반환")
        void shouldReturnFalseWhenRetryCountReachedMax() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.createFailed(3);

            // When & Then
            assertThat(change.canRetry()).isFalse();
        }

        @Test
        @DisplayName("여러 번 markAsFailed() 호출 시 retryCount 계속 증가")
        void shouldContinueIncrementingRetryCount() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();

            // When
            change.markAsFailed("Error 1");
            change.markAsFailed("Error 2");
            change.markAsFailed("Error 3");

            // Then
            assertThat(change.getRetryCount()).isEqualTo(3);
            assertThat(change.canRetry()).isFalse();
        }

        @Test
        @DisplayName("markAsFailed() 에서 실패 사유가 null이면 예외 발생")
        void shouldThrowExceptionWhenFailureReasonIsNull() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();

            // When & Then
            assertThatThrownBy(() -> change.markAsFailed(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("실패 사유는 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("markAsFailed() 에서 실패 사유가 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenFailureReasonIsBlank(String invalidReason) {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();

            // When & Then
            assertThatThrownBy(() -> change.markAsFailed(invalidReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("실패 사유는 필수입니다");
        }
    }

    @Nested
    @DisplayName("중복 알림 방지 테스트")
    class DuplicateNotificationTests {

        @Test
        @DisplayName("마지막 알림 시간이 null이면 isDuplicateNotification() 는 false 반환")
        void shouldReturnFalseWhenLastNotificationTimeIsNull() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();

            // When & Then
            assertThat(change.isDuplicateNotification(null)).isFalse();
        }

        @Test
        @DisplayName("마지막 알림 시간으로부터 24시간 이내면 isDuplicateNotification() 는 true 반환")
        void shouldReturnTrueWhenWithin24Hours() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ChangeDetection change = ChangeDetection.reconstitute(
                ChangeDetectionId.of(1L),
                ProductId.of(100L),
                ChangeType.PRICE,
                "hash-old",
                "hash-new",
                ChangeDataFixture.create(),
                NotificationStatus.PENDING,
                0,
                null,
                now, // 현재 시간에 감지
                null,
                now,
                now
            );
            LocalDateTime lastNotificationTime = now.minusHours(12); // 12시간 전 알림

            // When & Then
            assertThat(change.isDuplicateNotification(lastNotificationTime)).isTrue();
        }

        @Test
        @DisplayName("마지막 알림 시간으로부터 24시간 초과면 isDuplicateNotification() 는 false 반환")
        void shouldReturnFalseWhenAfter24Hours() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ChangeDetection change = ChangeDetection.reconstitute(
                ChangeDetectionId.of(1L),
                ProductId.of(100L),
                ChangeType.OPTION,
                "hash-old",
                "hash-new",
                ChangeDataFixture.create(),
                NotificationStatus.PENDING,
                0,
                null,
                now, // 현재 시간에 감지
                null,
                now,
                now
            );
            LocalDateTime lastNotificationTime = now.minusHours(25); // 25시간 전 알림

            // When & Then
            assertThat(change.isDuplicateNotification(lastNotificationTime)).isFalse();
        }

        @Test
        @DisplayName("정확히 24시간 경계에서 isDuplicateNotification() 테스트")
        void shouldHandleExactly24HourBoundary() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ChangeDetection change = ChangeDetection.reconstitute(
                ChangeDetectionId.of(1L),
                ProductId.of(100L),
                ChangeType.IMAGE,
                "hash-old",
                "hash-new",
                ChangeDataFixture.create(),
                NotificationStatus.PENDING,
                0,
                null,
                now, // 현재 시간에 감지
                null,
                now,
                now
            );
            LocalDateTime exactly24HoursAgo = now.minusHours(24).minusSeconds(1);

            // When & Then
            assertThat(change.isDuplicateNotification(exactly24HoursAgo)).isFalse();
        }
    }

    @Nested
    @DisplayName("변경 메시지 생성 테스트")
    class ChangeMessageGenerationTests {

        @Test
        @DisplayName("generateChangeMessage() 는 올바른 형식의 메시지를 생성")
        void shouldGenerateChangeMessageWithCorrectFormat() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();

            // When
            String message = change.generateChangeMessage();

            // Then
            assertThat(message).isNotNull();
            assertThat(message).contains("변경 감지");
            assertThat(message).contains("상품 ID:");
        }

        @Test
        @DisplayName("PRICE 유형의 메시지 생성")
        void shouldGeneratePriceChangeMessage() {
            // Given
            ChangeDetection change = ChangeDetection.forNew(
                ProductId.of(100L),
                ChangeType.PRICE,
                "hash-old",
                "hash-new",
                ChangeDataFixture.create()
            );

            // When
            String message = change.generateChangeMessage();

            // Then
            assertThat(message).contains("100");
        }
    }

    @Nested
    @DisplayName("상태 조회 테스트")
    class StatusQueryTests {

        @Test
        @DisplayName("hasStatus()는 현재 상태와 일치하면 true 반환")
        void shouldReturnTrueWhenStatusMatches() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();

            // When & Then
            assertThat(change.hasStatus(NotificationStatus.PENDING)).isTrue();
            assertThat(change.hasStatus(NotificationStatus.SENT)).isFalse();
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("상품 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenProductIdIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                ChangeDetection.forNew(
                    null,
                    ChangeType.PRICE,
                    "hash-old",
                    "hash-new",
                    ChangeDataFixture.create()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 ID는 필수입니다");
        }

        @Test
        @DisplayName("변경 유형이 null이면 예외 발생")
        void shouldThrowExceptionWhenChangeTypeIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                ChangeDetection.forNew(
                    ProductId.of(100L),
                    null,
                    "hash-old",
                    "hash-new",
                    ChangeDataFixture.create()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경 유형은 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("현재 해시가 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenCurrentHashIsNullOrBlank(String invalidHash) {
            // When & Then
            assertThatThrownBy(() ->
                ChangeDetection.forNew(
                    ProductId.of(100L),
                    ChangeType.PRICE,
                    "hash-old",
                    invalidHash,
                    ChangeDataFixture.create()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("현재 해시는 필수입니다");
        }

        @Test
        @DisplayName("변경 상세 정보가 null이면 예외 발생")
        void shouldThrowExceptionWhenChangeDetailsIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                ChangeDetection.forNew(
                    ProductId.of(100L),
                    ChangeType.OPTION,
                    "hash-old",
                    "hash-new",
                    null
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경 상세 정보는 필수입니다");
        }

        @Test
        @DisplayName("of() 메서드에서 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInOf() {
            // When & Then
            assertThatThrownBy(() ->
                ChangeDetection.of(
                    null,
                    ProductId.of(100L),
                    ChangeType.PRICE,
                    "hash-old",
                    "hash-new",
                    ChangeDataFixture.create()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ChangeDetection ID는 필수입니다");
        }

        @Test
        @DisplayName("reconstitute() 메서드에서 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInReconstitute() {
            // When & Then
            assertThatThrownBy(() ->
                ChangeDetection.reconstitute(
                    null,
                    ProductId.of(100L),
                    ChangeType.IMAGE,
                    "hash-old",
                    "hash-new",
                    ChangeDataFixture.create(),
                    NotificationStatus.PENDING,
                    0,
                    null,
                    LocalDateTime.now(),
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
        @DisplayName("신규 생성 시 초기 상태는 PENDING")
        void shouldHavePendingStatusWhenCreated() {
            // Given & When
            ChangeDetection change = ChangeDetectionFixture.create();

            // Then
            assertThat(change.getStatus()).isEqualTo(NotificationStatus.PENDING);
        }

        @Test
        @DisplayName("신규 생성 시 재시도 횟수는 0")
        void shouldHaveZeroRetryCountWhenCreated() {
            // Given & When
            ChangeDetection change = ChangeDetectionFixture.create();

            // Then
            assertThat(change.getRetryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("신규 생성 시 notifiedAt은 null")
        void shouldHaveNullNotifiedAtWhenCreated() {
            // Given & When
            ChangeDetection change = ChangeDetectionFixture.create();

            // Then
            assertThat(change.getNotifiedAt()).isNull();
        }

        @Test
        @DisplayName("재시도 횟수는 MAX_RETRY_COUNT(3)를 초과할 수 없음")
        void shouldNotExceedMaxRetryCount() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();

            // When
            change.markAsFailed("Error 1");
            change.markAsFailed("Error 2");
            change.markAsFailed("Error 3");

            // Then
            assertThat(change.getRetryCount()).isEqualTo(3);
            assertThat(change.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()는 ID를 직접 노출하지 않고 값만 반환")
        void shouldReturnIdValueWithoutExposingIdObject() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.createWithId(100L);

            // When
            Long idValue = change.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(100L);
        }

        @Test
        @DisplayName("getProductIdValue()는 상품 ID를 직접 노출하지 않고 값만 반환")
        void shouldReturnProductIdValueWithoutExposingProductIdObject() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.createWithProductId(200L);

            // When
            Long productIdValue = change.getProductIdValue();

            // Then
            assertThat(productIdValue).isEqualTo(200L);
        }

        @Test
        @DisplayName("getChangeDetailsValue()는 ChangeData 객체를 직접 노출하지 않고 값만 반환")
        void shouldReturnChangeDetailsValueWithoutExposingChangeDataObject() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();

            // When
            String detailsValue = change.getChangeDetailsValue();

            // Then
            assertThat(detailsValue).isNotNull();
        }

        @Test
        @DisplayName("equals()는 ID 기반으로 동작하며 객체 체이닝 없음")
        void shouldImplementEqualsBasedOnIdWithoutChaining() {
            // Given
            ChangeDetection change1 = ChangeDetectionFixture.createWithId(1L);
            ChangeDetection change2 = ChangeDetectionFixture.createWithId(1L);
            ChangeDetection change3 = ChangeDetectionFixture.createWithId(2L);

            // When & Then
            assertThat(change1).isEqualTo(change2);
            assertThat(change1).isNotEqualTo(change3);
        }

        @Test
        @DisplayName("hashCode()는 ID 기반으로 동작하며 객체 체이닝 없음")
        void shouldImplementHashCodeBasedOnIdWithoutChaining() {
            // Given
            ChangeDetection change1 = ChangeDetectionFixture.createWithId(1L);
            ChangeDetection change2 = ChangeDetectionFixture.createWithId(1L);

            // When & Then
            assertThat(change1.hashCode()).isEqualTo(change2.hashCode());
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("previousHash가 null이어도 정상 생성")
        void shouldCreateWithNullPreviousHash() {
            // Given
            ProductId productId = ProductId.of(100L);
            ChangeType changeType = ChangeType.PRICE;
            String previousHash = null; // 이전 해시 없음 (신규 상품)
            String currentHash = "hash-new";
            ChangeData changeDetails = ChangeDataFixture.create();

            // When
            ChangeDetection change = ChangeDetection.forNew(
                productId,
                changeType,
                previousHash,
                currentHash,
                changeDetails
            );

            // Then
            assertThat(change.getPreviousHash()).isNull();
            assertThat(change.getCurrentHash()).isEqualTo("hash-new");
        }

        @Test
        @DisplayName("해시가 매우 긴 문자열일 때도 정상 처리")
        void shouldHandleVeryLongHash() {
            // Given
            String longHash = "hash-" + "x".repeat(1000);

            // When
            ChangeDetection change = ChangeDetection.forNew(
                ProductId.of(100L),
                ChangeType.OPTION,
                "hash-old",
                longHash,
                ChangeDataFixture.create()
            );

            // Then
            assertThat(change.getCurrentHash()).hasSize(5 + 1000);
        }

        @Test
        @DisplayName("실패 사유가 매우 긴 문자열일 때도 정상 처리")
        void shouldHandleVeryLongFailureReason() {
            // Given
            ChangeDetection change = ChangeDetectionFixture.create();
            String longReason = "Error: " + "x".repeat(1000);

            // When
            change.markAsFailed(longReason);

            // Then
            assertThat(change.getFailureReason()).hasSize(7 + 1000);
        }
    }
}
