package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("SearchParseResult Value Object 단위 테스트")
class SearchParseResultTest {

    private static final String NEXT_URL = "https://api.example.com/items?page=2";

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("상품 목록과 nextApiUrl로 생성한다")
        void createWithItemsAndNextUrl() {
            List<MiniShopItem> items = createMiniShopItems();
            SearchParseResult result = new SearchParseResult(items, NEXT_URL);

            assertThat(result.items()).hasSize(1);
            assertThat(result.nextApiUrl()).isEqualTo(NEXT_URL);
        }

        @Test
        @DisplayName("items가 null이면 빈 목록으로 처리한다")
        void nullItemsBecomesEmptyList() {
            SearchParseResult result = new SearchParseResult(null, null);
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("nextApiUrl이 null이어도 생성된다")
        void createWithNullNextUrl() {
            List<MiniShopItem> items = createMiniShopItems();
            SearchParseResult result = new SearchParseResult(items, null);
            assertThat(result.nextApiUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("empty() 팩토리 메서드 테스트")
    class EmptyFactoryTest {

        @Test
        @DisplayName("빈 결과를 생성한다")
        void createEmptyResult() {
            SearchParseResult result = SearchParseResult.empty();
            assertThat(result.items()).isEmpty();
            assertThat(result.nextApiUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("isEmpty() 테스트")
    class IsEmptyTest {

        @Test
        @DisplayName("상품이 없으면 true를 반환한다")
        void returnsTrueWhenNoItems() {
            SearchParseResult result = SearchParseResult.empty();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("상품이 있으면 false를 반환한다")
        void returnsFalseWhenHasItems() {
            SearchParseResult result = new SearchParseResult(createMiniShopItems(), null);
            assertThat(result.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasNextPage() 테스트")
    class HasNextPageTest {

        @Test
        @DisplayName("nextApiUrl이 있으면 true를 반환한다")
        void returnsTrueWhenHasNextUrl() {
            SearchParseResult result = new SearchParseResult(List.of(), NEXT_URL);
            assertThat(result.hasNextPage()).isTrue();
        }

        @Test
        @DisplayName("nextApiUrl이 null이면 false를 반환한다")
        void returnsFalseWhenNullNextUrl() {
            SearchParseResult result = new SearchParseResult(List.of(), null);
            assertThat(result.hasNextPage()).isFalse();
        }

        @Test
        @DisplayName("nextApiUrl이 빈 문자열이면 false를 반환한다")
        void returnsFalseWhenBlankNextUrl() {
            SearchParseResult result = new SearchParseResult(List.of(), "  ");
            assertThat(result.hasNextPage()).isFalse();
        }
    }

    @Nested
    @DisplayName("shouldStopPagination() 테스트")
    class ShouldStopPaginationTest {

        @Test
        @DisplayName("상품이 없고 nextUrl도 없으면 true를 반환한다")
        void returnsTrueWhenBothEmpty() {
            SearchParseResult result = SearchParseResult.empty();
            assertThat(result.shouldStopPagination()).isTrue();
        }

        @Test
        @DisplayName("상품이 없어도 nextUrl이 있으면 false를 반환한다")
        void returnsFalseWhenHasNextUrl() {
            SearchParseResult result = new SearchParseResult(List.of(), NEXT_URL);
            assertThat(result.shouldStopPagination()).isFalse();
        }

        @Test
        @DisplayName("상품이 있으면 false를 반환한다")
        void returnsFalseWhenHasItems() {
            SearchParseResult result = new SearchParseResult(createMiniShopItems(), null);
            assertThat(result.shouldStopPagination()).isFalse();
        }
    }

    @Nested
    @DisplayName("size() 테스트")
    class SizeTest {

        @Test
        @DisplayName("상품 개수를 반환한다")
        void returnsItemCount() {
            SearchParseResult result = new SearchParseResult(createMiniShopItems(), null);
            assertThat(result.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 목록이면 0을 반환한다")
        void returnsZeroWhenEmpty() {
            SearchParseResult result = SearchParseResult.empty();
            assertThat(result.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            List<MiniShopItem> items = createMiniShopItems();
            SearchParseResult result1 = new SearchParseResult(items, NEXT_URL);
            SearchParseResult result2 = new SearchParseResult(items, NEXT_URL);
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }
    }

    private List<MiniShopItem> createMiniShopItems() {
        MiniShopItem item =
                new MiniShopItem(
                        1L,
                        List.of("https://img.com/thumb.jpg"),
                        "브랜드",
                        "테스트 상품",
                        10000,
                        12000,
                        12000,
                        10,
                        10,
                        9000,
                        List.of());
        return List.of(item);
    }
}
