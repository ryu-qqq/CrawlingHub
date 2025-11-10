package com.ryuqq.crawlinghub.domain.product.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FieldChange 테스트")
class FieldChangeTest {

    private static final String FIELD_NAME = "productName";
    private static final String OLD_VALUE = "기존 상품명";
    private static final String NEW_VALUE = "새 상품명";

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 값으로 FieldChange 생성 성공")
        void shouldCreateFieldChange() {
            // When
            FieldChange fieldChange = new FieldChange(FIELD_NAME, OLD_VALUE, NEW_VALUE);

            // Then
            assertThat(fieldChange.fieldName()).isEqualTo(FIELD_NAME);
            assertThat(fieldChange.oldValue()).isEqualTo(OLD_VALUE);
            assertThat(fieldChange.newValue()).isEqualTo(NEW_VALUE);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("필드명이 null이면 예외 발생")
        void shouldThrowExceptionWhenFieldNameIsNull(String nullFieldName) {
            // When & Then
            assertThatThrownBy(() -> new FieldChange(nullFieldName, OLD_VALUE, NEW_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("필드명은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("필드명이 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenFieldNameIsBlank(String blankFieldName) {
            // When & Then
            assertThatThrownBy(() -> new FieldChange(blankFieldName, OLD_VALUE, NEW_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("필드명은 필수입니다");
        }

        @Test
        @DisplayName("oldValue가 null이어도 생성 가능")
        void shouldAllowNullOldValue() {
            // When
            FieldChange fieldChange = new FieldChange(FIELD_NAME, null, NEW_VALUE);

            // Then
            assertThat(fieldChange.fieldName()).isEqualTo(FIELD_NAME);
            assertThat(fieldChange.oldValue()).isNull();
            assertThat(fieldChange.newValue()).isEqualTo(NEW_VALUE);
        }

        @Test
        @DisplayName("newValue가 null이어도 생성 가능")
        void shouldAllowNullNewValue() {
            // When
            FieldChange fieldChange = new FieldChange(FIELD_NAME, OLD_VALUE, null);

            // Then
            assertThat(fieldChange.fieldName()).isEqualTo(FIELD_NAME);
            assertThat(fieldChange.oldValue()).isEqualTo(OLD_VALUE);
            assertThat(fieldChange.newValue()).isNull();
        }

        @Test
        @DisplayName("oldValue와 newValue 모두 null이어도 생성 가능")
        void shouldAllowBothValuesNull() {
            // When
            FieldChange fieldChange = new FieldChange(FIELD_NAME, null, null);

            // Then
            assertThat(fieldChange.fieldName()).isEqualTo(FIELD_NAME);
            assertThat(fieldChange.oldValue()).isNull();
            assertThat(fieldChange.newValue()).isNull();
        }
    }

    @Nested
    @DisplayName("isActuallyChanged() 메서드 테스트")
    class IsActuallyChangedTests {

        @Test
        @DisplayName("oldValue와 newValue가 다르면 true 반환")
        void shouldReturnTrueWhenValuesDiffer() {
            // Given
            FieldChange fieldChange = new FieldChange(FIELD_NAME, OLD_VALUE, NEW_VALUE);

            // When
            boolean result = fieldChange.isActuallyChanged();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("oldValue와 newValue가 같으면 false 반환")
        void shouldReturnFalseWhenValuesSame() {
            // Given
            FieldChange fieldChange = new FieldChange(FIELD_NAME, "동일값", "동일값");

            // When
            boolean result = fieldChange.isActuallyChanged();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("oldValue와 newValue가 모두 null이면 false 반환")
        void shouldReturnFalseWhenBothNull() {
            // Given
            FieldChange fieldChange = new FieldChange(FIELD_NAME, null, null);

            // When
            boolean result = fieldChange.isActuallyChanged();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("oldValue가 null이고 newValue가 값이면 true 반환")
        void shouldReturnTrueWhenOldNullNewValue() {
            // Given
            FieldChange fieldChange = new FieldChange(FIELD_NAME, null, NEW_VALUE);

            // When
            boolean result = fieldChange.isActuallyChanged();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("oldValue가 값이고 newValue가 null이면 true 반환")
        void shouldReturnTrueWhenOldValueNewNull() {
            // Given
            FieldChange fieldChange = new FieldChange(FIELD_NAME, OLD_VALUE, null);

            // When
            boolean result = fieldChange.isActuallyChanged();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("숫자 타입도 정확히 비교")
        void shouldCompareNumericTypes() {
            // Given
            FieldChange priceChange = new FieldChange("price", 10000L, 15000L);
            FieldChange samePrice = new FieldChange("price", 10000L, 10000L);

            // Then
            assertThat(priceChange.isActuallyChanged()).isTrue();
            assertThat(samePrice.isActuallyChanged()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record 동등성 테스트")
    class EqualityTests {

        @Test
        @DisplayName("모든 필드가 같으면 동일한 객체")
        void shouldBeEqualWhenAllFieldsSame() {
            // Given
            FieldChange change1 = new FieldChange(FIELD_NAME, OLD_VALUE, NEW_VALUE);
            FieldChange change2 = new FieldChange(FIELD_NAME, OLD_VALUE, NEW_VALUE);

            // Then
            assertThat(change1).isEqualTo(change2);
            assertThat(change1.hashCode()).isEqualTo(change2.hashCode());
        }

        @Test
        @DisplayName("필드명이 다르면 다른 객체")
        void shouldNotBeEqualWhenFieldNameDiffers() {
            // Given
            FieldChange change1 = new FieldChange("fieldA", OLD_VALUE, NEW_VALUE);
            FieldChange change2 = new FieldChange("fieldB", OLD_VALUE, NEW_VALUE);

            // Then
            assertThat(change1).isNotEqualTo(change2);
        }

        @Test
        @DisplayName("oldValue가 다르면 다른 객체")
        void shouldNotBeEqualWhenOldValueDiffers() {
            // Given
            FieldChange change1 = new FieldChange(FIELD_NAME, "oldA", NEW_VALUE);
            FieldChange change2 = new FieldChange(FIELD_NAME, "oldB", NEW_VALUE);

            // Then
            assertThat(change1).isNotEqualTo(change2);
        }

        @Test
        @DisplayName("newValue가 다르면 다른 객체")
        void shouldNotBeEqualWhenNewValueDiffers() {
            // Given
            FieldChange change1 = new FieldChange(FIELD_NAME, OLD_VALUE, "newA");
            FieldChange change2 = new FieldChange(FIELD_NAME, OLD_VALUE, "newB");

            // Then
            assertThat(change1).isNotEqualTo(change2);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 모든 필드를 포함")
        void shouldIncludeAllFieldsInToString() {
            // Given
            FieldChange fieldChange = new FieldChange(FIELD_NAME, OLD_VALUE, NEW_VALUE);

            // When
            String result = fieldChange.toString();

            // Then
            assertThat(result)
                .contains("FieldChange")
                .contains(FIELD_NAME)
                .contains(OLD_VALUE)
                .contains(NEW_VALUE);
        }

        @Test
        @DisplayName("null 값도 toString()에 포함")
        void shouldHandleNullInToString() {
            // Given
            FieldChange fieldChange = new FieldChange(FIELD_NAME, null, null);

            // When
            String result = fieldChange.toString();

            // Then
            assertThat(result)
                .contains("FieldChange")
                .contains(FIELD_NAME)
                .contains("null");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("상품명 변경 시나리오")
        void shouldHandleProductNameChange() {
            // Given
            FieldChange change = new FieldChange(
                "productName",
                "삼성 갤럭시 S24",
                "삼성 갤럭시 S24 Ultra"
            );

            // Then
            assertThat(change.isActuallyChanged()).isTrue();
            assertThat(change.fieldName()).isEqualTo("productName");
        }

        @Test
        @DisplayName("가격 변경 시나리오")
        void shouldHandlePriceChange() {
            // Given
            FieldChange change = new FieldChange(
                "price",
                1_200_000L,
                1_500_000L
            );

            // Then
            assertThat(change.isActuallyChanged()).isTrue();
            assertThat(change.oldValue()).isEqualTo(1_200_000L);
            assertThat(change.newValue()).isEqualTo(1_500_000L);
        }

        @Test
        @DisplayName("재고 변경 없음 시나리오 (같은 값)")
        void shouldDetectNoChangeScenario() {
            // Given
            FieldChange change = new FieldChange(
                "stock",
                100,
                100
            );

            // Then
            assertThat(change.isActuallyChanged()).isFalse();
        }

        @Test
        @DisplayName("신규 필드 추가 시나리오 (oldValue = null)")
        void shouldHandleNewFieldScenario() {
            // Given
            FieldChange change = new FieldChange(
                "description",
                null,
                "새로운 상품 설명"
            );

            // Then
            assertThat(change.isActuallyChanged()).isTrue();
            assertThat(change.oldValue()).isNull();
        }

        @Test
        @DisplayName("필드 삭제 시나리오 (newValue = null)")
        void shouldHandleFieldDeletionScenario() {
            // Given
            FieldChange change = new FieldChange(
                "optionalField",
                "기존값",
                null
            );

            // Then
            assertThat(change.isActuallyChanged()).isTrue();
            assertThat(change.newValue()).isNull();
        }
    }
}
