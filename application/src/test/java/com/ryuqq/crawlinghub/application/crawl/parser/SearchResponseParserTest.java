package com.ryuqq.crawlinghub.application.crawl.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.parser.SearchResponseParser;
import com.ryuqq.crawlinghub.domain.product.vo.SearchParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SearchResponseParser 단위 테스트
 *
 * <p>Search API 응답 파서 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SearchResponseParser 테스트")
class SearchResponseParserTest {

    private SearchResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new SearchResponseParser(new ObjectMapper());
    }

    @Nested
    @DisplayName("parse() 테스트")
    class Parse {

        @Test
        @DisplayName("[성공] 정상 응답 파싱 - 상품 목록과 nextApiUrl")
        void shouldParseValidResponse() {
            // Given
            String responseBody =
                    """
                    {
                        "moduleList": [
                            {
                                "type": "SearchItemV2",
                                "data": {
                                    "itemNo": 12345,
                                    "name": "Test Product",
                                    "brandName": "TestBrand",
                                    "price": "100,000",
                                    "originalPrice": "120,000",
                                    "discountRate": "17%",
                                    "imageUrlList": ["https://example.com/image.jpg"],
                                    "tagList": [],
                                    "shippingType": "DOMESTIC"
                                }
                            }
                        ],
                        "nextApiUrl": "/v1/search/items?page=2"
                    }
                    """;

            // When
            SearchParseResult result = parser.parse(responseBody);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).itemNo()).isEqualTo(12345L);
            assertThat(result.items().get(0).name()).isEqualTo("Test Product");
            assertThat(result.nextApiUrl()).isEqualTo("/v1/search/items?page=2");
            assertThat(result.hasNextPage()).isTrue();
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 - nextApiUrl 없음")
        void shouldHandleLastPage() {
            // Given
            String responseBody =
                    """
                    {
                        "moduleList": [
                            {
                                "type": "SearchItemV2",
                                "data": {"itemNo": 1, "name": "Last Item"}
                            }
                        ],
                        "nextApiUrl": null
                    }
                    """;

            // When
            SearchParseResult result = parser.parse(responseBody);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.nextApiUrl()).isNull();
            assertThat(result.hasNextPage()).isFalse();
        }

        @Test
        @DisplayName("[성공] 빈 moduleList")
        void shouldHandleEmptyModuleList() {
            // Given
            String responseBody =
                    """
                    {"moduleList": [], "nextApiUrl": null}
                    """;

            // When
            SearchParseResult result = parser.parse(responseBody);

            // Then
            assertThat(result.items()).isEmpty();
            assertThat(result.hasNextPage()).isFalse();
        }

        @Test
        @DisplayName("[성공] SearchItemV2가 아닌 모듈 스킵")
        void shouldSkipNonSearchItemModules() {
            // Given
            String responseBody =
                    """
                    {
                        "moduleList": [
                            {"type": "BannerModule", "data": {}},
                            {"type": "SearchItemV2", "data": {"itemNo": 1, "name": "Valid Item"}},
                            {"type": "FilterModule", "data": {}}
                        ]
                    }
                    """;

            // When
            SearchParseResult result = parser.parse(responseBody);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).itemNo()).isEqualTo(1L);
        }

        @Test
        @DisplayName("[실패] null 응답")
        void shouldReturnEmptyForNullResponse() {
            // When
            SearchParseResult result = parser.parse(null);

            // Then
            assertThat(result.items()).isEmpty();
            assertThat(result.hasNextPage()).isFalse();
        }

        @Test
        @DisplayName("[실패] 빈 문자열 응답")
        void shouldReturnEmptyForBlankResponse() {
            // When
            SearchParseResult result = parser.parse("  ");

            // Then
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("[성공] 이미지 URL 및 태그 파싱")
        void shouldParseImageUrlsAndTags() {
            // Given
            String responseBody =
                    """
{
    "moduleList": [
        {
            "type": "SearchItemV2",
            "data": {
                "itemNo": 1,
                "name": "Test",
                "imageUrlList": ["url1", "url2"],
                "tagList": [
                    {"title": "SALE", "textColor": "#fff", "bgColor": "#000", "borderColor": "#000"}
                ]
            }
        }
    ]
}
""";

            // When
            SearchParseResult result = parser.parse(responseBody);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).imageUrls()).hasSize(2);
            assertThat(result.items().get(0).tagList()).hasSize(1);
        }

        @Test
        @DisplayName("[성공] 필수 필드 없는 아이템 스킵")
        void shouldSkipItemsWithoutRequiredFields() {
            // Given
            String responseBody =
                    """
                    {
                        "moduleList": [
                            {"type": "SearchItemV2", "data": {"name": "No itemNo"}},
                            {"type": "SearchItemV2", "data": {"itemNo": 1, "name": "Valid"}}
                        ]
                    }
                    """;

            // When
            SearchParseResult result = parser.parse(responseBody);

            // Then
            assertThat(result.items()).hasSize(1);
        }

        @Test
        @DisplayName("[실패] 잘못된 JSON 형식")
        void shouldReturnEmptyForInvalidJson() {
            // Given
            String responseBody = "invalid json";

            // When
            SearchParseResult result = parser.parse(responseBody);

            // Then
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("[성공] 빈 nextApiUrl 문자열은 null로 처리")
        void shouldTreatBlankNextApiUrlAsNull() {
            // Given
            String responseBody =
                    """
{
    "moduleList": [{"type": "SearchItemV2", "data": {"itemNo": 1, "name": "Test"}}],
    "nextApiUrl": ""
}
""";

            // When
            SearchParseResult result = parser.parse(responseBody);

            // Then
            assertThat(result.nextApiUrl()).isNull();
            assertThat(result.hasNextPage()).isFalse();
        }
    }
}
