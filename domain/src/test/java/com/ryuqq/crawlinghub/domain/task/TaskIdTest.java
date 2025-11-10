package com.ryuqq.crawlinghub.domain.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TaskId Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("TaskId Value Object 단위 테스트")
class TaskIdTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("유효한 양수 Long 값으로 TaskId 생성 성공")
        void shouldCreateWithValidPositiveLong() {
            // Given
            Long value = 12345L;

            // When
            TaskId taskId = TaskId.of(value);

            // Then
            assertThat(taskId).isNotNull();
            assertThat(taskId.value()).isEqualTo(12345L);
        }

        @Test
        @DisplayName("null 값으로 TaskId 생성 가능 (DB 저장 전 임시 상태)")
        void shouldCreateWithNullValue() {
            // When
            TaskId taskId = TaskId.of(null);

            // Then
            assertThat(taskId).isNotNull();
            assertThat(taskId.value()).isNull();
        }

        @Test
        @DisplayName("newId()로 UUID 기반 임시 TaskId 생성 성공")
        void shouldCreateNewIdWithUuidBased() {
            // When
            TaskId taskId = TaskId.newId();

            // Then
            assertThat(taskId).isNotNull();
            assertThat(taskId.value()).isNotNull();
            assertThat(taskId.value()).isPositive();
        }

        @Test
        @DisplayName("newId()는 호출마다 다른 값을 반환한다")
        void shouldReturnDifferentValueEachTimeWhenNewId() {
            // When
            TaskId taskId1 = TaskId.newId();
            TaskId taskId2 = TaskId.newId();

            // Then
            assertThat(taskId1.value()).isNotEqualTo(taskId2.value());
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100, Long.MIN_VALUE})
        @DisplayName("0 이하의 값으로 TaskId 생성 시 예외 발생")
        void shouldThrowExceptionWhenValueIsNotPositive(Long invalidValue) {
            // When & Then
            assertThatThrownBy(() -> TaskId.of(invalidValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task ID는 양수여야 합니다");
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 value를 가진 두 TaskId는 같다")
        void shouldBeEqualForSameValue() {
            // Given
            Long value = 12345L;
            TaskId taskId1 = TaskId.of(value);
            TaskId taskId2 = TaskId.of(value);

            // When & Then
            assertThat(taskId1).isEqualTo(taskId2);
        }

        @Test
        @DisplayName("다른 value를 가진 두 TaskId는 다르다")
        void shouldNotBeEqualForDifferentValue() {
            // Given
            TaskId taskId1 = TaskId.of(12345L);
            TaskId taskId2 = TaskId.of(67890L);

            // When & Then
            assertThat(taskId1).isNotEqualTo(taskId2);
        }

        @Test
        @DisplayName("같은 value를 가진 두 TaskId는 같은 hashCode를 반환한다")
        void shouldReturnSameHashCodeForSameValue() {
            // Given
            Long value = 12345L;
            TaskId taskId1 = TaskId.of(value);
            TaskId taskId2 = TaskId.of(value);

            // When & Then
            assertThat(taskId1.hashCode()).isEqualTo(taskId2.hashCode());
        }

        @Test
        @DisplayName("null value를 가진 두 TaskId는 같다")
        void shouldBeEqualForNullValue() {
            // Given
            TaskId taskId1 = TaskId.of(null);
            TaskId taskId2 = TaskId.of(null);

            // When & Then
            assertThat(taskId1).isEqualTo(taskId2);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 value를 포함한다")
        void shouldIncludeValueInToString() {
            // Given
            Long value = 12345L;
            TaskId taskId = TaskId.of(value);

            // When
            String result = taskId.toString();

            // Then: Record 형식
            assertThat(result).contains("TaskId[value=12345]");
        }
    }
}
