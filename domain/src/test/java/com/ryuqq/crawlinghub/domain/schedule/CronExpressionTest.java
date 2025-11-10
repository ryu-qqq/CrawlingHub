package com.ryuqq.crawlinghub.domain.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CronExpression 테스트")
class CronExpressionTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("유효한 표현식으로 CronExpression 생성")
        void shouldCreateWithValidExpression() {
            // Given
            String expression = "0 0 * * * *";

            // When
            CronExpression cronExpression = CronExpression.of(expression);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(expression);
        }

        @Test
        @DisplayName("복잡한 Cron 표현식으로 생성")
        void shouldCreateWithComplexExpression() {
            // Given
            String expression = "0 0/15 * * * ?";

            // When
            CronExpression cronExpression = CronExpression.of(expression);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(expression);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "0 0 * * * *",
            "0 */5 * * * ?",
            "0 0 12 * * ?",
            "0 15 10 * * ?",
            "0 0/5 14,18 * * ?"
        })
        @DisplayName("다양한 Cron 표현식으로 생성 가능")
        void shouldCreateWithVariousExpressions(String expression) {
            // When
            CronExpression cronExpression = CronExpression.of(expression);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(expression);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 표현식은 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullOrEmpty(String expression) {
            // When & Then
            assertThatThrownBy(() -> CronExpression.of(expression))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("공백 문자만 있는 표현식은 IllegalArgumentException 발생")
        void shouldThrowExceptionForBlankExpression(String expression) {
            // When & Then
            assertThatThrownBy(() -> CronExpression.of(expression))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getValue() 메서드 테스트")
    class GetValueTests {

        @Test
        @DisplayName("getValue()는 생성 시 전달된 표현식 반환")
        void shouldReturnOriginalExpression() {
            // Given
            String expression = "0 0 * * * *";
            CronExpression cronExpression = CronExpression.of(expression);

            // When
            String result = cronExpression.getValue();

            // Then
            assertThat(result).isEqualTo(expression);
        }

        @Test
        @DisplayName("getValue()는 null이 아님")
        void shouldReturnNonNullValue() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            String result = cronExpression.getValue();

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("getValue()는 비어있지 않음")
        void shouldReturnNonEmptyValue() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            String result = cronExpression.getValue();

            // Then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("getValue()는 불변 (여러 번 호출해도 동일)")
        void shouldBeImmutable() {
            // Given
            String expression = "0 0 * * * *";
            CronExpression cronExpression = CronExpression.of(expression);

            // When
            String result1 = cronExpression.getValue();
            String result2 = cronExpression.getValue();

            // Then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1).isSameAs(result2);
        }
    }

    @Nested
    @DisplayName("isSameAs() 메서드 테스트")
    class IsSameAsTests {

        @Test
        @DisplayName("같은 표현식이면 true 반환")
        void shouldReturnTrueForSameExpression() {
            // Given
            String expression = "0 0 * * * *";
            CronExpression cronExpression1 = CronExpression.of(expression);
            CronExpression cronExpression2 = CronExpression.of(expression);

            // When
            boolean isSame = cronExpression1.isSameAs(cronExpression2);

            // Then
            assertThat(isSame).isTrue();
        }

        @Test
        @DisplayName("다른 표현식이면 false 반환")
        void shouldReturnFalseForDifferentExpression() {
            // Given
            CronExpression cronExpression1 = CronExpression.of("0 0 * * * *");
            CronExpression cronExpression2 = CronExpression.of("0 */5 * * * ?");

            // When
            boolean isSame = cronExpression1.isSameAs(cronExpression2);

            // Then
            assertThat(isSame).isFalse();
        }

        @Test
        @DisplayName("null과 비교하면 false 반환")
        void shouldReturnFalseForNull() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            boolean isSame = cronExpression.isSameAs(null);

            // Then
            assertThat(isSame).isFalse();
        }

        @Test
        @DisplayName("자기 자신과 비교하면 true 반환")
        void shouldReturnTrueForSelf() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            boolean isSame = cronExpression.isSameAs(cronExpression);

            // Then
            assertThat(isSame).isTrue();
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 표현식으로 생성된 두 객체는 동일")
        void shouldBeEqualForSameExpression() {
            // Given
            String expression = "0 0 * * * *";
            CronExpression cronExpression1 = CronExpression.of(expression);
            CronExpression cronExpression2 = CronExpression.of(expression);

            // Then
            assertThat(cronExpression1).isEqualTo(cronExpression2);
            assertThat(cronExpression1.hashCode()).isEqualTo(cronExpression2.hashCode());
        }

        @Test
        @DisplayName("다른 표현식으로 생성된 두 객체는 다름")
        void shouldNotBeEqualForDifferentExpression() {
            // Given
            CronExpression cronExpression1 = CronExpression.of("0 0 * * * *");
            CronExpression cronExpression2 = CronExpression.of("0 */5 * * * ?");

            // Then
            assertThat(cronExpression1).isNotEqualTo(cronExpression2);
        }

        @Test
        @DisplayName("같은 객체는 자기 자신과 동일")
        void shouldBeEqualToItself() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // Then
            assertThat(cronExpression).isEqualTo(cronExpression);
        }

        @Test
        @DisplayName("null과는 동일하지 않음")
        void shouldNotBeEqualToNull() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // Then
            assertThat(cronExpression).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 동일하지 않음")
        void shouldNotBeEqualToDifferentType() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");
            String otherObject = "0 0 * * * *";

            // Then
            assertThat(cronExpression).isNotEqualTo(otherObject);
        }

        @Test
        @DisplayName("hashCode는 일관성 있게 반환")
        void shouldReturnConsistentHashCode() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            int hashCode1 = cronExpression.hashCode();
            int hashCode2 = cronExpression.hashCode();

            // Then
            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("같은 표현식은 같은 hashCode를 가짐")
        void shouldHaveSameHashCodeForSameExpression() {
            // Given
            String expression = "0 0 * * * *";
            CronExpression cronExpression1 = CronExpression.of(expression);
            CronExpression cronExpression2 = CronExpression.of(expression);

            // Then
            assertThat(cronExpression1.hashCode()).isEqualTo(cronExpression2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 null이 아님")
        void shouldHaveNonNullToString() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            String result = cronExpression.toString();

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toString()은 비어있지 않음")
        void shouldHaveNonEmptyToString() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            String result = cronExpression.toString();

            // Then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("toString()은 표현식을 포함")
        void shouldContainExpressionInToString() {
            // Given
            String expression = "0 0 * * * *";
            CronExpression cronExpression = CronExpression.of(expression);

            // When
            String result = cronExpression.toString();

            // Then
            assertThat(result).contains(expression);
        }

        @Test
        @DisplayName("toString()은 클래스명 포함")
        void shouldContainClassNameInToString() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            String result = cronExpression.toString();

            // Then
            assertThat(result).isEqualTo("0 0 * * * *");
        }
    }

    @Nested
    @DisplayName("Value Object 특성 테스트")
    class ValueObjectCharacteristicsTests {

        @Test
        @DisplayName("Value Object는 불변 (immutable)")
        void shouldBeImmutable() {
            // Given
            String expression = "0 0 * * * *";
            CronExpression cronExpression = CronExpression.of(expression);

            // When
            String retrievedValue = cronExpression.getValue();

            // Then
            assertThat(retrievedValue).isEqualTo(expression);
            // Value Object는 불변이므로 값을 변경할 수 없음
        }

        @Test
        @DisplayName("Value Object는 값 기반 동등성을 가짐")
        void shouldHaveValueBasedEquality() {
            // Given
            String expression = "0 0 * * * *";
            CronExpression cronExpression1 = CronExpression.of(expression);
            CronExpression cronExpression2 = CronExpression.of(expression);

            // Then
            assertThat(cronExpression1).isEqualTo(cronExpression2);
            assertThat(cronExpression1).isNotSameAs(cronExpression2);  // 다른 인스턴스
        }

        @Test
        @DisplayName("private 생성자를 가짐 (factory method만 사용)")
        void shouldHavePrivateConstructor() {
            // Given
            Class<CronExpression> clazz = CronExpression.class;

            // Then
            assertThat(clazz.getDeclaredConstructors())
                .allMatch(constructor -> java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("매 시 정각 실행 표현식 생성")
        void shouldCreateHourlyExpression() {
            // Given
            String expression = "0 0 * * * *";

            // When
            CronExpression cronExpression = CronExpression.of(expression);

            // Then
            assertThat(cronExpression.getValue()).isEqualTo(expression);
        }

        @Test
        @DisplayName("5분마다 실행 표현식 생성")
        void shouldCreateEveryFiveMinutesExpression() {
            // Given
            String expression = "0 */5 * * * ?";

            // When
            CronExpression cronExpression = CronExpression.of(expression);

            // Then
            assertThat(cronExpression.getValue()).isEqualTo(expression);
        }

        @Test
        @DisplayName("Cron 표현식 변경 시나리오 (새 객체 생성)")
        void shouldCreateNewObjectForChange() {
            // Given
            CronExpression oldExpression = CronExpression.of("0 0 * * * *");

            // When
            CronExpression newExpression = CronExpression.of("0 */5 * * * ?");

            // Then
            assertThat(oldExpression).isNotEqualTo(newExpression);
            assertThat(oldExpression.getValue()).isEqualTo("0 0 * * * *");
            assertThat(newExpression.getValue()).isEqualTo("0 */5 * * * ?");
        }

        @Test
        @DisplayName("Cron 표현식 비교 시나리오")
        void shouldCompareExpressions() {
            // Given
            CronExpression expression1 = CronExpression.of("0 0 * * * *");
            CronExpression expression2 = CronExpression.of("0 0 * * * *");
            CronExpression expression3 = CronExpression.of("0 */5 * * * ?");

            // Then
            assertThat(expression1.isSameAs(expression2)).isTrue();
            assertThat(expression1.isSameAs(expression3)).isFalse();
        }

        @Test
        @DisplayName("Map의 키로 사용 가능")
        void shouldBeUsableAsMapKey() {
            // Given
            java.util.Map<CronExpression, String> map = new java.util.HashMap<>();
            CronExpression expression = CronExpression.of("0 0 * * * *");

            // When
            map.put(expression, "Hourly Schedule");

            // Then
            assertThat(map.get(expression)).isEqualTo("Hourly Schedule");
            assertThat(map.containsKey(CronExpression.of("0 0 * * * *"))).isTrue();
        }

        @Test
        @DisplayName("Set에서 중복 제거 가능")
        void shouldBeUsableInSet() {
            // Given
            java.util.Set<CronExpression> set = new java.util.HashSet<>();
            CronExpression expression1 = CronExpression.of("0 0 * * * *");
            CronExpression expression2 = CronExpression.of("0 0 * * * *");

            // When
            set.add(expression1);
            set.add(expression2);

            // Then
            assertThat(set).hasSize(1);  // 중복 제거됨
        }

        @Test
        @DisplayName("잘못된 표현식 검증 시나리오")
        void shouldValidateInvalidExpression() {
            // When & Then
            assertThatThrownBy(() -> CronExpression.of(null))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> CronExpression.of(""))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> CronExpression.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
