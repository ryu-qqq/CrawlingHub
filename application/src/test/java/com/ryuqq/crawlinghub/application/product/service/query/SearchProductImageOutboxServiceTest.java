package com.ryuqq.crawlinghub.application.product.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductImageOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxWithImageResponse;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SearchProductImageOutboxService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProductImageOutboxService 테스트")
class SearchProductImageOutboxServiceTest {

    @Mock private ImageOutboxQueryPort queryPort;

    private SearchProductImageOutboxService service;

    @BeforeEach
    void setUp() {
        service = new SearchProductImageOutboxService(queryPort);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] Outbox 존재 시 → 이미지 정보 포함 PageResponse 반환")
        void shouldReturnPageResponseWithImageInfoWhenOutboxesExist() {
            // Given
            Long crawledProductImageId = 100L;
            ProductOutboxStatus status = ProductOutboxStatus.PENDING;
            int page = 0;
            int size = 10;
            SearchProductImageOutboxQuery query =
                    new SearchProductImageOutboxQuery(crawledProductImageId, status, page, size);

            ProductImageOutboxWithImageResponse response = createMockResponse();
            List<ProductImageOutboxWithImageResponse> responses = List.of(response);
            long totalElements = 1L;

            given(queryPort.searchWithImageInfo(crawledProductImageId, status, 0L, size))
                    .willReturn(responses);
            given(queryPort.count(crawledProductImageId, status)).willReturn(totalElements);

            // When
            PageResponse<ProductImageOutboxWithImageResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).crawledProductId()).isEqualTo(50L);
            assertThat(result.content().get(0).originalUrl())
                    .isEqualTo("https://example.com/image.jpg");
            assertThat(result.content().get(0).imageType()).isEqualTo(ImageType.THUMBNAIL);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            then(queryPort).should().searchWithImageInfo(crawledProductImageId, status, 0L, size);
            then(queryPort).should().count(crawledProductImageId, status);
        }

        @Test
        @DisplayName("[성공] Outbox 미존재 시 → 빈 PageResponse 반환")
        void shouldReturnEmptyPageResponseWhenNoOutboxesFound() {
            // Given
            Long crawledProductImageId = 999L;
            ProductOutboxStatus status = ProductOutboxStatus.PENDING;
            int page = 0;
            int size = 10;
            SearchProductImageOutboxQuery query =
                    new SearchProductImageOutboxQuery(crawledProductImageId, status, page, size);

            given(queryPort.searchWithImageInfo(crawledProductImageId, status, 0L, size))
                    .willReturn(Collections.emptyList());
            given(queryPort.count(crawledProductImageId, status)).willReturn(0L);

            // When
            PageResponse<ProductImageOutboxWithImageResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            then(queryPort).should().searchWithImageInfo(crawledProductImageId, status, 0L, size);
            then(queryPort).should().count(crawledProductImageId, status);
        }

        @Test
        @DisplayName("[성공] 페이징 파라미터가 올바르게 처리됨")
        void shouldHandlePagingCorrectly() {
            // Given
            int page = 2;
            int size = 20;
            long totalElements = 100L;
            SearchProductImageOutboxQuery query =
                    new SearchProductImageOutboxQuery(null, null, page, size);
            long expectedOffset = 40L; // page * size = 2 * 20

            ProductImageOutboxWithImageResponse response = createMockResponse();
            List<ProductImageOutboxWithImageResponse> responses = List.of(response);

            given(queryPort.searchWithImageInfo(null, null, expectedOffset, size))
                    .willReturn(responses);
            given(queryPort.count(null, null)).willReturn(totalElements);

            // When
            PageResponse<ProductImageOutboxWithImageResponse> result = service.execute(query);

            // Then
            assertThat(result.page()).isEqualTo(page);
            assertThat(result.size()).isEqualTo(size);
            assertThat(result.totalElements()).isEqualTo(totalElements);
            assertThat(result.totalPages()).isEqualTo(5); // 100 / 20 = 5
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isFalse();
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 판별 정확")
        void shouldCorrectlyDetermineLastPage() {
            // Given
            int page = 4; // last page (0-indexed, totalPages = 5)
            int size = 20;
            long totalElements = 100L;
            long expectedOffset = 80L;
            SearchProductImageOutboxQuery query =
                    new SearchProductImageOutboxQuery(null, null, page, size);

            ProductImageOutboxWithImageResponse response = createMockResponse();
            List<ProductImageOutboxWithImageResponse> responses = List.of(response);

            given(queryPort.searchWithImageInfo(null, null, expectedOffset, size))
                    .willReturn(responses);
            given(queryPort.count(null, null)).willReturn(totalElements);

            // When
            PageResponse<ProductImageOutboxWithImageResponse> result = service.execute(query);

            // Then
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isTrue();
        }

        @Test
        @DisplayName("[성공] nullable 필드 조회 시 정상 동작")
        void shouldHandleNullableFieldsInResponse() {
            // Given
            SearchProductImageOutboxQuery query =
                    new SearchProductImageOutboxQuery(null, ProductOutboxStatus.FAILED, 0, 10);

            ProductImageOutboxWithImageResponse responseWithNulls =
                    ProductImageOutboxWithImageResponse.of(
                            1L,
                            100L,
                            "image-100-1234567890",
                            ProductOutboxStatus.FAILED,
                            3,
                            "Upload failed",
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            null, // crawledProductId null
                            null, // originalUrl null
                            null, // s3Url null
                            null // imageType null
                            );

            given(queryPort.searchWithImageInfo(null, ProductOutboxStatus.FAILED, 0L, 10))
                    .willReturn(List.of(responseWithNulls));
            given(queryPort.count(null, ProductOutboxStatus.FAILED)).willReturn(1L);

            // When
            PageResponse<ProductImageOutboxWithImageResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).hasSize(1);
            ProductImageOutboxWithImageResponse response = result.content().get(0);
            assertThat(response.crawledProductId()).isNull();
            assertThat(response.originalUrl()).isNull();
            assertThat(response.s3Url()).isNull();
            assertThat(response.imageType()).isNull();
        }
    }

    // === Helper Methods ===

    private ProductImageOutboxWithImageResponse createMockResponse() {
        return ProductImageOutboxWithImageResponse.of(
                1L,
                100L,
                "image-100-1234567890",
                ProductOutboxStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                null,
                50L,
                "https://example.com/image.jpg",
                "https://s3.example.com/image.jpg",
                ImageType.THUMBNAIL);
    }
}
