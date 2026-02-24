package com.ryuqq.crawlinghub.domain.task.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("OutboxStatus 단위 테스트")
class OutboxStatusTest {

    @Nested
    @DisplayName("isPending() 테스트")
    class IsPendingTest {

        @Test
        @DisplayName("PENDING이면 true를 반환한다")
        void pendingReturnsTrue() {
            assertThat(OutboxStatus.PENDING.isPending()).isTrue();
        }

        @Test
        @DisplayName("PENDING이 아니면 false를 반환한다")
        void nonPendingReturnsFalse() {
            assertThat(OutboxStatus.PROCESSING.isPending()).isFalse();
            assertThat(OutboxStatus.SENT.isPending()).isFalse();
            assertThat(OutboxStatus.FAILED.isPending()).isFalse();
        }
    }

    @Nested
    @DisplayName("isProcessing() 테스트")
    class IsProcessingTest {

        @Test
        @DisplayName("PROCESSING이면 true를 반환한다")
        void processingReturnsTrue() {
            assertThat(OutboxStatus.PROCESSING.isProcessing()).isTrue();
        }

        @Test
        @DisplayName("PROCESSING이 아니면 false를 반환한다")
        void nonProcessingReturnsFalse() {
            assertThat(OutboxStatus.PENDING.isProcessing()).isFalse();
            assertThat(OutboxStatus.SENT.isProcessing()).isFalse();
            assertThat(OutboxStatus.FAILED.isProcessing()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSent() 테스트")
    class IsSentTest {

        @Test
        @DisplayName("SENT이면 true를 반환한다")
        void sentReturnsTrue() {
            assertThat(OutboxStatus.SENT.isSent()).isTrue();
        }

        @Test
        @DisplayName("SENT가 아니면 false를 반환한다")
        void nonSentReturnsFalse() {
            assertThat(OutboxStatus.PENDING.isSent()).isFalse();
            assertThat(OutboxStatus.PROCESSING.isSent()).isFalse();
            assertThat(OutboxStatus.FAILED.isSent()).isFalse();
        }
    }

    @Nested
    @DisplayName("canRetry() 테스트")
    class CanRetryTest {

        @Test
        @DisplayName("PENDING이면 재시도 가능하다")
        void pendingCanRetry() {
            assertThat(OutboxStatus.PENDING.canRetry()).isTrue();
        }

        @Test
        @DisplayName("FAILED이면 재시도 가능하다")
        void failedCanRetry() {
            assertThat(OutboxStatus.FAILED.canRetry()).isTrue();
        }

        @Test
        @DisplayName("PROCESSING이면 재시도 불가능하다")
        void processingCannotRetry() {
            assertThat(OutboxStatus.PROCESSING.canRetry()).isFalse();
        }

        @Test
        @DisplayName("SENT이면 재시도 불가능하다")
        void sentCannotRetry() {
            assertThat(OutboxStatus.SENT.canRetry()).isFalse();
        }
    }
}
