package com.ryuqq.crawlinghub.domain.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ScheduleStatus 테스트")
class ScheduleStatusTest {

    @Nested
    @DisplayName("Enum 값 테스트")
    class EnumValueTests {

        @Test
        @DisplayName("ACTIVE 상수 존재")
        void shouldHaveActiveConstant() {
            // Given & When
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // Then
            assertThat(status).isNotNull();
            assertThat(status.name()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("SUSPENDED 상수 존재")
        void shouldHaveSuspendedConstant() {
            // Given & When
            ScheduleStatus status = ScheduleStatus.SUSPENDED;

            // Then
            assertThat(status).isNotNull();
            assertThat(status.name()).isEqualTo("SUSPENDED");
        }

        @Test
        @DisplayName("Enum 값은 정확히 2개")
        void shouldHaveExactlyTwoValues() {
            // When
            ScheduleStatus[] values = ScheduleStatus.values();

            // Then
            assertThat(values).hasSize(2);
            assertThat(values).containsExactly(ScheduleStatus.ACTIVE, ScheduleStatus.SUSPENDED);
        }

        @ParameterizedTest
        @EnumSource(ScheduleStatus.class)
        @DisplayName("모든 Enum 값이 유효")
        void shouldHaveValidValues(ScheduleStatus status) {
            // Then
            assertThat(status).isNotNull();
            assertThat(status.name()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("getPriority() 메서드 테스트")
    class GetPriorityTests {

        @Test
        @DisplayName("ACTIVE는 우선순위 1 반환")
        void shouldReturnPriority1ForActive() {
            // Given
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When
            int priority = status.getPriority();

            // Then
            assertThat(priority).isEqualTo(1);
        }

        @Test
        @DisplayName("SUSPENDED는 우선순위 2 반환")
        void shouldReturnPriority2ForSuspended() {
            // Given
            ScheduleStatus status = ScheduleStatus.SUSPENDED;

            // When
            int priority = status.getPriority();

            // Then
            assertThat(priority).isEqualTo(2);
        }

        @ParameterizedTest
        @EnumSource(ScheduleStatus.class)
        @DisplayName("모든 상태는 양수 우선순위를 가짐")
        void shouldHavePositivePriority(ScheduleStatus status) {
            // When
            int priority = status.getPriority();

            // Then
            assertThat(priority).isPositive();
        }

        @Test
        @DisplayName("우선순위는 고유하다 (중복 없음)")
        void shouldHaveUniquePriorities() {
            // Given
            int activePriority = ScheduleStatus.ACTIVE.getPriority();
            int suspendedPriority = ScheduleStatus.SUSPENDED.getPriority();

            // Then
            assertThat(activePriority).isNotEqualTo(suspendedPriority);
        }
    }

    @Nested
    @DisplayName("getDescription() 메서드 테스트")
    class GetDescriptionTests {

        @Test
        @DisplayName("ACTIVE는 '활성' 설명 반환")
        void shouldReturnActiveDescription() {
            // Given
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When
            String description = status.getDescription();

            // Then
            assertThat(description).isEqualTo("활성");
        }

        @Test
        @DisplayName("SUSPENDED는 '일시정지' 설명 반환")
        void shouldReturnSuspendedDescription() {
            // Given
            ScheduleStatus status = ScheduleStatus.SUSPENDED;

            // When
            String description = status.getDescription();

            // Then
            assertThat(description).isEqualTo("일시정지");
        }

        @ParameterizedTest
        @EnumSource(ScheduleStatus.class)
        @DisplayName("모든 상태는 null이 아닌 설명을 가짐")
        void shouldHaveNonNullDescription(ScheduleStatus status) {
            // When
            String description = status.getDescription();

            // Then
            assertThat(description).isNotNull();
        }

        @ParameterizedTest
        @EnumSource(ScheduleStatus.class)
        @DisplayName("모든 상태는 비어있지 않은 설명을 가짐")
        void shouldHaveNonEmptyDescription(ScheduleStatus status) {
            // When
            String description = status.getDescription();

            // Then
            assertThat(description).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("isActive() 메서드 테스트")
    class IsActiveTests {

        @Test
        @DisplayName("ACTIVE는 true 반환")
        void shouldReturnTrueForActive() {
            // Given
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When
            boolean isActive = status.isActive();

            // Then
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED는 false 반환")
        void shouldReturnFalseForSuspended() {
            // Given
            ScheduleStatus status = ScheduleStatus.SUSPENDED;

            // When
            boolean isActive = status.isActive();

            // Then
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("isActive()는 상태에 따라 올바른 boolean 값 반환")
        void shouldReturnCorrectBooleanBasedOnStatus() {
            // Given
            ScheduleStatus[] allStatuses = ScheduleStatus.values();

            // When & Then
            for (ScheduleStatus status : allStatuses) {
                if (status == ScheduleStatus.ACTIVE) {
                    assertThat(status.isActive()).isTrue();
                } else {
                    assertThat(status.isActive()).isFalse();
                }
            }
        }
    }

    @Nested
    @DisplayName("fromString() 팩토리 메서드 테스트")
    class FromStringTests {

        @Test
        @DisplayName("'ACTIVE' 문자열로 ACTIVE 생성")
        void shouldCreateActiveFromString() {
            // When
            ScheduleStatus status = ScheduleStatus.fromString("ACTIVE");

            // Then
            assertThat(status).isEqualTo(ScheduleStatus.ACTIVE);
        }

        @Test
        @DisplayName("'SUSPENDED' 문자열로 SUSPENDED 생성")
        void shouldCreateSuspendedFromString() {
            // When
            ScheduleStatus status = ScheduleStatus.fromString("SUSPENDED");

            // Then
            assertThat(status).isEqualTo(ScheduleStatus.SUSPENDED);
        }

        @Test
        @DisplayName("소문자 'active'로 ACTIVE 생성 (대소문자 무시)")
        void shouldCreateActiveFromLowercase() {
            // When
            ScheduleStatus status = ScheduleStatus.fromString("active");

            // Then
            assertThat(status).isEqualTo(ScheduleStatus.ACTIVE);
        }

        @Test
        @DisplayName("대소문자 혼합 'AcTiVe'로 ACTIVE 생성")
        void shouldCreateActiveFromMixedCase() {
            // When
            ScheduleStatus status = ScheduleStatus.fromString("AcTiVe");

            // Then
            assertThat(status).isEqualTo(ScheduleStatus.ACTIVE);
        }

        @Test
        @DisplayName("앞뒤 공백 포함 ' ACTIVE '로 ACTIVE 생성 (trim 처리)")
        void shouldCreateActiveWithWhitespace() {
            // When
            ScheduleStatus status = ScheduleStatus.fromString(" ACTIVE ");

            // Then
            assertThat(status).isEqualTo(ScheduleStatus.ACTIVE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 문자열은 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullOrEmpty(String input) {
            // When & Then
            assertThatThrownBy(() -> ScheduleStatus.fromString(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ScheduleStatus는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("공백 문자만 있는 경우 IllegalArgumentException 발생")
        void shouldThrowExceptionForBlankString(String input) {
            // When & Then
            assertThatThrownBy(() -> ScheduleStatus.fromString(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ScheduleStatus는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "UNKNOWN", "RUNNING", "PENDING"})
        @DisplayName("유효하지 않은 문자열은 IllegalArgumentException 발생")
        void shouldThrowExceptionForInvalidString(String input) {
            // When & Then
            assertThatThrownBy(() -> ScheduleStatus.fromString(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 ScheduleStatus입니다");
        }

        @Test
        @DisplayName("예외 메시지에 잘못된 입력값 포함")
        void shouldIncludeInvalidValueInExceptionMessage() {
            // Given
            String invalidInput = "INVALID_STATUS";

            // When & Then
            assertThatThrownBy(() -> ScheduleStatus.fromString(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalidInput);
        }
    }

    @Nested
    @DisplayName("valueOf() 메서드 테스트")
    class ValueOfTests {

        @Test
        @DisplayName("문자열 'ACTIVE'로 Enum 생성")
        void shouldCreateActiveFromValueOf() {
            // When
            ScheduleStatus status = ScheduleStatus.valueOf("ACTIVE");

            // Then
            assertThat(status).isEqualTo(ScheduleStatus.ACTIVE);
        }

        @Test
        @DisplayName("문자열 'SUSPENDED'로 Enum 생성")
        void shouldCreateSuspendedFromValueOf() {
            // When
            ScheduleStatus status = ScheduleStatus.valueOf("SUSPENDED");

            // Then
            assertThat(status).isEqualTo(ScheduleStatus.SUSPENDED);
        }

        @Test
        @DisplayName("잘못된 문자열로 valueOf() 호출 시 IllegalArgumentException 발생")
        void shouldThrowExceptionForInvalidValueOf() {
            // When & Then
            assertThatThrownBy(() -> ScheduleStatus.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Enum 비교 테스트")
    class ComparisonTests {

        @Test
        @DisplayName("같은 Enum 값은 동일하다 (==)")
        void shouldBeEqualForSameEnum() {
            // Given
            ScheduleStatus status1 = ScheduleStatus.ACTIVE;
            ScheduleStatus status2 = ScheduleStatus.ACTIVE;

            // Then
            assertThat(status1).isEqualTo(status2);
            assertThat(status1).isSameAs(status2);  // Enum은 싱글톤
        }

        @Test
        @DisplayName("다른 Enum 값은 다르다 (!=)")
        void shouldNotBeEqualForDifferentEnum() {
            // Given
            ScheduleStatus status1 = ScheduleStatus.ACTIVE;
            ScheduleStatus status2 = ScheduleStatus.SUSPENDED;

            // Then
            assertThat(status1).isNotEqualTo(status2);
        }

        @Test
        @DisplayName("Enum 순서 확인 (ACTIVE는 0, SUSPENDED는 1)")
        void shouldHaveCorrectOrdinal() {
            // Given
            ScheduleStatus[] values = ScheduleStatus.values();

            // Then
            assertThat(values[0]).isEqualTo(ScheduleStatus.ACTIVE);
            assertThat(values[1]).isEqualTo(ScheduleStatus.SUSPENDED);
            assertThat(ScheduleStatus.ACTIVE.ordinal()).isEqualTo(0);
            assertThat(ScheduleStatus.SUSPENDED.ordinal()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("ACTIVE toString은 'ACTIVE' 반환")
        void shouldReturnActiveAsString() {
            // Given
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When
            String result = status.toString();

            // Then
            assertThat(result).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("SUSPENDED toString은 'SUSPENDED' 반환")
        void shouldReturnSuspendedAsString() {
            // Given
            ScheduleStatus status = ScheduleStatus.SUSPENDED;

            // When
            String result = status.toString();

            // Then
            assertThat(result).isEqualTo("SUSPENDED");
        }

        @ParameterizedTest
        @EnumSource(ScheduleStatus.class)
        @DisplayName("toString()은 Enum name()과 동일")
        void shouldToStringMatchName(ScheduleStatus status) {
            // When
            String toString = status.toString();
            String name = status.name();

            // Then
            assertThat(toString).isEqualTo(name);
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("스케줄 활성 상태 확인 시나리오")
        void shouldCheckActiveStatus() {
            // Given
            ScheduleStatus status = ScheduleStatus.fromString("ACTIVE");

            // When
            boolean isActive = status.isActive();

            // Then
            assertThat(isActive).isTrue();
            assertThat(status.getPriority()).isEqualTo(1);
            assertThat(status.getDescription()).isEqualTo("활성");
        }

        @Test
        @DisplayName("스케줄 일시정지 상태 확인 시나리오")
        void shouldCheckSuspendedStatus() {
            // Given
            ScheduleStatus status = ScheduleStatus.fromString("suspended");

            // When
            boolean isActive = status.isActive();

            // Then
            assertThat(isActive).isFalse();
            assertThat(status.getPriority()).isEqualTo(2);
            assertThat(status.getDescription()).isEqualTo("일시정지");
        }

        @Test
        @DisplayName("Switch 문에서 모든 상태 처리 가능")
        void shouldHandleAllStatusesInSwitch() {
            // Given
            ScheduleStatus[] allStatuses = ScheduleStatus.values();

            // When & Then
            for (ScheduleStatus status : allStatuses) {
                String result = switch (status) {
                    case ACTIVE -> "활성 스케줄";
                    case SUSPENDED -> "정지된 스케줄";
                };
                assertThat(result).isNotBlank();
            }
        }

        @Test
        @DisplayName("우선순위 기반 정렬 가능")
        void shouldSortByPriority() {
            // Given
            ScheduleStatus[] statuses = {ScheduleStatus.SUSPENDED, ScheduleStatus.ACTIVE};

            // When
            java.util.Arrays.sort(statuses, java.util.Comparator.comparingInt(ScheduleStatus::getPriority));

            // Then
            assertThat(statuses[0]).isEqualTo(ScheduleStatus.ACTIVE);  // 우선순위 1
            assertThat(statuses[1]).isEqualTo(ScheduleStatus.SUSPENDED);  // 우선순위 2
        }

        @Test
        @DisplayName("사용자 입력 검증 및 변환 시나리오")
        void shouldValidateAndConvertUserInput() {
            // Given
            String userInput = " Active ";  // 앞뒤 공백, 대소문자 혼합

            // When
            ScheduleStatus status = ScheduleStatus.fromString(userInput);

            // Then
            assertThat(status).isEqualTo(ScheduleStatus.ACTIVE);
            assertThat(status.isActive()).isTrue();
        }

        @Test
        @DisplayName("잘못된 사용자 입력 처리 시나리오")
        void shouldHandleInvalidUserInput() {
            // Given
            String invalidInput = "RUNNING";

            // When & Then
            assertThatThrownBy(() -> ScheduleStatus.fromString(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 ScheduleStatus입니다")
                .hasMessageContaining(invalidInput);
        }
    }
}
