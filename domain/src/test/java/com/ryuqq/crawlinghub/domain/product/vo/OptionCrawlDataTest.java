package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("OptionCrawlData Value Object 단위 테스트")
class OptionCrawlDataTest {

    private static final ProductOptions DEFAULT_OPTIONS =
            ProductOptions.of(
                    List.of(
                            new ProductOption(1L, 100L, "Red", "M", 10, null),
                            new ProductOption(2L, 100L, "Blue", "L", 5, null)));
    private static final Instant UPDATED_AT = Instant.parse("2025-01-01T00:00:00Z");

    private OptionCrawlData createDefault() {
        return OptionCrawlData.of(DEFAULT_OPTIONS, UPDATED_AT);
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("유효한 값으로 생성한다")
        void createWithValidValues() {
            OptionCrawlData data = createDefault();

            assertThat(data.options()).isEqualTo(DEFAULT_OPTIONS);
            assertThat(data.updatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        @DisplayName("빈 옵션 목록으로 생성한다")
        void createWithEmptyOptions() {
            OptionCrawlData data = OptionCrawlData.of(ProductOptions.empty(), UPDATED_AT);

            assertThat(data.options().isEmpty()).isTrue();
            assertThat(data.updatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    @DisplayName("생성 실패 테스트 - null 검증")
    class NullValidationTest {

        @Test
        @DisplayName("options가 null이면 예외가 발생한다")
        void nullOptionsThrowsException() {
            assertThatThrownBy(() -> OptionCrawlData.of(null, UPDATED_AT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("options");
        }

        @Test
        @DisplayName("updatedAt이 null이면 예외가 발생한다")
        void nullUpdatedAtThrowsException() {
            assertThatThrownBy(() -> OptionCrawlData.of(DEFAULT_OPTIONS, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("updatedAt");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            OptionCrawlData data1 = createDefault();
            OptionCrawlData data2 = createDefault();

            assertThat(data1).isEqualTo(data2);
            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
        }

        @Test
        @DisplayName("다른 옵션이면 다르다")
        void differentOptionsAreNotEqual() {
            OptionCrawlData data1 = createDefault();
            OptionCrawlData data2 =
                    OptionCrawlData.of(
                            ProductOptions.of(
                                    List.of(new ProductOption(3L, 100L, "Green", "S", 3, null))),
                            UPDATED_AT);

            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("다른 updatedAt이면 다르다")
        void differentUpdatedAtAreNotEqual() {
            OptionCrawlData data1 = createDefault();
            OptionCrawlData data2 =
                    OptionCrawlData.of(DEFAULT_OPTIONS, Instant.parse("2025-06-01T00:00:00Z"));

            assertThat(data1).isNotEqualTo(data2);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("같은 파라미터로 생성하면 별개 인스턴스이지만 동등하다")
        void twoInstancesWithSameValuesAreEqualButNotSame() {
            OptionCrawlData data1 = createDefault();
            OptionCrawlData data2 = createDefault();

            assertThat(data1).isNotSameAs(data2);
            assertThat(data1).isEqualTo(data2);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString이 옵션 개수를 포함한다")
        void toStringContainsOptionsCount() {
            OptionCrawlData data = createDefault();
            assertThat(data.toString()).isNotNull();
            assertThat(data.toString()).contains("OptionCrawlData");
            assertThat(data.toString()).contains("optionsCount=2");
        }

        @Test
        @DisplayName("빈 옵션의 toString은 optionsCount=0을 포함한다")
        void toStringWithEmptyOptionsContainsZeroCount() {
            OptionCrawlData data = OptionCrawlData.of(ProductOptions.empty(), UPDATED_AT);
            assertThat(data.toString()).contains("optionsCount=0");
        }
    }
}
