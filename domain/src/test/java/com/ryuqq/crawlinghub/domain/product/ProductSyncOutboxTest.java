package com.ryuqq.crawlinghub.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProductSyncOutbox Aggregate Root 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-10
 */
@DisplayName("ProductSyncOutbox Aggregate Root 단위 테스트")
class ProductSyncOutboxTest {

    private static final Long PRODUCT_ID = 12345L;
    private static final String PRODUCT_JSON = "{\"id\":12345,\"name\":\"테스트상품\"}";
    private static final ProductSyncOutboxId OUTBOX_ID = new ProductSyncOutboxId(1L);

    @Nested
    @DisplayName("Static Factory 메서드 테스트")
    class StaticFactoryTests {

        @Test
        @DisplayName("create()로 신규 Outbox 생성 성공 (ID 없음)")
        void shouldCreateNewOutboxWithoutId() {
            // When
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);

            // Then
            assertThat(outbox).isNotNull();
            assertThat(outbox.getIdValue()).isNull();  // ID 없음
            assertThat(outbox.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(outbox.getProductJson()).isEqualTo(PRODUCT_JSON);
            assertThat(outbox.getStatus()).isEqualTo(SyncStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
            assertThat(outbox.getErrorMessage()).isNull();
            assertThat(outbox.getCreatedAt()).isNotNull();
            assertThat(outbox.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("reconstitute()로 DB 재구성 성공")
        void shouldReconstituteFromDatabase() {
            // When
            ProductSyncOutbox outbox = ProductSyncOutbox.reconstitute(
                OUTBOX_ID,
                PRODUCT_ID,
                PRODUCT_JSON,
                SyncStatus.COMPLETED,
                2,
                "에러 메시지",
                java.time.LocalDateTime.now().minusHours(1),
                java.time.LocalDateTime.now()
            );

            // Then
            assertThat(outbox.getIdValue()).isEqualTo(OUTBOX_ID.value());
            assertThat(outbox.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(outbox.getStatus()).isEqualTo(SyncStatus.COMPLETED);
            assertThat(outbox.getRetryCount()).isEqualTo(2);
            assertThat(outbox.getErrorMessage()).isEqualTo("에러 메시지");
        }

        @Test
        @DisplayName("reconstitute()에서 null ID는 예외 발생")
        void shouldThrowExceptionWhenReconstituteWithNullId() {
            // When & Then
            assertThatThrownBy(() -> ProductSyncOutbox.reconstitute(
                null,  // null ID
                PRODUCT_ID,
                PRODUCT_JSON,
                SyncStatus.PENDING,
                0,
                null,
                java.time.LocalDateTime.now(),
                null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }
    }

    @Nested
    @DisplayName("필수 필드 검증 테스트")
    class ValidationTests {

        @Test
        @DisplayName("null Product ID로 생성 시 예외 발생")
        void shouldThrowExceptionWhenNullProductId() {
            // When & Then
            assertThatThrownBy(() -> ProductSyncOutbox.create(null, PRODUCT_JSON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null Product JSON으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenNullProductJson(String nullJson) {
            // When & Then
            assertThatThrownBy(() -> ProductSyncOutbox.create(PRODUCT_ID, nullJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product JSON은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
        @DisplayName("빈 문자열 Product JSON으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenBlankProductJson(String blankJson) {
            // When & Then
            assertThatThrownBy(() -> ProductSyncOutbox.create(PRODUCT_ID, blankJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product JSON은 필수입니다");
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트 - PENDING → PROCESSING")
    class PendingToProcessingTests {

        @Test
        @DisplayName("PENDING 상태에서 PROCESSING으로 전환 성공")
        void shouldTransitionFromPendingToProcessing() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            assertThat(outbox.getStatus()).isEqualTo(SyncStatus.PENDING);

            // When
            outbox.markAsProcessing();

            // Then
            assertThat(outbox.getStatus()).isEqualTo(SyncStatus.PROCESSING);
        }

        @Test
        @DisplayName("PROCESSING 상태에서 PROCESSING으로 전환 시 예외 발생")
        void shouldThrowExceptionWhenProcessingToProcessing() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.markAsProcessing();

            // When & Then
            assertThatThrownBy(() -> outbox.markAsProcessing())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING 상태에서만 PROCESSING으로 변경 가능합니다");
        }

        @Test
        @DisplayName("COMPLETED 상태에서 PROCESSING으로 전환 시 예외 발생")
        void shouldThrowExceptionWhenCompletedToProcessing() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.markAsProcessing();
            outbox.markAsCompleted();

            // When & Then
            assertThatThrownBy(() -> outbox.markAsProcessing())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING 상태에서만 PROCESSING으로 변경 가능합니다");
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트 - PROCESSING → COMPLETED")
    class ProcessingToCompletedTests {

        @Test
        @DisplayName("PROCESSING 상태에서 COMPLETED로 전환 성공")
        void shouldTransitionFromProcessingToCompleted() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.markAsProcessing();

            // When
            outbox.markAsCompleted();

            // Then
            assertThat(outbox.getStatus()).isEqualTo(SyncStatus.COMPLETED);
            assertThat(outbox.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING 상태에서 COMPLETED로 전환 시 예외 발생")
        void shouldThrowExceptionWhenPendingToCompleted() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);

            // When & Then
            assertThatThrownBy(() -> outbox.markAsCompleted())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PROCESSING 상태에서만 COMPLETED로 변경 가능합니다");
        }
    }

    @Nested
    @DisplayName("재시도 로직 테스트")
    class RetryTests {

        @Test
        @DisplayName("재시도 카운트 증가 및 PENDING으로 복원")
        void shouldIncrementRetryCountAndResetToPending() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.markAsProcessing();
            assertThat(outbox.getRetryCount()).isEqualTo(0);

            // When
            outbox.incrementRetryCount();

            // Then
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.getStatus()).isEqualTo(SyncStatus.PENDING);  // 재시도를 위해 복원
        }

        @Test
        @DisplayName("여러 번 재시도 카운트 증가")
        void shouldIncrementRetryCountMultipleTimes() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);

            // When
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();

            // Then
            assertThat(outbox.getRetryCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과 여부 확인 - 초과하지 않음")
        void shouldNotExceedMaxRetries() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();

            // When & Then
            assertThat(outbox.isMaxRetriesExceeded()).isFalse();
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과 여부 확인 - 정확히 3회")
        void shouldExceedMaxRetriesAtThree() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();

            // When & Then
            assertThat(outbox.isMaxRetriesExceeded()).isTrue();
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과 여부 확인 - 3회 초과")
        void shouldExceedMaxRetriesAboveThree() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();

            // When & Then
            assertThat(outbox.isMaxRetriesExceeded()).isTrue();
        }
    }

    @Nested
    @DisplayName("에러 메시지 기록 테스트")
    class ErrorMessageTests {

        @Test
        @DisplayName("에러 메시지 기록 성공")
        void shouldRecordErrorMessage() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            String errorMessage = "Network timeout occurred";

            // When
            outbox.recordError(errorMessage);

            // Then
            assertThat(outbox.getErrorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("에러 메시지 여러 번 기록 시 마지막 값 유지")
        void shouldKeepLatestErrorMessage() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);

            // When
            outbox.recordError("First error");
            outbox.recordError("Second error");
            outbox.recordError("Third error");

            // Then
            assertThat(outbox.getErrorMessage()).isEqualTo("Third error");
        }
    }

    @Nested
    @DisplayName("실패 처리 테스트")
    class FailureTests {

        @Test
        @DisplayName("실패로 마킹 성공")
        void shouldMarkAsFailed() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.markAsProcessing();

            // When
            outbox.markAsFailed();

            // Then
            assertThat(outbox.getStatus()).isEqualTo(SyncStatus.FAILED);
            assertThat(outbox.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING 상태에서도 FAILED로 전환 가능")
        void shouldMarkAsFailedFromPending() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);

            // When
            outbox.markAsFailed();

            // Then
            assertThat(outbox.getStatus()).isEqualTo(SyncStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("처리 가능 여부 확인 테스트")
    class CanProcessTests {

        @Test
        @DisplayName("PENDING 상태이고 재시도 횟수가 3회 미만이면 처리 가능")
        void shouldBeAbleToProcessWhenPendingAndBelowMaxRetries() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);

            // When & Then
            assertThat(outbox.canProcess()).isTrue();
        }

        @Test
        @DisplayName("PROCESSING 상태이면 처리 불가능")
        void shouldNotBeAbleToProcessWhenProcessing() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.markAsProcessing();

            // When & Then
            assertThat(outbox.canProcess()).isFalse();
        }

        @Test
        @DisplayName("COMPLETED 상태이면 처리 불가능")
        void shouldNotBeAbleToProcessWhenCompleted() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.markAsProcessing();
            outbox.markAsCompleted();

            // When & Then
            assertThat(outbox.canProcess()).isFalse();
        }

        @Test
        @DisplayName("FAILED 상태이면 처리 불가능")
        void shouldNotBeAbleToProcessWhenFailed() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.markAsFailed();

            // When & Then
            assertThat(outbox.canProcess()).isFalse();
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과 시 처리 불가능")
        void shouldNotBeAbleToProcessWhenMaxRetriesExceeded() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();  // 3회

            // When & Then
            assertThat(outbox.canProcess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Law of Demeter 래퍼 메서드 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()는 ID가 있으면 Long 반환")
        void shouldReturnIdValueWhenIdExists() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.reconstitute(
                OUTBOX_ID,
                PRODUCT_ID,
                PRODUCT_JSON,
                SyncStatus.PENDING,
                0,
                null,
                java.time.LocalDateTime.now(),
                null
            );

            // When & Then
            assertThat(outbox.getIdValue()).isEqualTo(OUTBOX_ID.value());
        }

        @Test
        @DisplayName("getIdValue()는 ID가 없으면 null 반환")
        void shouldReturnNullWhenIdNotExists() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.create(PRODUCT_ID, PRODUCT_JSON);

            // When & Then
            assertThat(outbox.getIdValue()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 ID를 가진 두 Outbox는 같다")
        void shouldBeEqualForSameId() {
            // Given
            ProductSyncOutbox outbox1 = ProductSyncOutbox.reconstitute(
                OUTBOX_ID, PRODUCT_ID, PRODUCT_JSON,
                SyncStatus.PENDING, 0, null,
                java.time.LocalDateTime.now(), null
            );
            ProductSyncOutbox outbox2 = ProductSyncOutbox.reconstitute(
                OUTBOX_ID, PRODUCT_ID, PRODUCT_JSON,
                SyncStatus.COMPLETED, 2, "error",
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
            );

            // When & Then
            assertThat(outbox1).isEqualTo(outbox2);
        }

        @Test
        @DisplayName("다른 ID를 가진 두 Outbox는 다르다")
        void shouldNotBeEqualForDifferentId() {
            // Given
            ProductSyncOutbox outbox1 = ProductSyncOutbox.reconstitute(
                OUTBOX_ID, PRODUCT_ID, PRODUCT_JSON,
                SyncStatus.PENDING, 0, null,
                java.time.LocalDateTime.now(), null
            );
            ProductSyncOutbox outbox2 = ProductSyncOutbox.reconstitute(
                new ProductSyncOutboxId(2L), PRODUCT_ID, PRODUCT_JSON,
                SyncStatus.PENDING, 0, null,
                java.time.LocalDateTime.now(), null
            );

            // When & Then
            assertThat(outbox1).isNotEqualTo(outbox2);
        }

        @Test
        @DisplayName("같은 ID를 가진 두 Outbox는 같은 hashCode를 반환한다")
        void shouldReturnSameHashCodeForSameId() {
            // Given
            ProductSyncOutbox outbox1 = ProductSyncOutbox.reconstitute(
                OUTBOX_ID, PRODUCT_ID, PRODUCT_JSON,
                SyncStatus.PENDING, 0, null,
                java.time.LocalDateTime.now(), null
            );
            ProductSyncOutbox outbox2 = ProductSyncOutbox.reconstitute(
                OUTBOX_ID, PRODUCT_ID, PRODUCT_JSON,
                SyncStatus.COMPLETED, 2, "error",
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
            );

            // When & Then
            assertThat(outbox1.hashCode()).isEqualTo(outbox2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 주요 정보를 포함한다")
        void shouldContainKeyInformation() {
            // Given
            ProductSyncOutbox outbox = ProductSyncOutbox.reconstitute(
                OUTBOX_ID, PRODUCT_ID, PRODUCT_JSON,
                SyncStatus.PROCESSING, 1, null,
                java.time.LocalDateTime.now(), null
            );

            // When
            String result = outbox.toString();

            // Then
            assertThat(result).contains("ProductSyncOutbox");
            assertThat(result).contains("id=" + OUTBOX_ID);
            assertThat(result).contains("productId=" + PRODUCT_ID);
            assertThat(result).contains("status=" + SyncStatus.PROCESSING);
            assertThat(result).contains("retryCount=1");
        }
    }
}
