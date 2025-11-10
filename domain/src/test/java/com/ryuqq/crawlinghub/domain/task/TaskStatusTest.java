package com.ryuqq.crawlinghub.domain.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TaskStatus Enum 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("TaskStatus Enum 단위 테스트")
class TaskStatusTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTests {

        @Test
        @DisplayName("TaskStatus는 6개의 상태를 가진다")
        void shouldHaveSixStatuses() {
            // When
            TaskStatus[] statuses = TaskStatus.values();

            // Then
            assertThat(statuses).hasSize(6);
            assertThat(statuses).containsExactly(
                TaskStatus.WAITING,
                TaskStatus.PUBLISHED,
                TaskStatus.RUNNING,
                TaskStatus.SUCCESS,
                TaskStatus.FAILED,
                TaskStatus.RETRY
            );
        }

        @ParameterizedTest
        @EnumSource(TaskStatus.class)
        @DisplayName("모든 TaskStatus는 priority와 description을 가진다")
        void shouldHavePriorityAndDescription(TaskStatus status) {
            // Then
            assertThat(status.getPriority()).isPositive();
            assertThat(status.getDescription()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("상태 판별 메서드 테스트")
    class StatusCheckTests {

        @Test
        @DisplayName("isCompleted()는 SUCCESS 상태에서만 true를 반환한다")
        void shouldReturnTrueOnlyForSuccessWhenIsCompleted() {
            // When & Then
            assertThat(TaskStatus.SUCCESS.isCompleted()).isTrue();
            assertThat(TaskStatus.WAITING.isCompleted()).isFalse();
            assertThat(TaskStatus.PUBLISHED.isCompleted()).isFalse();
            assertThat(TaskStatus.RUNNING.isCompleted()).isFalse();
            assertThat(TaskStatus.FAILED.isCompleted()).isFalse();
            assertThat(TaskStatus.RETRY.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("isFailed()는 FAILED 상태에서만 true를 반환한다")
        void shouldReturnTrueOnlyForFailedWhenIsFailed() {
            // When & Then
            assertThat(TaskStatus.FAILED.isFailed()).isTrue();
            assertThat(TaskStatus.WAITING.isFailed()).isFalse();
            assertThat(TaskStatus.PUBLISHED.isFailed()).isFalse();
            assertThat(TaskStatus.RUNNING.isFailed()).isFalse();
            assertThat(TaskStatus.SUCCESS.isFailed()).isFalse();
            assertThat(TaskStatus.RETRY.isFailed()).isFalse();
        }

        @Test
        @DisplayName("isRunning()은 RUNNING 상태에서만 true를 반환한다")
        void shouldReturnTrueOnlyForRunningWhenIsRunning() {
            // When & Then
            assertThat(TaskStatus.RUNNING.isRunning()).isTrue();
            assertThat(TaskStatus.WAITING.isRunning()).isFalse();
            assertThat(TaskStatus.PUBLISHED.isRunning()).isFalse();
            assertThat(TaskStatus.SUCCESS.isRunning()).isFalse();
            assertThat(TaskStatus.FAILED.isRunning()).isFalse();
            assertThat(TaskStatus.RETRY.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("fromString() 테스트")
    class FromStringTests {

        @ParameterizedTest
        @ValueSource(strings = {"WAITING", "PUBLISHED", "RUNNING", "SUCCESS", "FAILED", "RETRY"})
        @DisplayName("유효한 문자열로 TaskStatus 변환 성공")
        void shouldConvertFromValidString(String statusStr) {
            // When
            TaskStatus status = TaskStatus.fromString(statusStr);

            // Then
            assertThat(status).isNotNull();
            assertThat(status.name()).isEqualTo(statusStr);
        }

        @ParameterizedTest
        @ValueSource(strings = {" WAITING ", "  success  ", "\tRUNNING\n"})
        @DisplayName("공백이 있는 문자열도 trim 후 변환 성공")
        void shouldTrimAndConvertFromStringWithWhitespace(String statusStr) {
            // When
            TaskStatus status = TaskStatus.fromString(statusStr);

            // Then
            assertThat(status).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"waiting", "success", "retry"})
        @DisplayName("소문자 문자열도 대문자 변환 후 처리")
        void shouldConvertLowercaseString(String statusStr) {
            // When
            TaskStatus status = TaskStatus.fromString(statusStr);

            // Then
            assertThat(status).isNotNull();
            assertThat(status.name()).isEqualTo(statusStr.toUpperCase());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("빈 문자열이나 공백 문자열 입력 시 예외 발생")
        void shouldThrowExceptionWhenBlankString(String blankStr) {
            // When & Then
            assertThatThrownBy(() -> TaskStatus.fromString(blankStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TaskStatus는 필수입니다");
        }

        @Test
        @DisplayName("null 입력 시 예외 발생")
        void shouldThrowExceptionWhenNull() {
            // When & Then
            assertThatThrownBy(() -> TaskStatus.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TaskStatus는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
        @DisplayName("유효하지 않은 문자열 입력 시 예외 발생")
        void shouldThrowExceptionWhenInvalidString(String invalidStr) {
            // When & Then
            assertThatThrownBy(() -> TaskStatus.fromString(invalidStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 TaskStatus입니다");
        }
    }

    @Nested
    @DisplayName("Priority 테스트")
    class PriorityTests {

        @Test
        @DisplayName("각 상태는 고유한 priority를 가진다")
        void shouldHaveUniquePriority() {
            // When
            TaskStatus[] statuses = TaskStatus.values();

            // Then: 6개 상태 각각 다른 priority
            assertThat(statuses).extracting(TaskStatus::getPriority)
                .containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("상태 우선순위: WAITING(1) < PUBLISHED(2) < RUNNING(3) < SUCCESS(4) < FAILED(5) < RETRY(6)")
        void shouldHaveCorrectPriorityOrder() {
            // Then
            assertThat(TaskStatus.WAITING.getPriority()).isEqualTo(1);
            assertThat(TaskStatus.PUBLISHED.getPriority()).isEqualTo(2);
            assertThat(TaskStatus.RUNNING.getPriority()).isEqualTo(3);
            assertThat(TaskStatus.SUCCESS.getPriority()).isEqualTo(4);
            assertThat(TaskStatus.FAILED.getPriority()).isEqualTo(5);
            assertThat(TaskStatus.RETRY.getPriority()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("Description 테스트")
    class DescriptionTests {

        @Test
        @DisplayName("각 상태는 한글 설명을 가진다")
        void shouldHaveKoreanDescription() {
            // Then
            assertThat(TaskStatus.WAITING.getDescription()).isEqualTo("대기");
            assertThat(TaskStatus.PUBLISHED.getDescription()).isEqualTo("발행됨");
            assertThat(TaskStatus.RUNNING.getDescription()).isEqualTo("실행중");
            assertThat(TaskStatus.SUCCESS.getDescription()).isEqualTo("성공");
            assertThat(TaskStatus.FAILED.getDescription()).isEqualTo("실패");
            assertThat(TaskStatus.RETRY.getDescription()).isEqualTo("재시도");
        }
    }
}
