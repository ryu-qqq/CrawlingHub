package com.ryuqq.crawlinghub.domain.product.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChangeSource 테스트")
class ChangeSourceTest {

    @Nested
    @DisplayName("Enum 값 테스트")
    class EnumValueTests {

        @Test
        @DisplayName("MINI_SHOP 상수 존재")
        void shouldHaveMiniShop() {
            // Given & When
            ChangeSource source = ChangeSource.MINI_SHOP;

            // Then
            assertThat(source).isNotNull();
            assertThat(source.name()).isEqualTo("MINI_SHOP");
        }

        @Test
        @DisplayName("OPTION 상수 존재")
        void shouldHaveOption() {
            // Given & When
            ChangeSource source = ChangeSource.OPTION;

            // Then
            assertThat(source).isNotNull();
            assertThat(source.name()).isEqualTo("OPTION");
        }

        @Test
        @DisplayName("DETAIL 상수 존재")
        void shouldHaveDetail() {
            // Given & When
            ChangeSource source = ChangeSource.DETAIL;

            // Then
            assertThat(source).isNotNull();
            assertThat(source.name()).isEqualTo("DETAIL");
        }

        @Test
        @DisplayName("Enum 값은 정확히 3개")
        void shouldHaveExactlyThreeValues() {
            // When
            ChangeSource[] values = ChangeSource.values();

            // Then
            assertThat(values).hasSize(3);
        }

        @ParameterizedTest
        @EnumSource(ChangeSource.class)
        @DisplayName("모든 Enum 값이 유효")
        void shouldHaveValidValues(ChangeSource source) {
            // Then
            assertThat(source).isNotNull();
            assertThat(source.name()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("valueOf() 메서드 테스트")
    class ValueOfTests {

        @Test
        @DisplayName("문자열 'MINI_SHOP'으로 Enum 생성")
        void shouldCreateFromMiniShopString() {
            // When
            ChangeSource source = ChangeSource.valueOf("MINI_SHOP");

            // Then
            assertThat(source).isEqualTo(ChangeSource.MINI_SHOP);
        }

        @Test
        @DisplayName("문자열 'OPTION'으로 Enum 생성")
        void shouldCreateFromOptionString() {
            // When
            ChangeSource source = ChangeSource.valueOf("OPTION");

            // Then
            assertThat(source).isEqualTo(ChangeSource.OPTION);
        }

        @Test
        @DisplayName("문자열 'DETAIL'로 Enum 생성")
        void shouldCreateFromDetailString() {
            // When
            ChangeSource source = ChangeSource.valueOf("DETAIL");

            // Then
            assertThat(source).isEqualTo(ChangeSource.DETAIL);
        }
    }

    @Nested
    @DisplayName("Enum 비교 테스트")
    class ComparisonTests {

        @Test
        @DisplayName("같은 Enum 값은 동일하다")
        void shouldBeEqualForSameEnum() {
            // Given
            ChangeSource source1 = ChangeSource.MINI_SHOP;
            ChangeSource source2 = ChangeSource.MINI_SHOP;

            // Then
            assertThat(source1).isEqualTo(source2);
            assertThat(source1).isSameAs(source2);  // Enum은 싱글톤
        }

        @Test
        @DisplayName("다른 Enum 값은 동일하지 않다")
        void shouldNotBeEqualForDifferentEnum() {
            // Given
            ChangeSource source1 = ChangeSource.MINI_SHOP;
            ChangeSource source2 = ChangeSource.OPTION;
            ChangeSource source3 = ChangeSource.DETAIL;

            // Then
            assertThat(source1).isNotEqualTo(source2);
            assertThat(source1).isNotEqualTo(source3);
            assertThat(source2).isNotEqualTo(source3);
        }

        @Test
        @DisplayName("Enum 순서 확인")
        void shouldHaveCorrectOrdinal() {
            // Given
            ChangeSource[] values = ChangeSource.values();

            // Then
            assertThat(values[0]).isEqualTo(ChangeSource.MINI_SHOP);
            assertThat(values[1]).isEqualTo(ChangeSource.OPTION);
            assertThat(values[2]).isEqualTo(ChangeSource.DETAIL);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("MINI_SHOP toString은 'MINI_SHOP' 반환")
        void shouldReturnMiniShopAsString() {
            // Given
            ChangeSource source = ChangeSource.MINI_SHOP;

            // When
            String result = source.toString();

            // Then
            assertThat(result).isEqualTo("MINI_SHOP");
        }

        @Test
        @DisplayName("OPTION toString은 'OPTION' 반환")
        void shouldReturnOptionAsString() {
            // Given
            ChangeSource source = ChangeSource.OPTION;

            // When
            String result = source.toString();

            // Then
            assertThat(result).isEqualTo("OPTION");
        }

        @Test
        @DisplayName("DETAIL toString은 'DETAIL' 반환")
        void shouldReturnDetailAsString() {
            // Given
            ChangeSource source = ChangeSource.DETAIL;

            // When
            String result = source.toString();

            // Then
            assertThat(result).isEqualTo("DETAIL");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("미니샵 크롤링 소스 시나리오")
        void shouldHandleMiniShopSource() {
            // Given
            ChangeSource source = ChangeSource.MINI_SHOP;

            // When & Then
            assertThat(source).isEqualTo(ChangeSource.MINI_SHOP);
            // 미니샵 크롤링: 이미지, 상품명, 가격 변경
        }

        @Test
        @DisplayName("옵션 크롤링 소스 시나리오")
        void shouldHandleOptionSource() {
            // Given
            ChangeSource source = ChangeSource.OPTION;

            // When & Then
            assertThat(source).isEqualTo(ChangeSource.OPTION);
            // 옵션 크롤링: 옵션, 재고 변경
        }

        @Test
        @DisplayName("상세 크롤링 소스 시나리오")
        void shouldHandleDetailSource() {
            // Given
            ChangeSource source = ChangeSource.DETAIL;

            // When & Then
            assertThat(source).isEqualTo(ChangeSource.DETAIL);
            // 상세 크롤링: 모듈 변경
        }

        @Test
        @DisplayName("Switch 문에서 모든 소스 처리 가능")
        void shouldHandleAllSourcesInSwitch() {
            // Given
            ChangeSource[] allSources = ChangeSource.values();

            // When & Then
            for (ChangeSource source : allSources) {
                String result = switch (source) {
                    case MINI_SHOP -> "미니샵";
                    case OPTION -> "옵션";
                    case DETAIL -> "상세";
                };
                assertThat(result).isNotBlank();
            }
        }
    }
}
