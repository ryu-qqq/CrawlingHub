package com.ryuqq.crawlinghub.domain.seller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SellerName 테스트")
class SellerNameTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("유효한 셀러 이름으로 SellerName 생성")
        void shouldCreateWithValidName() {
            // Given
            String name = "Mustit Seller";

            // When
            SellerName sellerName = SellerName.of(name);

            // Then
            assertThat(sellerName).isNotNull();
            assertThat(sellerName.getValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("1자 셀러 이름으로 생성 (최소 길이)")
        void shouldCreateWithMinimumLength() {
            // Given
            String name = "A";

            // When
            SellerName sellerName = SellerName.of(name);

            // Then
            assertThat(sellerName).isNotNull();
            assertThat(sellerName.getValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("100자 셀러 이름으로 생성 (최대 길이)")
        void shouldCreateWithMaximumLength() {
            // Given
            String name = "A".repeat(100);

            // When
            SellerName sellerName = SellerName.of(name);

            // Then
            assertThat(sellerName).isNotNull();
            assertThat(sellerName.getValue()).isEqualTo(name);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Mustit",
            "머스트잇",
            "Seller123",
            "셀러 이름",
            "A B C",
            "Test-Seller"
        })
        @DisplayName("다양한 셀러 이름으로 생성 가능")
        void shouldCreateWithVariousNames(String name) {
            // When
            SellerName sellerName = SellerName.of(name);

            // Then
            assertThat(sellerName).isNotNull();
            assertThat(sellerName.getValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("앞뒤 공백이 있는 이름도 trim 처리 후 생성")
        void shouldTrimWhitespace() {
            // Given
            String nameWithWhitespace = "  Mustit  ";

            // When
            SellerName sellerName = SellerName.of(nameWithWhitespace);

            // Then
            assertThat(sellerName).isNotNull();
            // trim()은 검증에만 사용되고, 원본 값이 저장됨
            assertThat(sellerName.getValue()).isEqualTo(nameWithWhitespace);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 셀러 이름은 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullOrEmpty(String name) {
            // When & Then
            assertThatThrownBy(() -> SellerName.of(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t   "})
        @DisplayName("공백 문자만 있는 셀러 이름은 IllegalArgumentException 발생")
        void shouldThrowExceptionForBlankName(String name) {
            // When & Then
            assertThatThrownBy(() -> SellerName.of(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");
        }

        @Test
        @DisplayName("101자 이상 셀러 이름은 IllegalArgumentException 발생")
        void shouldThrowExceptionForTooLongName() {
            // Given
            String name = "A".repeat(101);

            // When & Then
            assertThatThrownBy(() -> SellerName.of(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1자 이상 100자 이하여야 합니다");
        }

        @Test
        @DisplayName("공백으로 둘러싸인 0자는 IllegalArgumentException 발생")
        void shouldThrowExceptionForZeroLengthAfterTrim() {
            // Given
            String name = "     "; // trim 후 빈 문자열

            // When & Then
            assertThatThrownBy(() -> SellerName.of(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");
        }
    }

    @Nested
    @DisplayName("getValue() 메서드 테스트")
    class GetValueTests {

        @Test
        @DisplayName("getValue()는 생성 시 전달된 이름 반환")
        void shouldReturnOriginalName() {
            // Given
            String name = "Mustit Seller";
            SellerName sellerName = SellerName.of(name);

            // When
            String result = sellerName.getValue();

            // Then
            assertThat(result).isEqualTo(name);
        }

        @Test
        @DisplayName("getValue()는 null이 아님")
        void shouldReturnNonNullValue() {
            // Given
            SellerName sellerName = SellerName.of("Mustit");

            // When
            String result = sellerName.getValue();

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("getValue()는 비어있지 않음")
        void shouldReturnNonEmptyValue() {
            // Given
            SellerName sellerName = SellerName.of("Mustit");

            // When
            String result = sellerName.getValue();

            // Then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("getValue()는 불변 (여러 번 호출해도 동일)")
        void shouldBeImmutable() {
            // Given
            String name = "Mustit Seller";
            SellerName sellerName = SellerName.of(name);

            // When
            String result1 = sellerName.getValue();
            String result2 = sellerName.getValue();

            // Then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1).isSameAs(result2);
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 이름으로 생성된 두 객체는 동일")
        void shouldBeEqualForSameName() {
            // Given
            String name = "Mustit Seller";
            SellerName sellerName1 = SellerName.of(name);
            SellerName sellerName2 = SellerName.of(name);

            // Then
            assertThat(sellerName1).isEqualTo(sellerName2);
            assertThat(sellerName1.hashCode()).isEqualTo(sellerName2.hashCode());
        }

        @Test
        @DisplayName("다른 이름으로 생성된 두 객체는 다름")
        void shouldNotBeEqualForDifferentName() {
            // Given
            SellerName sellerName1 = SellerName.of("Mustit");
            SellerName sellerName2 = SellerName.of("Other Seller");

            // Then
            assertThat(sellerName1).isNotEqualTo(sellerName2);
        }

        @Test
        @DisplayName("같은 객체는 자기 자신과 동일")
        void shouldBeEqualToItself() {
            // Given
            SellerName sellerName = SellerName.of("Mustit");

            // Then
            assertThat(sellerName).isEqualTo(sellerName);
        }

        @Test
        @DisplayName("null과는 동일하지 않음")
        void shouldNotBeEqualToNull() {
            // Given
            SellerName sellerName = SellerName.of("Mustit");

            // Then
            assertThat(sellerName).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 동일하지 않음")
        void shouldNotBeEqualToDifferentType() {
            // Given
            SellerName sellerName = SellerName.of("Mustit");
            String otherObject = "Mustit";

            // Then
            assertThat(sellerName).isNotEqualTo(otherObject);
        }

        @Test
        @DisplayName("hashCode는 일관성 있게 반환")
        void shouldReturnConsistentHashCode() {
            // Given
            SellerName sellerName = SellerName.of("Mustit");

            // When
            int hashCode1 = sellerName.hashCode();
            int hashCode2 = sellerName.hashCode();

            // Then
            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("같은 이름은 같은 hashCode를 가짐")
        void shouldHaveSameHashCodeForSameName() {
            // Given
            String name = "Mustit Seller";
            SellerName sellerName1 = SellerName.of(name);
            SellerName sellerName2 = SellerName.of(name);

            // Then
            assertThat(sellerName1.hashCode()).isEqualTo(sellerName2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 null이 아님")
        void shouldHaveNonNullToString() {
            // Given
            SellerName sellerName = SellerName.of("Mustit");

            // When
            String result = sellerName.toString();

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toString()은 비어있지 않음")
        void shouldHaveNonEmptyToString() {
            // Given
            SellerName sellerName = SellerName.of("Mustit");

            // When
            String result = sellerName.toString();

            // Then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("toString()은 이름을 포함")
        void shouldContainNameInToString() {
            // Given
            String name = "Mustit Seller";
            SellerName sellerName = SellerName.of(name);

            // When
            String result = sellerName.toString();

            // Then
            assertThat(result).contains(name);
        }

        @Test
        @DisplayName("toString()은 클래스명 포함")
        void shouldContainClassNameInToString() {
            // Given
            SellerName sellerName = SellerName.of("Mustit");

            // When
            String result = sellerName.toString();

            // Then
            assertThat(result).contains("SellerName");
        }
    }

    @Nested
    @DisplayName("Value Object 특성 테스트")
    class ValueObjectCharacteristicsTests {

        @Test
        @DisplayName("Value Object는 불변 (immutable)")
        void shouldBeImmutable() {
            // Given
            String name = "Mustit Seller";
            SellerName sellerName = SellerName.of(name);

            // When
            String retrievedValue = sellerName.getValue();

            // Then
            assertThat(retrievedValue).isEqualTo(name);
            // Value Object는 불변이므로 값을 변경할 수 없음
        }

        @Test
        @DisplayName("Value Object는 값 기반 동등성을 가짐")
        void shouldHaveValueBasedEquality() {
            // Given
            String name = "Mustit Seller";
            SellerName sellerName1 = SellerName.of(name);
            SellerName sellerName2 = SellerName.of(name);

            // Then
            assertThat(sellerName1).isEqualTo(sellerName2);
            assertThat(sellerName1).isNotSameAs(sellerName2);  // 다른 인스턴스
        }

        @Test
        @DisplayName("private 생성자를 가짐 (factory method만 사용)")
        void shouldHavePrivateConstructor() {
            // Given
            Class<SellerName> clazz = SellerName.class;

            // Then
            assertThat(clazz.getDeclaredConstructors())
                .allMatch(constructor -> java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("일반적인 셀러 이름 생성 시나리오")
        void shouldCreateCommonSellerName() {
            // Given
            String name = "머스트잇";

            // When
            SellerName sellerName = SellerName.of(name);

            // Then
            assertThat(sellerName.getValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("영문 셀러 이름 생성 시나리오")
        void shouldCreateEnglishName() {
            // Given
            String name = "Mustit Seller";

            // When
            SellerName sellerName = SellerName.of(name);

            // Then
            assertThat(sellerName.getValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("숫자 포함 셀러 이름 생성 시나리오")
        void shouldCreateNameWithNumbers() {
            // Given
            String name = "Seller123";

            // When
            SellerName sellerName = SellerName.of(name);

            // Then
            assertThat(sellerName.getValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("셀러 이름 변경 시나리오 (새 객체 생성)")
        void shouldCreateNewObjectForChange() {
            // Given
            SellerName oldName = SellerName.of("Old Seller");

            // When
            SellerName newName = SellerName.of("New Seller");

            // Then
            assertThat(oldName).isNotEqualTo(newName);
            assertThat(oldName.getValue()).isEqualTo("Old Seller");
            assertThat(newName.getValue()).isEqualTo("New Seller");
        }

        @Test
        @DisplayName("셀러 이름 비교 시나리오")
        void shouldCompareSellerNames() {
            // Given
            SellerName name1 = SellerName.of("Mustit");
            SellerName name2 = SellerName.of("Mustit");
            SellerName name3 = SellerName.of("Other");

            // Then
            assertThat(name1).isEqualTo(name2);
            assertThat(name1).isNotEqualTo(name3);
        }

        @Test
        @DisplayName("Map의 키로 사용 가능")
        void shouldBeUsableAsMapKey() {
            // Given
            java.util.Map<SellerName, String> map = new java.util.HashMap<>();
            SellerName name = SellerName.of("Mustit");

            // When
            map.put(name, "Seller Info");

            // Then
            assertThat(map.get(name)).isEqualTo("Seller Info");
            assertThat(map.containsKey(SellerName.of("Mustit"))).isTrue();
        }

        @Test
        @DisplayName("Set에서 중복 제거 가능")
        void shouldBeUsableInSet() {
            // Given
            java.util.Set<SellerName> set = new java.util.HashSet<>();
            SellerName name1 = SellerName.of("Mustit");
            SellerName name2 = SellerName.of("Mustit");

            // When
            set.add(name1);
            set.add(name2);

            // Then
            assertThat(set).hasSize(1);  // 중복 제거됨
        }

        @Test
        @DisplayName("잘못된 셀러 이름 검증 시나리오")
        void shouldValidateInvalidName() {
            // When & Then
            assertThatThrownBy(() -> SellerName.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");

            assertThatThrownBy(() -> SellerName.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");

            assertThatThrownBy(() -> SellerName.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");

            assertThatThrownBy(() -> SellerName.of("A".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1자 이상 100자 이하여야 합니다");
        }

        @Test
        @DisplayName("경계값 테스트 - 1자 (최소 허용)")
        void shouldAllowMinimumBoundary() {
            // Given
            String name = "A";

            // When
            SellerName sellerName = SellerName.of(name);

            // Then
            assertThat(sellerName.getValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("경계값 테스트 - 100자 (최대 허용)")
        void shouldAllowMaximumBoundary() {
            // Given
            String name = "A".repeat(100);

            // When
            SellerName sellerName = SellerName.of(name);

            // Then
            assertThat(sellerName.getValue()).isEqualTo(name);
        }
    }
}
