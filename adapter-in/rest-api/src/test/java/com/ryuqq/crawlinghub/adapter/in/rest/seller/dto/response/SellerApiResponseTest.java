package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SellerApiResponse 단위 테스트
 *
 * <p>셀러 API 응답 DTO의 생성 및 필드 접근을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("SellerApiResponse 단위 테스트")
class SellerApiResponseTest {

    @Nested
    @DisplayName("record 생성 검증")
    class RecordCreationTest {

        @Test
        @DisplayName("모든 필드를 포함하여 생성할 수 있다")
        void shouldCreateWithAllFields() {
            // Given
            Long sellerId = 1L;
            String mustItSellerName = "머스트잇 테스트 셀러";
            String sellerName = "테스트 셀러";
            String status = "ACTIVE";
            String createdAt = "2024-01-15T10:30:00+09:00";
            String updatedAt = "2024-01-20T15:00:00+09:00";

            // When
            SellerApiResponse response =
                    new SellerApiResponse(
                            sellerId, mustItSellerName, sellerName, status, createdAt, updatedAt);

            // Then
            assertThat(response.sellerId()).isEqualTo(sellerId);
            assertThat(response.mustItSellerName()).isEqualTo(mustItSellerName);
            assertThat(response.sellerName()).isEqualTo(sellerName);
            assertThat(response.status()).isEqualTo(status);
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("updatedAt이 null이어도 생성할 수 있다")
        void shouldCreateWithNullUpdatedAt() {
            // When
            SellerApiResponse response =
                    new SellerApiResponse(
                            1L, "머스트잇 셀러", "셀러", "ACTIVE", "2024-01-15T10:30:00+09:00", null);

            // Then
            assertThat(response.sellerId()).isEqualTo(1L);
            assertThat(response.updatedAt()).isNull();
        }

        @Test
        @DisplayName("INACTIVE 상태로 생성할 수 있다")
        void shouldCreateWithInactiveStatus() {
            // When
            SellerApiResponse response =
                    new SellerApiResponse(
                            2L, "머스트잇 셀러2", "셀러2", "INACTIVE", "2024-01-15T10:30:00+09:00", null);

            // Then
            assertThat(response.status()).isEqualTo("INACTIVE");
        }
    }

    @Nested
    @DisplayName("record 동등성 검증")
    class RecordEqualityTest {

        @Test
        @DisplayName("동일한 필드값을 가진 두 인스턴스는 동등하다")
        void shouldBeEqualWhenSameFields() {
            // Given
            SellerApiResponse response1 =
                    new SellerApiResponse(
                            1L, "머스트잇 셀러", "셀러", "ACTIVE", "2024-01-15T10:30:00+09:00", null);
            SellerApiResponse response2 =
                    new SellerApiResponse(
                            1L, "머스트잇 셀러", "셀러", "ACTIVE", "2024-01-15T10:30:00+09:00", null);

            // Then
            assertThat(response1).isEqualTo(response2);
        }

        @Test
        @DisplayName("sellerId가 다른 두 인스턴스는 동등하지 않다")
        void shouldNotBeEqualWhenDifferentSellerId() {
            // Given
            SellerApiResponse response1 =
                    new SellerApiResponse(
                            1L, "머스트잇 셀러", "셀러", "ACTIVE", "2024-01-15T10:30:00+09:00", null);
            SellerApiResponse response2 =
                    new SellerApiResponse(
                            2L, "머스트잇 셀러", "셀러", "ACTIVE", "2024-01-15T10:30:00+09:00", null);

            // Then
            assertThat(response1).isNotEqualTo(response2);
        }
    }

    @Nested
    @DisplayName("record toString 검증")
    class RecordToStringTest {

        @Test
        @DisplayName("toString()이 정상적으로 동작한다")
        void shouldHaveToString() {
            // Given
            SellerApiResponse response =
                    new SellerApiResponse(
                            1L, "머스트잇 셀러", "셀러", "ACTIVE", "2024-01-15T10:30:00+09:00", null);

            // When
            String result = response.toString();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("SellerApiResponse");
            assertThat(result).contains("1");
        }
    }
}
