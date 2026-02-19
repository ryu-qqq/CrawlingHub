package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query.SearchProductImageOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query.SearchProductSyncOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductImageOutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductSyncOutboxApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductImageOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductSyncOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxWithImageResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductSyncOutboxResponse;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ProductOutboxQueryApiMapper 단위 테스트
 *
 * <p>ProductOutbox Query REST API ↔ Application Layer DTO 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>SyncOutbox API 요청 → Application Query 변환
 *   <li>ImageOutbox API 요청 → Application Query 변환
 *   <li>Application SyncOutbox 응답 → PageApiResponse 변환
 *   <li>Application ImageOutbox 응답 → PageApiResponse 변환
 *   <li>상태 문자열 파싱 (null, 빈 값, 유효하지 않은 값 처리)
 *   <li>null 시간 필드 처리 (Instant → "yyyy-MM-dd HH:mm:ss" Asia/Seoul 변환)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("ProductOutboxQueryApiMapper 단위 테스트")
class ProductOutboxQueryApiMapperTest {

    private ProductOutboxQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductOutboxQueryApiMapper();
    }

    @Nested
    @DisplayName("toSyncQuery()는")
    class ToSyncQuery {

        @Test
        @DisplayName("SearchProductSyncOutboxApiRequest를 SearchProductSyncOutboxQuery로 변환한다")
        void shouldConvertApiRequestToSyncQuery() {
            // Given
            Instant createdFrom = Instant.parse("2025-01-01T00:00:00Z");
            Instant createdTo = Instant.parse("2025-12-31T23:59:59Z");
            SearchProductSyncOutboxApiRequest request =
                    new SearchProductSyncOutboxApiRequest(
                            10L,
                            100L,
                            List.of(1001L, 1002L),
                            List.of("PENDING", "FAILED"),
                            createdFrom,
                            createdTo,
                            0,
                            20);

            // When
            SearchProductSyncOutboxQuery query = mapper.toSyncQuery(request);

            // Then
            assertThat(query.crawledProductId()).isEqualTo(10L);
            assertThat(query.sellerId()).isEqualTo(100L);
            assertThat(query.itemNos()).containsExactly(1001L, 1002L);
            assertThat(query.statuses())
                    .containsExactly(ProductOutboxStatus.PENDING, ProductOutboxStatus.FAILED);
            assertThat(query.createdFrom()).isEqualTo(createdFrom);
            assertThat(query.createdTo()).isEqualTo(createdTo);
            assertThat(query.page()).isZero();
            assertThat(query.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("null 필드가 포함된 요청도 정상 변환한다")
        void shouldHandleNullFields() {
            // Given
            SearchProductSyncOutboxApiRequest request =
                    new SearchProductSyncOutboxApiRequest(
                            null, null, null, null, null, null, null, null);

            // When
            SearchProductSyncOutboxQuery query = mapper.toSyncQuery(request);

            // Then
            assertThat(query.crawledProductId()).isNull();
            assertThat(query.sellerId()).isNull();
            assertThat(query.itemNos()).isNull();
            assertThat(query.statuses()).isNull();
            assertThat(query.createdFrom()).isNull();
            assertThat(query.createdTo()).isNull();
            assertThat(query.page()).isZero(); // 기본값 적용
            assertThat(query.size()).isEqualTo(20); // 기본값 적용
        }
    }

    @Nested
    @DisplayName("toImageQuery()는")
    class ToImageQuery {

        @Test
        @DisplayName("SearchProductImageOutboxApiRequest를 SearchProductImageOutboxQuery로 변환한다")
        void shouldConvertApiRequestToImageQuery() {
            // Given
            Instant createdFrom = Instant.parse("2025-06-01T00:00:00Z");
            Instant createdTo = Instant.parse("2025-06-30T23:59:59Z");
            SearchProductImageOutboxApiRequest request =
                    new SearchProductImageOutboxApiRequest(
                            50L,
                            20L,
                            List.of("COMPLETED", "PROCESSING"),
                            createdFrom,
                            createdTo,
                            1,
                            10);

            // When
            SearchProductImageOutboxQuery query = mapper.toImageQuery(request);

            // Then
            assertThat(query.crawledProductImageId()).isEqualTo(50L);
            assertThat(query.crawledProductId()).isEqualTo(20L);
            assertThat(query.statuses())
                    .containsExactly(ProductOutboxStatus.COMPLETED, ProductOutboxStatus.PROCESSING);
            assertThat(query.createdFrom()).isEqualTo(createdFrom);
            assertThat(query.createdTo()).isEqualTo(createdTo);
            assertThat(query.page()).isEqualTo(1);
            assertThat(query.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("null 필드가 포함된 요청도 정상 변환한다")
        void shouldHandleNullFields() {
            // Given
            SearchProductImageOutboxApiRequest request =
                    new SearchProductImageOutboxApiRequest(
                            null, null, null, null, null, null, null);

            // When
            SearchProductImageOutboxQuery query = mapper.toImageQuery(request);

            // Then
            assertThat(query.crawledProductImageId()).isNull();
            assertThat(query.crawledProductId()).isNull();
            assertThat(query.statuses()).isNull();
            assertThat(query.createdFrom()).isNull();
            assertThat(query.createdTo()).isNull();
            assertThat(query.page()).isZero(); // 기본값 적용
            assertThat(query.size()).isEqualTo(20); // 기본값 적용
        }
    }

    @Nested
    @DisplayName("toSyncPageApiResponse()는")
    class ToSyncPageApiResponse {

        @Test
        @DisplayName(
                "PageResponse<ProductSyncOutboxResponse>를"
                        + " PageApiResponse<ProductSyncOutboxApiResponse>로 변환한다")
        void shouldConvertSyncPageResponse() {
            // Given
            Instant createdAt = Instant.parse("2025-01-15T00:30:00Z"); // KST: 2025-01-15 09:30:00
            Instant updatedAt = Instant.parse("2025-01-15T01:00:00Z"); // KST: 2025-01-15 10:00:00
            Instant processedAt = Instant.parse("2025-01-15T01:30:00Z"); // KST: 2025-01-15 10:30:00
            List<ProductSyncOutboxResponse> items =
                    List.of(
                            new ProductSyncOutboxResponse(
                                    1L,
                                    10L,
                                    100L,
                                    12345L,
                                    "CREATE",
                                    "sync-100-12345-111",
                                    999L,
                                    ProductOutboxStatus.COMPLETED,
                                    0,
                                    null,
                                    true,
                                    createdAt,
                                    updatedAt,
                                    processedAt));
            PageResponse<ProductSyncOutboxResponse> pageResponse =
                    new PageResponse<>(items, 0, 20, 1L, 1, true, true);

            // When
            PageApiResponse<ProductSyncOutboxApiResponse> result =
                    mapper.toSyncPageApiResponse(pageResponse);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(1L);

            ProductSyncOutboxApiResponse item = result.content().get(0);
            assertThat(item.id()).isEqualTo(1L);
            assertThat(item.crawledProductId()).isEqualTo(10L);
            assertThat(item.sellerId()).isEqualTo(100L);
            assertThat(item.itemNo()).isEqualTo(12345L);
            assertThat(item.syncType()).isEqualTo("CREATE");
            assertThat(item.idempotencyKey()).isEqualTo("sync-100-12345-111");
            assertThat(item.externalProductId()).isEqualTo(999L);
            assertThat(item.status()).isEqualTo("COMPLETED");
            assertThat(item.retryCount()).isZero();
            assertThat(item.errorMessage()).isNull();
            assertThat(item.canRetry()).isTrue();
            assertThat(item.createdAt()).isEqualTo("2025-01-15 09:30:00");
            assertThat(item.updatedAt()).isEqualTo("2025-01-15 10:00:00");
            assertThat(item.processedAt()).isEqualTo("2025-01-15 10:30:00");
        }

        @Test
        @DisplayName("status가 null이면 null 문자열로 변환한다")
        void shouldConvertNullStatusToNull() {
            // Given
            List<ProductSyncOutboxResponse> items =
                    List.of(
                            new ProductSyncOutboxResponse(
                                    2L,
                                    10L,
                                    100L,
                                    12346L,
                                    "UPDATE",
                                    "sync-100-12346-222",
                                    null,
                                    null,
                                    1,
                                    "처리 오류",
                                    false,
                                    Instant.now(),
                                    null,
                                    null));
            PageResponse<ProductSyncOutboxResponse> pageResponse =
                    new PageResponse<>(items, 0, 20, 1L, 1, true, true);

            // When
            PageApiResponse<ProductSyncOutboxApiResponse> result =
                    mapper.toSyncPageApiResponse(pageResponse);

            // Then
            ProductSyncOutboxApiResponse item = result.content().get(0);
            assertThat(item.status()).isNull();
            assertThat(item.externalProductId()).isNull();
            assertThat(item.updatedAt()).isNull();
            assertThat(item.processedAt()).isNull();
        }

        @Test
        @DisplayName("빈 목록도 정상 변환한다")
        void shouldHandleEmptyList() {
            // Given
            PageResponse<ProductSyncOutboxResponse> pageResponse =
                    new PageResponse<>(List.of(), 0, 20, 0L, 0, true, true);

            // When
            PageApiResponse<ProductSyncOutboxApiResponse> result =
                    mapper.toSyncPageApiResponse(pageResponse);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.last()).isTrue();
        }
    }

    @Nested
    @DisplayName("toImagePageApiResponse()는")
    class ToImagePageApiResponse {

        @Test
        @DisplayName(
                "PageResponse<ProductImageOutboxWithImageResponse>를"
                        + " PageApiResponse<ProductImageOutboxApiResponse>로 변환한다")
        void shouldConvertImagePageResponse() {
            // Given
            Instant createdAt = Instant.parse("2025-03-10T00:00:00Z"); // KST: 2025-03-10 09:00:00
            Instant updatedAt = Instant.parse("2025-03-10T01:00:00Z"); // KST: 2025-03-10 10:00:00
            List<ProductImageOutboxWithImageResponse> items =
                    List.of(
                            new ProductImageOutboxWithImageResponse(
                                    10L,
                                    200L,
                                    "image-200-1234567890",
                                    ProductOutboxStatus.PENDING,
                                    0,
                                    null,
                                    true,
                                    createdAt,
                                    updatedAt,
                                    null,
                                    50L,
                                    "https://example.com/image.jpg",
                                    "https://s3.example.com/image.jpg",
                                    ImageType.THUMBNAIL));
            PageResponse<ProductImageOutboxWithImageResponse> pageResponse =
                    new PageResponse<>(items, 0, 20, 1L, 1, true, true);

            // When
            PageApiResponse<ProductImageOutboxApiResponse> result =
                    mapper.toImagePageApiResponse(pageResponse);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(1L);

            ProductImageOutboxApiResponse item = result.content().get(0);
            assertThat(item.id()).isEqualTo(10L);
            assertThat(item.crawledProductImageId()).isEqualTo(200L);
            assertThat(item.idempotencyKey()).isEqualTo("image-200-1234567890");
            assertThat(item.status()).isEqualTo("PENDING");
            assertThat(item.retryCount()).isZero();
            assertThat(item.errorMessage()).isNull();
            assertThat(item.canRetry()).isTrue();
            assertThat(item.createdAt()).isEqualTo("2025-03-10 09:00:00");
            assertThat(item.updatedAt()).isEqualTo("2025-03-10 10:00:00");
            assertThat(item.processedAt()).isNull();
            assertThat(item.crawledProductId()).isEqualTo(50L);
            assertThat(item.originalUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(item.s3Url()).isEqualTo("https://s3.example.com/image.jpg");
            assertThat(item.imageType()).isEqualTo("THUMBNAIL");
        }

        @Test
        @DisplayName("imageType과 status가 null이면 null 문자열로 변환한다")
        void shouldConvertNullImageTypeAndStatusToNull() {
            // Given
            List<ProductImageOutboxWithImageResponse> items =
                    List.of(
                            new ProductImageOutboxWithImageResponse(
                                    11L,
                                    201L,
                                    "image-201-0987654321",
                                    null,
                                    2,
                                    "업로드 오류",
                                    false,
                                    Instant.now(),
                                    null,
                                    null,
                                    51L,
                                    "https://example.com/fail.jpg",
                                    null,
                                    null));
            PageResponse<ProductImageOutboxWithImageResponse> pageResponse =
                    new PageResponse<>(items, 0, 20, 1L, 1, true, true);

            // When
            PageApiResponse<ProductImageOutboxApiResponse> result =
                    mapper.toImagePageApiResponse(pageResponse);

            // Then
            ProductImageOutboxApiResponse item = result.content().get(0);
            assertThat(item.status()).isNull();
            assertThat(item.imageType()).isNull();
            assertThat(item.s3Url()).isNull();
            assertThat(item.updatedAt()).isNull();
            assertThat(item.processedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("상태 파싱 (parseStatuses) 엣지 케이스")
    class ParseStatuses {

        @Test
        @DisplayName("statuses가 null이면 Query의 statuses는 null이다")
        void shouldReturnNullWhenStatusesIsNull() {
            // Given
            SearchProductSyncOutboxApiRequest request =
                    new SearchProductSyncOutboxApiRequest(
                            null, null, null, null, null, null, null, null);

            // When
            SearchProductSyncOutboxQuery query = mapper.toSyncQuery(request);

            // Then
            assertThat(query.statuses()).isNull();
        }

        @Test
        @DisplayName("statuses가 빈 리스트이면 Query의 statuses는 null이다")
        void shouldReturnNullWhenStatusesIsEmpty() {
            // Given
            SearchProductSyncOutboxApiRequest request =
                    new SearchProductSyncOutboxApiRequest(
                            null, null, null, List.of(), null, null, null, null);

            // When
            SearchProductSyncOutboxQuery query = mapper.toSyncQuery(request);

            // Then
            assertThat(query.statuses()).isNull();
        }

        @Test
        @DisplayName("유효하지 않은 상태 문자열은 필터링되어 제거된다")
        void shouldFilterOutInvalidStatusStrings() {
            // Given
            SearchProductSyncOutboxApiRequest request =
                    new SearchProductSyncOutboxApiRequest(
                            null,
                            null,
                            null,
                            List.of("PENDING", "INVALID_STATUS", "FAILED"),
                            null,
                            null,
                            null,
                            null);

            // When
            SearchProductSyncOutboxQuery query = mapper.toSyncQuery(request);

            // Then
            assertThat(query.statuses())
                    .containsExactly(ProductOutboxStatus.PENDING, ProductOutboxStatus.FAILED);
        }

        @Test
        @DisplayName("공백 문자열 상태는 필터링되어 제거된다")
        void shouldFilterOutBlankStatusStrings() {
            // Given
            SearchProductSyncOutboxApiRequest request =
                    new SearchProductSyncOutboxApiRequest(
                            null,
                            null,
                            null,
                            List.of("PENDING", "   ", "COMPLETED"),
                            null,
                            null,
                            null,
                            null);

            // When
            SearchProductSyncOutboxQuery query = mapper.toSyncQuery(request);

            // Then
            assertThat(query.statuses())
                    .containsExactly(ProductOutboxStatus.PENDING, ProductOutboxStatus.COMPLETED);
        }
    }
}
