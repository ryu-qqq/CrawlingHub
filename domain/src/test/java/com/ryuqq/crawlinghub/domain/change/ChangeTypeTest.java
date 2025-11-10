package com.ryuqq.crawlinghub.domain.change;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * ChangeType 테스트
 */
@DisplayName("ChangeType 테스트")
class ChangeTypeTest {

    @Nested
    @DisplayName("Enum 상수 값 테스트")
    class EnumConstantTests {

        @Test
        @DisplayName("PRICE 상수의 priority와 description 검증")
        void shouldHaveCorrectPriceValues() {
            ChangeType type = ChangeType.PRICE;
            assertThat(type.getPriority()).isEqualTo(1);
            assertThat(type.getDescription()).isEqualTo("가격");
        }

        @Test
        @DisplayName("STOCK 상수의 priority와 description 검증")
        void shouldHaveCorrectStockValues() {
            ChangeType type = ChangeType.STOCK;
            assertThat(type.getPriority()).isEqualTo(2);
            assertThat(type.getDescription()).isEqualTo("재고");
        }

        @Test
        @DisplayName("OPTION 상수의 priority와 description 검증")
        void shouldHaveCorrectOptionValues() {
            ChangeType type = ChangeType.OPTION;
            assertThat(type.getPriority()).isEqualTo(3);
            assertThat(type.getDescription()).isEqualTo("옵션");
        }

        @Test
        @DisplayName("IMAGE 상수의 priority와 description 검증")
        void shouldHaveCorrectImageValues() {
            ChangeType type = ChangeType.IMAGE;
            assertThat(type.getPriority()).isEqualTo(4);
            assertThat(type.getDescription()).isEqualTo("이미지");
        }

        @Test
        @DisplayName("모든 Enum 상수는 정확히 4개")
        void shouldHaveExactlyFourValues() {
            ChangeType[] values = ChangeType.values();
            assertThat(values).hasSize(4);
            assertThat(values).containsExactly(
                ChangeType.PRICE,
                ChangeType.STOCK,
                ChangeType.OPTION,
                ChangeType.IMAGE
            );
        }

        @Test
        @DisplayName("priority 값은 1, 2, 3, 4 순서로 할당")
        void shouldHavePrioritiesInOrder() {
            assertThat(ChangeType.PRICE.getPriority()).isEqualTo(1);
            assertThat(ChangeType.STOCK.getPriority()).isEqualTo(2);
            assertThat(ChangeType.OPTION.getPriority()).isEqualTo(3);
            assertThat(ChangeType.IMAGE.getPriority()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("fromString() 팩토리 메서드 테스트")
    class FromStringTests {

        @Test
        @DisplayName("PRICE 문자열로 PRICE 상수 생성")
        void shouldCreatePriceFromString() {
            ChangeType type = ChangeType.fromString("PRICE");
            assertThat(type).isEqualTo(ChangeType.PRICE);
        }

        @Test
        @DisplayName("STOCK 문자열로 STOCK 상수 생성")
        void shouldCreateStockFromString() {
            ChangeType type = ChangeType.fromString("STOCK");
            assertThat(type).isEqualTo(ChangeType.STOCK);
        }

        @Test
        @DisplayName("OPTION 문자열로 OPTION 상수 생성")
        void shouldCreateOptionFromString() {
            ChangeType type = ChangeType.fromString("OPTION");
            assertThat(type).isEqualTo(ChangeType.OPTION);
        }

        @Test
        @DisplayName("IMAGE 문자열로 IMAGE 상수 생성")
        void shouldCreateImageFromString() {
            ChangeType type = ChangeType.fromString("IMAGE");
            assertThat(type).isEqualTo(ChangeType.IMAGE);
        }

        @Test
        @DisplayName("소문자 입력도 대문자로 변환하여 처리")
        void shouldHandleLowercaseInput() {
            ChangeType price = ChangeType.fromString("price");
            ChangeType stock = ChangeType.fromString("stock");
            ChangeType option = ChangeType.fromString("option");
            ChangeType image = ChangeType.fromString("image");

            assertThat(price).isEqualTo(ChangeType.PRICE);
            assertThat(stock).isEqualTo(ChangeType.STOCK);
            assertThat(option).isEqualTo(ChangeType.OPTION);
            assertThat(image).isEqualTo(ChangeType.IMAGE);
        }

        @Test
        @DisplayName("대소문자 혼합 입력도 처리")
        void shouldHandleMixedCaseInput() {
            ChangeType price = ChangeType.fromString("Price");
            ChangeType stock = ChangeType.fromString("StOcK");
            ChangeType option = ChangeType.fromString("OpTiOn");
            ChangeType image = ChangeType.fromString("ImAgE");

            assertThat(price).isEqualTo(ChangeType.PRICE);
            assertThat(stock).isEqualTo(ChangeType.STOCK);
            assertThat(option).isEqualTo(ChangeType.OPTION);
            assertThat(image).isEqualTo(ChangeType.IMAGE);
        }

        @Test
        @DisplayName("앞뒤 공백은 trim 처리")
        void shouldTrimWhitespace() {
            ChangeType price = ChangeType.fromString("  PRICE  ");
            ChangeType stock = ChangeType.fromString("\tSTOCK\t");
            ChangeType option = ChangeType.fromString("\nOPTION\n");
            ChangeType image = ChangeType.fromString(" IMAGE ");

            assertThat(price).isEqualTo(ChangeType.PRICE);
            assertThat(stock).isEqualTo(ChangeType.STOCK);
            assertThat(option).isEqualTo(ChangeType.OPTION);
            assertThat(image).isEqualTo(ChangeType.IMAGE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("null 또는 빈 문자열은 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullOrEmpty(String input) {
            assertThatThrownBy(() -> ChangeType.fromString(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ChangeType은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"UNKNOWN", "INVALID", "NOT_A_TYPE", "123", ""})
        @DisplayName("유효하지 않은 문자열은 IllegalArgumentException 발생")
        void shouldThrowExceptionForInvalidString(String input) {
            // 빈 문자열은 필수 검증 메시지, 나머지는 유효하지 않은 타입 메시지
            if (input.isBlank()) {
                assertThatThrownBy(() -> ChangeType.fromString(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ChangeType은 필수입니다");
            } else {
                assertThatThrownBy(() -> ChangeType.fromString(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 ChangeType입니다");
            }
        }
    }

    @Nested
    @DisplayName("Enum 기본 메서드 테스트")
    class EnumBasicMethodsTests {

        @Test
        @DisplayName("valueOf()로 Enum 상수 가져오기")
        void shouldGetEnumByValueOf() {
            ChangeType price = ChangeType.valueOf("PRICE");
            ChangeType stock = ChangeType.valueOf("STOCK");
            ChangeType option = ChangeType.valueOf("OPTION");
            ChangeType image = ChangeType.valueOf("IMAGE");

            assertThat(price).isEqualTo(ChangeType.PRICE);
            assertThat(stock).isEqualTo(ChangeType.STOCK);
            assertThat(option).isEqualTo(ChangeType.OPTION);
            assertThat(image).isEqualTo(ChangeType.IMAGE);
        }

        @Test
        @DisplayName("name() 메서드는 Enum 상수 이름 반환")
        void shouldReturnNameByNameMethod() {
            assertThat(ChangeType.PRICE.name()).isEqualTo("PRICE");
            assertThat(ChangeType.STOCK.name()).isEqualTo("STOCK");
            assertThat(ChangeType.OPTION.name()).isEqualTo("OPTION");
            assertThat(ChangeType.IMAGE.name()).isEqualTo("IMAGE");
        }

        @Test
        @DisplayName("ordinal() 메서드는 선언 순서 반환")
        void shouldReturnOrdinalByOrdinalMethod() {
            assertThat(ChangeType.PRICE.ordinal()).isEqualTo(0);
            assertThat(ChangeType.STOCK.ordinal()).isEqualTo(1);
            assertThat(ChangeType.OPTION.ordinal()).isEqualTo(2);
            assertThat(ChangeType.IMAGE.ordinal()).isEqualTo(3);
        }

        @Test
        @DisplayName("toString() 메서드는 Enum 상수 이름 반환")
        void shouldReturnNameByToStringMethod() {
            assertThat(ChangeType.PRICE.toString()).isEqualTo("PRICE");
            assertThat(ChangeType.STOCK.toString()).isEqualTo("STOCK");
            assertThat(ChangeType.OPTION.toString()).isEqualTo("OPTION");
            assertThat(ChangeType.IMAGE.toString()).isEqualTo("IMAGE");
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 Enum 상수는 equals로 동일")
        void shouldBeEqualForSameConstant() {
            ChangeType price1 = ChangeType.PRICE;
            ChangeType price2 = ChangeType.PRICE;

            assertThat(price1).isEqualTo(price2);
            assertThat(price1 == price2).isTrue();
        }

        @Test
        @DisplayName("다른 Enum 상수는 equals로 다름")
        void shouldNotBeEqualForDifferentConstant() {
            ChangeType price = ChangeType.PRICE;
            ChangeType stock = ChangeType.STOCK;

            assertThat(price).isNotEqualTo(stock);
        }

        @Test
        @DisplayName("같은 Enum 상수는 같은 hashCode")
        void shouldHaveSameHashCodeForSameConstant() {
            ChangeType price1 = ChangeType.PRICE;
            ChangeType price2 = ChangeType.PRICE;

            assertThat(price1.hashCode()).isEqualTo(price2.hashCode());
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("Switch 문에서 사용")
        void shouldWorkInSwitchStatement() {
            ChangeType type = ChangeType.PRICE;

            String result = switch (type) {
                case PRICE -> "가격 변경";
                case STOCK -> "재고 변경";
                case OPTION -> "옵션 변경";
                case IMAGE -> "이미지 변경";
            };

            assertThat(result).isEqualTo("가격 변경");
        }

        @Test
        @DisplayName("Map의 키로 사용")
        void shouldBeUsableAsMapKey() {
            java.util.Map<ChangeType, String> typeMap = new java.util.HashMap<>();
            typeMap.put(ChangeType.PRICE, "가격");
            typeMap.put(ChangeType.STOCK, "재고");
            typeMap.put(ChangeType.OPTION, "옵션");
            typeMap.put(ChangeType.IMAGE, "이미지");

            assertThat(typeMap.get(ChangeType.PRICE)).isEqualTo("가격");
        }

        @Test
        @DisplayName("EnumSet에서 사용")
        void shouldWorkInEnumSet() {
            java.util.EnumSet<ChangeType> criticalTypes =
                java.util.EnumSet.of(ChangeType.PRICE, ChangeType.STOCK);

            assertThat(criticalTypes).containsExactlyInAnyOrder(
                ChangeType.PRICE,
                ChangeType.STOCK
            );
            assertThat(criticalTypes).doesNotContain(ChangeType.OPTION, ChangeType.IMAGE);
        }

        @Test
        @DisplayName("외부 입력 처리 시나리오")
        void shouldHandleExternalInputScenario() {
            // 사용자 입력: 소문자, 공백 포함
            String userInput = "  price  ";
            ChangeType type = ChangeType.fromString(userInput);

            // 올바른 상수로 변환
            assertThat(type).isEqualTo(ChangeType.PRICE);
            assertThat(type.getDescription()).isEqualTo("가격");
            assertThat(type.getPriority()).isEqualTo(1);
        }

        @Test
        @DisplayName("우선순위 기반 정렬 시나리오")
        void shouldSortByPriorityScenario() {
            java.util.List<ChangeType> types = java.util.Arrays.asList(
                ChangeType.IMAGE,
                ChangeType.OPTION,
                ChangeType.PRICE,
                ChangeType.STOCK
            );

            types.sort(java.util.Comparator.comparingInt(ChangeType::getPriority));

            assertThat(types).containsExactly(
                ChangeType.PRICE,   // priority 1
                ChangeType.STOCK,   // priority 2
                ChangeType.OPTION,  // priority 3
                ChangeType.IMAGE    // priority 4
            );
        }

        @Test
        @DisplayName("변경 유형별 처리 시나리오")
        void shouldHandleChangeTypeScenario() {
            // Given: 4가지 변경 유형
            java.util.List<ChangeType> allTypes = java.util.Arrays.asList(
                ChangeType.PRICE,
                ChangeType.STOCK,
                ChangeType.OPTION,
                ChangeType.IMAGE
            );

            // When: 각 유형별 설명 가져오기
            java.util.List<String> descriptions = allTypes.stream()
                .map(ChangeType::getDescription)
                .toList();

            // Then: 올바른 설명 반환
            assertThat(descriptions).containsExactly("가격", "재고", "옵션", "이미지");
        }
    }
}
