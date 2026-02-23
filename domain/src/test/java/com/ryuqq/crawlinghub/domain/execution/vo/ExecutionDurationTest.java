package com.ryuqq.crawlinghub.domain.execution.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("ExecutionDuration Value Object 단위 테스트")
class ExecutionDurationTest {

    private static final Instant START = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2025-01-01T00:00:05Z");

    @Nested
    @DisplayName("start() 팩토리 메서드 테스트")
    class StartFactoryTest {

        @Test
        @DisplayName("시작 시간만 설정되고 나머지는 null이다")
        void createsRunningState() {
            ExecutionDuration duration = ExecutionDuration.start(START);

            assertThat(duration.startedAt()).isEqualTo(START);
            assertThat(duration.completedAt()).isNull();
            assertThat(duration.durationMs()).isNull();
        }

        @Test
        @DisplayName("isRunning()이 true를 반환한다")
        void isRunningReturnsTrue() {
            ExecutionDuration duration = ExecutionDuration.start(START);
            assertThat(duration.isRunning()).isTrue();
        }

        @Test
        @DisplayName("isCompleted()이 false를 반환한다")
        void isCompletedReturnsFalse() {
            ExecutionDuration duration = ExecutionDuration.start(START);
            assertThat(duration.isCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("startAt() 팩토리 메서드 테스트")
    class StartAtFactoryTest {

        @Test
        @DisplayName("특정 시간으로 시작 상태를 생성한다")
        void createsRunningStateAtSpecificTime() {
            ExecutionDuration duration = ExecutionDuration.startAt(START);

            assertThat(duration.startedAt()).isEqualTo(START);
            assertThat(duration.isRunning()).isTrue();
        }
    }

    @Nested
    @DisplayName("complete() 메서드 테스트")
    class CompleteTest {

        @Test
        @DisplayName("완료 시간과 소요 시간이 설정된다")
        void setsCompletedAtAndDuration() {
            ExecutionDuration duration = ExecutionDuration.start(START).complete(END);

            assertThat(duration.startedAt()).isEqualTo(START);
            assertThat(duration.completedAt()).isEqualTo(END);
            assertThat(duration.durationMs()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("isCompleted()이 true를 반환한다")
        void isCompletedReturnsTrue() {
            ExecutionDuration duration = ExecutionDuration.start(START).complete(END);
            assertThat(duration.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isRunning()이 false를 반환한다")
        void isRunningReturnsFalse() {
            ExecutionDuration duration = ExecutionDuration.start(START).complete(END);
            assertThat(duration.isRunning()).isFalse();
        }

        @Test
        @DisplayName("getDurationSeconds()가 초 단위로 반환한다")
        void returnsDurationInSeconds() {
            ExecutionDuration duration = ExecutionDuration.start(START).complete(END);
            assertThat(duration.getDurationSeconds()).isEqualTo(5L);
        }

        @Test
        @DisplayName("완료되지 않으면 getDurationSeconds()가 null을 반환한다")
        void returnsNullDurationSecondsWhenNotCompleted() {
            ExecutionDuration duration = ExecutionDuration.start(START);
            assertThat(duration.getDurationSeconds()).isNull();
        }
    }

    @Nested
    @DisplayName("completeAt() 메서드 테스트")
    class CompleteAtTest {

        @Test
        @DisplayName("특정 완료 시간으로 완료 처리한다")
        void completesAtSpecificTime() {
            ExecutionDuration duration = ExecutionDuration.startAt(START).completeAt(END);

            assertThat(duration.completedAt()).isEqualTo(END);
            assertThat(duration.durationMs()).isEqualTo(5000L);
        }
    }

    @Nested
    @DisplayName("reconstitute() 팩토리 메서드 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("모든 필드를 복원한다")
        void reconstitutesAllFields() {
            ExecutionDuration duration = ExecutionDuration.reconstitute(START, END, 5000L);

            assertThat(duration.startedAt()).isEqualTo(START);
            assertThat(duration.completedAt()).isEqualTo(END);
            assertThat(duration.durationMs()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("completedAt가 null이어도 복원된다")
        void reconstitutesWithNullCompletedAt() {
            ExecutionDuration duration = ExecutionDuration.reconstitute(START, null, null);

            assertThat(duration.isRunning()).isTrue();
            assertThat(duration.getDurationSeconds()).isNull();
        }
    }

    @Nested
    @DisplayName("생성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("startedAt이 null이면 예외가 발생한다")
        void nullStartedAtThrowsException() {
            assertThatThrownBy(() -> new ExecutionDuration(null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작 시간");
        }

        @Test
        @DisplayName("completedAt이 startedAt보다 이전이면 예외가 발생한다")
        void completedAtBeforeStartedAtThrowsException() {
            Instant beforeStart = Instant.parse("2024-12-31T23:59:59Z");
            assertThatThrownBy(() -> new ExecutionDuration(START, beforeStart, 0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("완료 시간");
        }

        @Test
        @DisplayName("durationMs가 음수이면 예외가 발생한다")
        void negativeDurationMsThrowsException() {
            assertThatThrownBy(() -> new ExecutionDuration(START, END, -1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("소요 시간");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            ExecutionDuration duration1 = ExecutionDuration.reconstitute(START, END, 5000L);
            ExecutionDuration duration2 = ExecutionDuration.reconstitute(START, END, 5000L);
            assertThat(duration1).isEqualTo(duration2);
            assertThat(duration1.hashCode()).isEqualTo(duration2.hashCode());
        }

        @Test
        @DisplayName("다른 durationMs이면 다르다")
        void differentDurationMsAreNotEqual() {
            ExecutionDuration duration1 = ExecutionDuration.reconstitute(START, END, 5000L);
            ExecutionDuration duration2 = ExecutionDuration.reconstitute(START, END, 3000L);
            assertThat(duration1).isNotEqualTo(duration2);
        }
    }
}
