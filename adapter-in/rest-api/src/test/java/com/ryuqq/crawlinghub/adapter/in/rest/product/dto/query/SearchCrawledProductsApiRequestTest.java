package com.ryuqq.crawlinghub.adapter.in.rest.product.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SearchCrawledProductsApiRequest 단위 테스트
 *
 * <p>크롤링 상품 검색 요청 DTO의 기본값 적용 및 검증 로직을 테스트합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>기본값 적용 검증 (page: 0, size: 20)
 *   <li>필드 값 보존 검증
 *   <li>null 필드 처리 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("dto")
@DisplayName("SearchCrawledProductsApiRequest 단위 테스트")
class SearchCrawledProductsApiRequestTest {

    @Nested
    @DisplayName("기본값 적용 테스트")
    class DefaultValueTests {

        @Test
        @DisplayName("page가 null이면 기본값 0이 적용된다")
        void shouldApplyDefaultPageWhenNull() {
            // When
            SearchCrawledProductsApiRequest request =
                    new SearchCrawledProductsApiRequest(
                            1L, 100L, "상품명", "브랜드", true, false, true, null, 30);

            // Then
            assertThat(request.page()).isZero();
        }

        @Test
        @DisplayName("size가 null이면 기본값 20이 적용된다")
        void shouldApplyDefaultSizeWhenNull() {
            // When
            SearchCrawledProductsApiRequest request =
                    new SearchCrawledProductsApiRequest(
                            1L, 100L, "상품명", "브랜드", true, false, true, 0, null);

            // Then
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("page와 size가 모두 null이면 기본값이 적용된다")
        void shouldApplyDefaultValuesWhenBothNull() {
            // When
            SearchCrawledProductsApiRequest request =
                    new SearchCrawledProductsApiRequest(
                            null, null, null, null, null, null, null, null, null);

            // Then
            assertThat(request.page()).isZero();
            assertThat(request.size()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("값 보존 테스트")
    class ValuePreservationTests {

        @Test
        @DisplayName("명시적으로 지정된 page 값은 유지된다")
        void shouldPreserveExplicitPageValue() {
            // When
            SearchCrawledProductsApiRequest request =
                    new SearchCrawledProductsApiRequest(
                            null, null, null, null, null, null, null, 5, null);

            // Then
            assertThat(request.page()).isEqualTo(5);
        }

        @Test
        @DisplayName("명시적으로 지정된 size 값은 유지된다")
        void shouldPreserveExplicitSizeValue() {
            // When
            SearchCrawledProductsApiRequest request =
                    new SearchCrawledProductsApiRequest(
                            null, null, null, null, null, null, null, null, 50);

            // Then
            assertThat(request.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void shouldSetAllFieldsCorrectly() {
            // When
            SearchCrawledProductsApiRequest request =
                    new SearchCrawledProductsApiRequest(
                            1L, 100L, "테스트 상품", "테스트 브랜드", true, false, true, 2, 30);

            // Then
            assertThat(request.sellerId()).isEqualTo(1L);
            assertThat(request.itemNo()).isEqualTo(100L);
            assertThat(request.itemName()).isEqualTo("테스트 상품");
            assertThat(request.brandName()).isEqualTo("테스트 브랜드");
            assertThat(request.needsSync()).isTrue();
            assertThat(request.allCrawled()).isFalse();
            assertThat(request.hasExternalId()).isTrue();
            assertThat(request.page()).isEqualTo(2);
            assertThat(request.size()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("null 필드 테스트")
    class NullFieldTests {

        @Test
        @DisplayName("선택적 필드는 null을 허용한다")
        void shouldAllowNullForOptionalFields() {
            // When
            SearchCrawledProductsApiRequest request =
                    new SearchCrawledProductsApiRequest(
                            null, null, null, null, null, null, null, 0, 20);

            // Then
            assertThat(request.sellerId()).isNull();
            assertThat(request.itemNo()).isNull();
            assertThat(request.itemName()).isNull();
            assertThat(request.brandName()).isNull();
            assertThat(request.needsSync()).isNull();
            assertThat(request.allCrawled()).isNull();
            assertThat(request.hasExternalId()).isNull();
        }
    }
}
