package com.ryuqq.crawlinghub.application.crawl.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * MiniShopResponseParser 단위 테스트
 *
 * <p>MINI_SHOP 크롤링 응답 파서 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("MiniShopResponseParser 테스트")
class MiniShopResponseParserTest {

    private MiniShopResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new MiniShopResponseParser(new ObjectMapper());
    }

    @Nested
    @DisplayName("parse() 테스트")
    class Parse {

        @Test
        @DisplayName("[성공] 정상 응답 파싱")
        void shouldParseValidResponse() {
            // Given
            String responseBody =
                    """
{
    "items": [
        {
            "itemNo": 12345,
            "name": "Test Product",
            "brandName": "TestBrand",
            "price": "100,000",
            "originalPrice": "120,000",
            "normalPrice": "120,000",
            "discountRate": "17%",
            "appDiscountRate": "20%",
            "appPrice": "96,000",
            "imageUrlList": ["https://example.com/image1.jpg"],
            "tagList": [{"title": "NEW", "textColor": "#fff", "bgColor": "#000", "borderColor": "#000"}]
        }
    ]
}
""";

            // When
            List<MiniShopItem> result = parser.parse(responseBody);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).itemNo()).isEqualTo(12345L);
            assertThat(result.get(0).name()).isEqualTo("Test Product");
            assertThat(result.get(0).brandName()).isEqualTo("TestBrand");
        }

        @Test
        @DisplayName("[성공] 여러 상품 파싱")
        void shouldParseMultipleItems() {
            // Given
            String responseBody =
                    """
                    {
                        "items": [
                            {"itemNo": 1, "name": "Product 1", "brandName": "Brand1"},
                            {"itemNo": 2, "name": "Product 2", "brandName": "Brand2"},
                            {"itemNo": 3, "name": "Product 3", "brandName": "Brand3"}
                        ]
                    }
                    """;

            // When
            List<MiniShopItem> result = parser.parse(responseBody);

            // Then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("[실패] null 응답")
        void shouldReturnEmptyForNullResponse() {
            // When
            List<MiniShopItem> result = parser.parse(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 빈 문자열 응답")
        void shouldReturnEmptyForBlankResponse() {
            // When
            List<MiniShopItem> result = parser.parse("  ");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] items 필드 없음")
        void shouldReturnEmptyWhenNoItemsField() {
            // Given
            String responseBody =
                    """
                    {"products": []}
                    """;

            // When
            List<MiniShopItem> result = parser.parse(responseBody);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] 필수 필드 없는 항목 스킵")
        void shouldSkipItemsWithoutRequiredFields() {
            // Given
            String responseBody =
                    """
                    {
                        "items": [
                            {"name": "No ItemNo"},
                            {"itemNo": 1, "name": "Valid Item"}
                        ]
                    }
                    """;

            // When
            List<MiniShopItem> result = parser.parse(responseBody);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).itemNo()).isEqualTo(1L);
        }

        @Test
        @DisplayName("[성공] 빈 items 배열")
        void shouldReturnEmptyForEmptyItemsArray() {
            // Given
            String responseBody =
                    """
                    {"items": []}
                    """;

            // When
            List<MiniShopItem> result = parser.parse(responseBody);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] 이미지 URL 및 태그 파싱")
        void shouldParseImageUrlsAndTags() {
            // Given
            String responseBody =
                    """
{
    "items": [
        {
            "itemNo": 1,
            "name": "Test",
            "imageUrlList": ["url1", "url2", "url3"],
            "tagList": [
                {"title": "SALE", "textColor": "#red", "bgColor": "#white", "borderColor": "#gray"}
            ]
        }
    ]
}
""";

            // When
            List<MiniShopItem> result = parser.parse(responseBody);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).imageUrls()).hasSize(3);
            assertThat(result.get(0).tagList()).hasSize(1);
            assertThat(result.get(0).tagList().get(0).title()).isEqualTo("SALE");
        }

        @Test
        @DisplayName("[실패] 잘못된 JSON 형식")
        void shouldReturnEmptyForInvalidJson() {
            // Given
            String responseBody = "invalid json";

            // When
            List<MiniShopItem> result = parser.parse(responseBody);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
