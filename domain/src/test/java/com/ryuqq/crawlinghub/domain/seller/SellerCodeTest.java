package com.ryuqq.crawlinghub.domain.seller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SellerCode 테스트")
class SellerCodeTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("유효한 셀러 코드로 SellerCode 생성")
        void shouldCreateWithValidCode() {
            // Given
            String code = "SELLER001";

            // When
            SellerCode sellerCode = SellerCode.of(code);

            // Then
            assertThat(sellerCode).isNotNull();
            assertThat(sellerCode.getValue()).isEqualTo(code);
        }

        @Test
        @DisplayName("짧은 셀러 코드로 생성")
        void shouldCreateWithShortCode() {
            // Given
            String code = "S1";

            // When
            SellerCode sellerCode = SellerCode.of(code);

            // Then
            assertThat(sellerCode).isNotNull();
            assertThat(sellerCode.getValue()).isEqualTo(code);
        }

        @Test
        @DisplayName("긴 셀러 코드로 생성")
        void shouldCreateWithLongCode() {
            // Given
            String code = "VERY_LONG_SELLER_CODE_WITH_MANY_CHARACTERS_12345";

            // When
            SellerCode sellerCode = SellerCode.of(code);

            // Then
            assertThat(sellerCode).isNotNull();
            assertThat(sellerCode.getValue()).isEqualTo(code);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "SELLER001",
            "seller-code-123",
            "ABC",
            "12345",
            "SELLER_CODE_WITH_UNDERSCORE"
        })
        @DisplayName("다양한 셀러 코드로 생성 가능")
        void shouldCreateWithVariousCodes(String code) {
            // When
            SellerCode sellerCode = SellerCode.of(code);

            // Then
            assertThat(sellerCode).isNotNull();
            assertThat(sellerCode.getValue()).isEqualTo(code);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 셀러 코드는 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullOrEmpty(String code) {
            // When & Then
            assertThatThrownBy(() -> SellerCode.of(code))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 코드는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t   "})
        @DisplayName("공백 문자만 있는 셀러 코드는 IllegalArgumentException 발생")
        void shouldThrowExceptionForBlankCode(String code) {
            // When & Then
            assertThatThrownBy(() -> SellerCode.of(code))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 코드는 필수입니다");
        }
    }

    @Nested
    @DisplayName("getValue() 메서드 테스트")
    class GetValueTests {

        @Test
        @DisplayName("getValue()는 생성 시 전달된 코드 반환")
        void shouldReturnOriginalCode() {
            // Given
            String code = "SELLER001";
            SellerCode sellerCode = SellerCode.of(code);

            // When
            String result = sellerCode.getValue();

            // Then
            assertThat(result).isEqualTo(code);
        }

        @Test
        @DisplayName("getValue()는 null이 아님")
        void shouldReturnNonNullValue() {
            // Given
            SellerCode sellerCode = SellerCode.of("SELLER001");

            // When
            String result = sellerCode.getValue();

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("getValue()는 비어있지 않음")
        void shouldReturnNonEmptyValue() {
            // Given
            SellerCode sellerCode = SellerCode.of("SELLER001");

            // When
            String result = sellerCode.getValue();

            // Then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("getValue()는 불변 (여러 번 호출해도 동일)")
        void shouldBeImmutable() {
            // Given
            String code = "SELLER001";
            SellerCode sellerCode = SellerCode.of(code);

            // When
            String result1 = sellerCode.getValue();
            String result2 = sellerCode.getValue();

            // Then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1).isSameAs(result2);
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 코드로 생성된 두 객체는 동일")
        void shouldBeEqualForSameCode() {
            // Given
            String code = "SELLER001";
            SellerCode sellerCode1 = SellerCode.of(code);
            SellerCode sellerCode2 = SellerCode.of(code);

            // Then
            assertThat(sellerCode1).isEqualTo(sellerCode2);
            assertThat(sellerCode1.hashCode()).isEqualTo(sellerCode2.hashCode());
        }

        @Test
        @DisplayName("다른 코드로 생성된 두 객체는 다름")
        void shouldNotBeEqualForDifferentCode() {
            // Given
            SellerCode sellerCode1 = SellerCode.of("SELLER001");
            SellerCode sellerCode2 = SellerCode.of("SELLER002");

            // Then
            assertThat(sellerCode1).isNotEqualTo(sellerCode2);
        }

        @Test
        @DisplayName("같은 객체는 자기 자신과 동일")
        void shouldBeEqualToItself() {
            // Given
            SellerCode sellerCode = SellerCode.of("SELLER001");

            // Then
            assertThat(sellerCode).isEqualTo(sellerCode);
        }

        @Test
        @DisplayName("null과는 동일하지 않음")
        void shouldNotBeEqualToNull() {
            // Given
            SellerCode sellerCode = SellerCode.of("SELLER001");

            // Then
            assertThat(sellerCode).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 동일하지 않음")
        void shouldNotBeEqualToDifferentType() {
            // Given
            SellerCode sellerCode = SellerCode.of("SELLER001");
            String otherObject = "SELLER001";

            // Then
            assertThat(sellerCode).isNotEqualTo(otherObject);
        }

        @Test
        @DisplayName("hashCode는 일관성 있게 반환")
        void shouldReturnConsistentHashCode() {
            // Given
            SellerCode sellerCode = SellerCode.of("SELLER001");

            // When
            int hashCode1 = sellerCode.hashCode();
            int hashCode2 = sellerCode.hashCode();

            // Then
            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("같은 코드는 같은 hashCode를 가짐")
        void shouldHaveSameHashCodeForSameCode() {
            // Given
            String code = "SELLER001";
            SellerCode sellerCode1 = SellerCode.of(code);
            SellerCode sellerCode2 = SellerCode.of(code);

            // Then
            assertThat(sellerCode1.hashCode()).isEqualTo(sellerCode2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 null이 아님")
        void shouldHaveNonNullToString() {
            // Given
            SellerCode sellerCode = SellerCode.of("SELLER001");

            // When
            String result = sellerCode.toString();

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toString()은 비어있지 않음")
        void shouldHaveNonEmptyToString() {
            // Given
            SellerCode sellerCode = SellerCode.of("SELLER001");

            // When
            String result = sellerCode.toString();

            // Then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("toString()은 코드 값을 포함")
        void shouldContainCodeInToString() {
            // Given
            String code = "SELLER001";
            SellerCode sellerCode = SellerCode.of(code);

            // When
            String result = sellerCode.toString();

            // Then
            assertThat(result).contains(code);
        }

        @Test
        @DisplayName("toString()은 클래스명 포함")
        void shouldContainClassNameInToString() {
            // Given
            SellerCode sellerCode = SellerCode.of("SELLER001");

            // When
            String result = sellerCode.toString();

            // Then
            assertThat(result).contains("SellerCode");
        }
    }

    @Nested
    @DisplayName("Value Object 특성 테스트")
    class ValueObjectCharacteristicsTests {

        @Test
        @DisplayName("Value Object는 불변 (immutable)")
        void shouldBeImmutable() {
            // Given
            String code = "SELLER001";
            SellerCode sellerCode = SellerCode.of(code);

            // When
            String retrievedValue = sellerCode.getValue();

            // Then
            assertThat(retrievedValue).isEqualTo(code);
            // Value Object는 불변이므로 값을 변경할 수 없음
        }

        @Test
        @DisplayName("Value Object는 값 기반 동등성을 가짐")
        void shouldHaveValueBasedEquality() {
            // Given
            String code = "SELLER001";
            SellerCode sellerCode1 = SellerCode.of(code);
            SellerCode sellerCode2 = SellerCode.of(code);

            // Then
            assertThat(sellerCode1).isEqualTo(sellerCode2);
            assertThat(sellerCode1).isNotSameAs(sellerCode2);  // 다른 인스턴스
        }

        @Test
        @DisplayName("private 생성자를 가짐 (factory method만 사용)")
        void shouldHavePrivateConstructor() {
            // Given
            Class<SellerCode> clazz = SellerCode.class;

            // Then
            assertThat(clazz.getDeclaredConstructors())
                .allMatch(constructor -> java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("일반적인 셀러 코드 생성 시나리오")
        void shouldCreateCommonSellerCode() {
            // Given
            String code = "MUSTIT_SELLER_001";

            // When
            SellerCode sellerCode = SellerCode.of(code);

            // Then
            assertThat(sellerCode.getValue()).isEqualTo(code);
        }

        @Test
        @DisplayName("숫자 코드 생성 시나리오")
        void shouldCreateNumericCode() {
            // Given
            String code = "123456";

            // When
            SellerCode sellerCode = SellerCode.of(code);

            // Then
            assertThat(sellerCode.getValue()).isEqualTo(code);
        }

        @Test
        @DisplayName("셀러 코드 변경 시나리오 (새 객체 생성)")
        void shouldCreateNewObjectForChange() {
            // Given
            SellerCode oldCode = SellerCode.of("OLD_CODE");

            // When
            SellerCode newCode = SellerCode.of("NEW_CODE");

            // Then
            assertThat(oldCode).isNotEqualTo(newCode);
            assertThat(oldCode.getValue()).isEqualTo("OLD_CODE");
            assertThat(newCode.getValue()).isEqualTo("NEW_CODE");
        }

        @Test
        @DisplayName("셀러 코드 비교 시나리오")
        void shouldCompareSellerCodes() {
            // Given
            SellerCode code1 = SellerCode.of("SELLER001");
            SellerCode code2 = SellerCode.of("SELLER001");
            SellerCode code3 = SellerCode.of("SELLER002");

            // Then
            assertThat(code1).isEqualTo(code2);
            assertThat(code1).isNotEqualTo(code3);
        }

        @Test
        @DisplayName("Map의 키로 사용 가능")
        void shouldBeUsableAsMapKey() {
            // Given
            java.util.Map<SellerCode, String> map = new java.util.HashMap<>();
            SellerCode code = SellerCode.of("SELLER001");

            // When
            map.put(code, "Mustit Seller");

            // Then
            assertThat(map.get(code)).isEqualTo("Mustit Seller");
            assertThat(map.containsKey(SellerCode.of("SELLER001"))).isTrue();
        }

        @Test
        @DisplayName("Set에서 중복 제거 가능")
        void shouldBeUsableInSet() {
            // Given
            java.util.Set<SellerCode> set = new java.util.HashSet<>();
            SellerCode code1 = SellerCode.of("SELLER001");
            SellerCode code2 = SellerCode.of("SELLER001");

            // When
            set.add(code1);
            set.add(code2);

            // Then
            assertThat(set).hasSize(1);  // 중복 제거됨
        }

        @Test
        @DisplayName("잘못된 셀러 코드 검증 시나리오")
        void shouldValidateInvalidCode() {
            // When & Then
            assertThatThrownBy(() -> SellerCode.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 코드는 필수입니다");

            assertThatThrownBy(() -> SellerCode.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 코드는 필수입니다");

            assertThatThrownBy(() -> SellerCode.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 코드는 필수입니다");
        }
    }
}
