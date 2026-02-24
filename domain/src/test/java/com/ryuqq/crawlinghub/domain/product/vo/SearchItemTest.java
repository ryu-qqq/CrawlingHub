package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("SearchItem Value Object 단위 테스트")
class SearchItemTest {

    @Nested
    @DisplayName("생성 검증 테스트")
    class CreationValidationTest {

        @Test
        @DisplayName("itemNo가 null이면 예외가 발생한다")
        void nullItemNoThrowsException() {
            assertThatThrownBy(
                            () ->
                                    new SearchItem(
                                            null,
                                            List.of("https://img.com/1.jpg"),
                                            "브랜드",
                                            "상품명",
                                            "10000",
                                            "20000",
                                            "50",
                                            List.of(),
                                            "PAID"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemNo");
        }

        @Test
        @DisplayName("name이 null이면 예외가 발생한다")
        void nullNameThrowsException() {
            assertThatThrownBy(
                            () ->
                                    new SearchItem(
                                            1L,
                                            List.of("https://img.com/1.jpg"),
                                            "브랜드",
                                            null,
                                            "10000",
                                            "20000",
                                            "50",
                                            List.of(),
                                            "PAID"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("name이 빈 문자열이면 예외가 발생한다")
        void blankNameThrowsException() {
            assertThatThrownBy(
                            () ->
                                    new SearchItem(
                                            1L, List.of(), "브랜드", "   ", "10000", "20000", "50",
                                            List.of(), "PAID"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("imageUrlList가 null이면 빈 리스트로 방어적 복사된다")
        void nullImageUrlListBecomesEmptyList() {
            SearchItem item =
                    new SearchItem(1L, null, "브랜드", "상품명", "10000", "20000", "50", null, "PAID");

            assertThat(item.imageUrlList()).isEmpty();
        }

        @Test
        @DisplayName("tagList가 null이면 빈 리스트로 방어적 복사된다")
        void nullTagListBecomesEmptyList() {
            SearchItem item =
                    new SearchItem(
                            1L,
                            List.of("https://img.com/1.jpg"),
                            "브랜드",
                            "상품명",
                            "10000",
                            "20000",
                            "50",
                            null,
                            "PAID");

            assertThat(item.tagList()).isEmpty();
        }

        @Test
        @DisplayName("imageUrlList는 방어적 복사된다")
        void imageUrlListIsDefensivelyCopied() {
            List<String> mutableList = new ArrayList<>();
            mutableList.add("https://img.com/1.jpg");

            SearchItem item =
                    new SearchItem(
                            1L, mutableList, "브랜드", "상품명", "10000", "20000", "50", null, "PAID");

            mutableList.add("https://img.com/2.jpg");

            assertThat(item.imageUrlList()).hasSize(1);
        }

        @Test
        @DisplayName("tagList는 방어적 복사된다")
        void tagListIsDefensivelyCopied() {
            List<ItemTag> mutableTagList = new ArrayList<>();
            mutableTagList.add(ItemTag.ofTitle("태그1"));

            SearchItem item =
                    new SearchItem(
                            1L,
                            List.of("https://img.com/1.jpg"),
                            "브랜드",
                            "상품명",
                            "10000",
                            "20000",
                            "50",
                            mutableTagList,
                            "PAID");

            mutableTagList.add(ItemTag.ofTitle("태그2"));

            assertThat(item.tagList()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("모든 필드로 SearchItem을 생성한다")
        void createWithAllFields() {
            SearchItem item =
                    SearchItem.of(
                            1L,
                            List.of("https://img.com/1.jpg"),
                            "나이키",
                            "에어맥스 90",
                            "150000",
                            "200000",
                            "25",
                            List.of(ItemTag.ofTitle("할인")),
                            "PAID");

            assertThat(item.itemNo()).isEqualTo(1L);
            assertThat(item.brandName()).isEqualTo("나이키");
            assertThat(item.name()).isEqualTo("에어맥스 90");
            assertThat(item.price()).isEqualTo("150000");
            assertThat(item.originalPrice()).isEqualTo("200000");
            assertThat(item.discountRate()).isEqualTo("25");
            assertThat(item.shippingType()).isEqualTo("PAID");
            assertThat(item.imageUrlList()).containsExactly("https://img.com/1.jpg");
        }
    }

    @Nested
    @DisplayName("isFreeShipping() 테스트")
    class IsFreeShippingTest {

        @Test
        @DisplayName("shippingType이 DOMESTIC이면 무료배송이다")
        void domesticShippingTypeIsFreeShipping() {
            SearchItem item =
                    new SearchItem(
                            1L,
                            List.of(),
                            "브랜드",
                            "상품명",
                            "10000",
                            "20000",
                            "50",
                            List.of(),
                            "DOMESTIC");

            assertThat(item.isFreeShipping()).isTrue();
        }

        @Test
        @DisplayName("shippingType이 domestic(소문자)이면 무료배송이다")
        void lowercaseDomesticShippingTypeIsFreeShipping() {
            SearchItem item =
                    new SearchItem(
                            1L,
                            List.of(),
                            "브랜드",
                            "상품명",
                            "10000",
                            "20000",
                            "50",
                            List.of(),
                            "domestic");

            assertThat(item.isFreeShipping()).isTrue();
        }

        @Test
        @DisplayName("태그에 무료배송이 포함되면 무료배송이다")
        void freeShippingTagMeansFreeShipping() {
            List<ItemTag> tags = List.of(ItemTag.ofTitle("무료배송"));
            SearchItem item =
                    new SearchItem(
                            1L, List.of(), "브랜드", "상품명", "10000", "20000", "50", tags, "PAID");

            assertThat(item.isFreeShipping()).isTrue();
        }

        @Test
        @DisplayName("DOMESTIC도 아니고 무료배송 태그도 없으면 무료배송이 아니다")
        void paidShippingWithoutFreeShippingTag() {
            SearchItem item =
                    new SearchItem(
                            1L, List.of(), "브랜드", "상품명", "10000", "20000", "50", List.of(), "PAID");

            assertThat(item.isFreeShipping()).isFalse();
        }

        @Test
        @DisplayName("태그 목록이 비어있으면 무료배송이 아니다")
        void emptyTagListIsNotFreeShipping() {
            SearchItem item =
                    new SearchItem(
                            1L, List.of(), "브랜드", "상품명", "10000", "20000", "50", List.of(),
                            "EXPRESS");

            assertThat(item.isFreeShipping()).isFalse();
        }

        @Test
        @DisplayName("shippingType이 null이면 태그로만 판단한다")
        void nullShippingTypeChecksTags() {
            SearchItem item =
                    new SearchItem(
                            1L, List.of(), "브랜드", "상품명", "10000", "20000", "50", List.of(), null);

            assertThat(item.isFreeShipping()).isFalse();
        }
    }

    @Nested
    @DisplayName("mainImageUrl() 테스트")
    class MainImageUrlTest {

        @Test
        @DisplayName("이미지 목록의 첫 번째 URL을 반환한다")
        void returnsFirstImageUrl() {
            SearchItem item =
                    new SearchItem(
                            1L,
                            List.of("https://img.com/1.jpg", "https://img.com/2.jpg"),
                            "브랜드",
                            "상품명",
                            "10000",
                            "20000",
                            "50",
                            List.of(),
                            "PAID");

            assertThat(item.mainImageUrl()).isEqualTo("https://img.com/1.jpg");
        }

        @Test
        @DisplayName("이미지 목록이 비어있으면 null을 반환한다")
        void returnsNullWhenImageListIsEmpty() {
            SearchItem item =
                    new SearchItem(
                            1L, List.of(), "브랜드", "상품명", "10000", "20000", "50", List.of(), "PAID");

            assertThat(item.mainImageUrl()).isNull();
        }

        @Test
        @DisplayName("imageUrlList가 null이면 null을 반환한다")
        void returnsNullWhenImageListIsNull() {
            SearchItem item =
                    new SearchItem(1L, null, "브랜드", "상품명", "10000", "20000", "50", null, "PAID");

            assertThat(item.mainImageUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("toMiniShopItem() 테스트")
    class ToMiniShopItemTest {

        @Test
        @DisplayName("SearchItem을 MiniShopItem으로 변환한다")
        void convertsToMiniShopItem() {
            SearchItem item =
                    new SearchItem(
                            12345L,
                            List.of("https://img.com/1.jpg"),
                            "나이키",
                            "에어맥스 90",
                            "150,000",
                            "200,000",
                            "25",
                            List.of(),
                            "PAID");

            MiniShopItem miniShopItem = item.toMiniShopItem();

            assertThat(miniShopItem).isNotNull();
            assertThat(miniShopItem.itemNo()).isEqualTo(12345L);
            assertThat(miniShopItem.brandName()).isEqualTo("나이키");
            assertThat(miniShopItem.name()).isEqualTo("에어맥스 90");
        }

        @Test
        @DisplayName("DOMESTIC 배송 타입이면 무료배송 태그가 추가된다")
        void domesticShippingAddsFreeShippingTag() {
            SearchItem item =
                    new SearchItem(
                            1L,
                            List.of("https://img.com/1.jpg"),
                            "브랜드",
                            "상품명",
                            "10000",
                            "20000",
                            "50",
                            List.of(),
                            "DOMESTIC");

            MiniShopItem miniShopItem = item.toMiniShopItem();

            assertThat(miniShopItem.tagList()).anyMatch(tag -> tag.title().contains("무료배송"));
        }

        @Test
        @DisplayName("이미 무료배송 태그가 있으면 중복 추가하지 않는다")
        void existingFreeShippingTagIsNotDuplicated() {
            List<ItemTag> tags = new ArrayList<>();
            tags.add(ItemTag.ofTitle("무료배송"));

            SearchItem item =
                    new SearchItem(
                            1L,
                            List.of("https://img.com/1.jpg"),
                            "브랜드",
                            "상품명",
                            "10000",
                            "20000",
                            "50",
                            tags,
                            "DOMESTIC");

            MiniShopItem miniShopItem = item.toMiniShopItem();

            long freeShippingCount =
                    miniShopItem.tagList().stream()
                            .filter(tag -> "무료배송".equals(tag.title()))
                            .count();
            assertThat(freeShippingCount).isEqualTo(1);
        }
    }
}
