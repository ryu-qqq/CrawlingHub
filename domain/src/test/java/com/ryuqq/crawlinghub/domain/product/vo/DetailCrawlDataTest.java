package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("DetailCrawlData Value Object 단위 테스트")
class DetailCrawlDataTest {

    private static final Instant NOW = Instant.parse("2025-11-27T00:00:00Z");

    private DetailCrawlData defaultData() {
        return DetailCrawlData.of(
                0L,
                ProductCategory.of("W", "여성", "001", "가방", "A01", "백팩"),
                ShippingInfo.freeShipping("DOMESTIC", 3),
                "<p>상품 설명</p>",
                "새상품",
                "한국",
                "서울",
                List.of("https://img.com/detail.jpg"),
                NOW);
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("모든 필드로 DetailCrawlData를 생성한다")
        void createWithAllFields() {
            DetailCrawlData data = defaultData();

            assertThat(data.category()).isNotNull();
            assertThat(data.shippingInfo()).isNotNull();
            assertThat(data.descriptionMarkUp()).isEqualTo("<p>상품 설명</p>");
            assertThat(data.itemStatus()).isEqualTo("새상품");
            assertThat(data.originCountry()).isEqualTo("한국");
            assertThat(data.shippingLocation()).isEqualTo("서울");
            assertThat(data.descriptionImages()).containsExactly("https://img.com/detail.jpg");
            assertThat(data.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("updatedAt이 null이면 예외가 발생한다")
        void nullUpdatedAtThrowsException() {
            assertThatThrownBy(
                            () ->
                                    DetailCrawlData.of(
                                            0L, null, null, null, null, null, null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("descriptionImages가 null이면 빈 리스트로 방어적 복사된다")
        void nullDescriptionImagesBecomesEmptyList() {
            DetailCrawlData data =
                    DetailCrawlData.of(0L, null, null, null, null, null, null, null, NOW);

            assertThat(data.descriptionImages()).isEmpty();
        }

        @Test
        @DisplayName("descriptionImages는 방어적 복사된다")
        void descriptionImagesAreDefensivelyCopied() {
            List<String> mutableList = new ArrayList<>();
            mutableList.add("https://img.com/1.jpg");

            DetailCrawlData data =
                    DetailCrawlData.of(0L, null, null, null, null, null, null, mutableList, NOW);

            mutableList.add("https://img.com/2.jpg");

            assertThat(data.descriptionImages()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 필드이면 동일하다")
        void sameFieldsAreEqual() {
            DetailCrawlData data1 = defaultData();
            DetailCrawlData data2 = defaultData();

            assertThat(data1).isEqualTo(data2);
            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
        }

        @Test
        @DisplayName("다른 updatedAt이면 다르다")
        void differentUpdatedAtAreNotEqual() {
            DetailCrawlData data1 = defaultData();
            DetailCrawlData data2 =
                    DetailCrawlData.of(
                            0L, null, null, null, null, null, null, null, NOW.plusSeconds(3600));

            assertThat(data1).isNotEqualTo(data2);
        }
    }
}
