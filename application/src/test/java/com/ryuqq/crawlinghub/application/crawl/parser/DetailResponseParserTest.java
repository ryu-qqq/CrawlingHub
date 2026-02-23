package com.ryuqq.crawlinghub.application.crawl.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.parser.DetailResponseParser;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * DetailResponseParser 단위 테스트
 *
 * <p>DETAIL 크롤링 응답 파서 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("DetailResponseParser 테스트")
class DetailResponseParserTest {

    private DetailResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new DetailResponseParser(new ObjectMapper());
    }

    @Nested
    @DisplayName("parse() 테스트")
    class Parse {

        @Test
        @DisplayName("[성공] 정상 응답 파싱 - ProductInfoModule")
        void shouldParseValidResponse() {
            // Given
            String responseBody =
                    """
                    {
                        "moduleList": [
                            {
                                "type": "ProductInfoModule",
                                "data": {
                                    "sellerNo": 100,
                                    "sellerId": "seller123",
                                    "itemNo": 12345,
                                    "itemName": "Test Product",
                                    "brandName": "TestBrand",
                                    "brandNameKr": "테스트브랜드",
                                    "brandCode": 999,
                                    "normalPrice": 120000,
                                    "sellingPrice": 100000,
                                    "discountPrice": 20000,
                                    "discountRate": 17,
                                    "stock": 50,
                                    "isSoldOut": false,
                                    "headerCategoryCode": "HC01",
                                    "headerCategory": "의류",
                                    "largeCategoryCode": "LC01",
                                    "largeCategory": "상의",
                                    "mediumCategoryCode": "MC01",
                                    "mediumCategory": "티셔츠"
                                }
                            }
                        ]
                    }
                    """;

            // When
            Optional<ProductDetailInfo> result = parser.parse(responseBody, 12345L);

            // Then
            assertThat(result).isPresent();
            ProductDetailInfo info = result.get();
            assertThat(info.itemNo()).isEqualTo(12345L);
            assertThat(info.itemName()).isEqualTo("Test Product");
            assertThat(info.brandName()).isEqualTo("TestBrand");
            assertThat(info.sellingPrice()).isEqualTo(100000);
        }

        @Test
        @DisplayName("[성공] 배너 이미지 파싱")
        void shouldParseBannerImages() {
            // Given
            String responseBody =
                    """
                    {
                        "moduleList": [
                            {
                                "type": "ProductBannersModule",
                                "data": {
                                    "images": ["https://img1.jpg", "https://img2.jpg"]
                                }
                            },
                            {
                                "type": "ProductInfoModule",
                                "data": {
                                    "sellerNo": 100,
                                    "sellerId": "seller123",
                                    "itemNo": 1,
                                    "itemName": "Test",
                                    "normalPrice": 10000,
                                    "sellingPrice": 10000
                                }
                            }
                        ]
                    }
                    """;

            // When
            Optional<ProductDetailInfo> result = parser.parse(responseBody, 1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().bannerImages()).hasSize(2);
            assertThat(result.get().bannerImages())
                    .contains("https://img1.jpg", "https://img2.jpg");
        }

        @Test
        @DisplayName("[성공] 배송 정보 파싱")
        void shouldParseShippingInfo() {
            // Given
            String responseBody =
                    """
                    {
                        "moduleList": [
                            {
                                "type": "ProductInfoModule",
                                "data": {
                                    "sellerNo": 100,
                                    "sellerId": "seller123",
                                    "itemNo": 1,
                                    "itemName": "Test",
                                    "normalPrice": 10000,
                                    "sellingPrice": 10000
                                }
                            },
                            {
                                "type": "ShippingModule",
                                "data": {
                                    "items": [
                                        {
                                            "data": {
                                                "shippingType": "DOMESTIC",
                                                "shippingFee": 3000,
                                                "shippingFeeType": "PAID",
                                                "averageDeliveryDay": {"text": "2-3일 내 배송"}
                                            }
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                    """;

            // When
            Optional<ProductDetailInfo> result = parser.parse(responseBody, 1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().shipping()).isNotNull();
        }

        @Test
        @DisplayName("[성공] 상세 정보 모듈 파싱 - HTML 이미지 추출")
        void shouldParseDetailInfoModule() {
            // Given
            String responseBody =
                    """
{
    "moduleList": [
        {
            "type": "ProductInfoModule",
            "data": {
                "sellerNo": 100,
                "sellerId": "seller123",
                "itemNo": 1,
                "itemName": "Test",
                "normalPrice": 10000,
                "sellingPrice": 10000
            }
        },
        {
            "type": "ProductDetailInfoModule",
            "data": {
                "originCountry": "한국",
                "itemStatus": "NEW",
                "descriptionMarkUp": "<p>설명</p><img src='https://detail1.jpg'/><img src=\\"https://detail2.jpg\\"/>"
            }
        }
    ]
}
""";

            // When
            Optional<ProductDetailInfo> result = parser.parse(responseBody, 1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().originCountry()).isEqualTo("한국");
            assertThat(result.get().itemStatus()).isEqualTo("NEW");
            assertThat(result.get().detailImages()).hasSize(2);
        }

        @Test
        @DisplayName("[실패] null 응답")
        void shouldReturnEmptyForNullResponse() {
            // When
            Optional<ProductDetailInfo> result = parser.parse(null, 1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 빈 문자열 응답")
        void shouldReturnEmptyForBlankResponse() {
            // When
            Optional<ProductDetailInfo> result = parser.parse("  ", 1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] moduleList 없음")
        void shouldReturnEmptyWhenNoModuleList() {
            // Given
            String responseBody =
                    """
                    {"data": {}}
                    """;

            // When
            Optional<ProductDetailInfo> result = parser.parse(responseBody, 1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] ProductInfoModule 없음")
        void shouldReturnEmptyWhenNoProductInfoModule() {
            // Given
            String responseBody =
                    """
                    {
                        "moduleList": [
                            {"type": "ProductBannersModule", "data": {"images": []}}
                        ]
                    }
                    """;

            // When
            Optional<ProductDetailInfo> result = parser.parse(responseBody, 1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 잘못된 JSON 형식")
        void shouldReturnEmptyForInvalidJson() {
            // Given
            String responseBody = "invalid json";

            // When
            Optional<ProductDetailInfo> result = parser.parse(responseBody, 1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] 숫자가 문자열로 온 경우 파싱")
        void shouldParseStringNumbers() {
            // Given
            String responseBody =
                    """
                    {
                        "moduleList": [
                            {
                                "type": "ProductInfoModule",
                                "data": {
                                    "sellerNo": "100",
                                    "sellerId": "seller123",
                                    "itemNo": "12345",
                                    "itemName": "Test",
                                    "normalPrice": "120000",
                                    "sellingPrice": "100000"
                                }
                            }
                        ]
                    }
                    """;

            // When
            Optional<ProductDetailInfo> result = parser.parse(responseBody, 12345L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().itemNo()).isEqualTo(12345L);
            assertThat(result.get().sellingPrice()).isEqualTo(100000);
        }
    }
}
