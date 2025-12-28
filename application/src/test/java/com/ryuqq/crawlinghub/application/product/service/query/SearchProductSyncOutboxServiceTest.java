package com.ryuqq.crawlinghub.application.product.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductSyncOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductSyncOutboxResponse;
import com.ryuqq.crawlinghub.application.product.port.out.query.SyncOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
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
 * SearchProductSyncOutboxService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProductSyncOutboxService 테스트")
class SearchProductSyncOutboxServiceTest {

    @Mock private SyncOutboxQueryPort queryPort;

    private SearchProductSyncOutboxService service;

    @BeforeEach
    void setUp() {
        service = new SearchProductSyncOutboxService(queryPort);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] Outbox 존재 시 → PageResponse 반환")
        void shouldReturnPageResponseWhenOutboxesExist() {
            // Given
            Long crawledProductId = 1L;
            Long sellerId = 100L;
            ProductOutboxStatus status = ProductOutboxStatus.PENDING;
            int page = 0;
            int size = 10;
            SearchProductSyncOutboxQuery query =
                    new SearchProductSyncOutboxQuery(
                            crawledProductId, sellerId, status, page, size);

            CrawledProductSyncOutbox outbox = createMockOutbox(ProductOutboxStatus.PENDING, 0);
            List<CrawledProductSyncOutbox> outboxes = List.of(outbox);
            long totalElements = 1L;

            given(queryPort.search(crawledProductId, sellerId, status, 0L, size))
                    .willReturn(outboxes);
            given(queryPort.count(crawledProductId, sellerId, status)).willReturn(totalElements);

            // When
            PageResponse<ProductSyncOutboxResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).crawledProductId()).isEqualTo(crawledProductId);
            assertThat(result.content().get(0).status()).isEqualTo(ProductOutboxStatus.PENDING);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            then(queryPort).should().search(crawledProductId, sellerId, status, 0L, size);
            then(queryPort).should().count(crawledProductId, sellerId, status);
        }

        @Test
        @DisplayName("[성공] Outbox 미존재 시 → 빈 PageResponse 반환")
        void shouldReturnEmptyPageResponseWhenNoOutboxesFound() {
            // Given
            Long crawledProductId = 999L;
            Long sellerId = 999L;
            ProductOutboxStatus status = ProductOutboxStatus.PENDING;
            int page = 0;
            int size = 10;
            SearchProductSyncOutboxQuery query =
                    new SearchProductSyncOutboxQuery(
                            crawledProductId, sellerId, status, page, size);

            given(queryPort.search(crawledProductId, sellerId, status, 0L, size))
                    .willReturn(Collections.emptyList());
            given(queryPort.count(crawledProductId, sellerId, status)).willReturn(0L);

            // When
            PageResponse<ProductSyncOutboxResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            then(queryPort).should().search(crawledProductId, sellerId, status, 0L, size);
            then(queryPort).should().count(crawledProductId, sellerId, status);
        }

        @Test
        @DisplayName("[성공] 페이징 파라미터가 올바르게 처리됨")
        void shouldHandlePagingCorrectly() {
            // Given
            int page = 2;
            int size = 20;
            long totalElements = 100L;
            long expectedOffset = 40L; // page * size = 2 * 20
            SearchProductSyncOutboxQuery query =
                    new SearchProductSyncOutboxQuery(null, null, null, page, size);

            CrawledProductSyncOutbox outbox = createMockOutbox(ProductOutboxStatus.PENDING, 0);
            List<CrawledProductSyncOutbox> outboxes = List.of(outbox);

            given(queryPort.search(null, null, null, expectedOffset, size)).willReturn(outboxes);
            given(queryPort.count(null, null, null)).willReturn(totalElements);

            // When
            PageResponse<ProductSyncOutboxResponse> result = service.execute(query);

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
            SearchProductSyncOutboxQuery query =
                    new SearchProductSyncOutboxQuery(null, null, null, page, size);

            CrawledProductSyncOutbox outbox = createMockOutbox(ProductOutboxStatus.PENDING, 0);
            List<CrawledProductSyncOutbox> outboxes = List.of(outbox);

            given(queryPort.search(null, null, null, expectedOffset, size)).willReturn(outboxes);
            given(queryPort.count(null, null, null)).willReturn(totalElements);

            // When
            PageResponse<ProductSyncOutboxResponse> result = service.execute(query);

            // Then
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isTrue();
        }

        @Test
        @DisplayName("[성공] FAILED 상태 Outbox 조회")
        void shouldReturnFailedOutboxes() {
            // Given
            SearchProductSyncOutboxQuery query =
                    new SearchProductSyncOutboxQuery(null, null, ProductOutboxStatus.FAILED, 0, 10);

            CrawledProductSyncOutbox failedOutbox =
                    createMockOutboxWithError(ProductOutboxStatus.FAILED, 3, "Sync failed");
            List<CrawledProductSyncOutbox> outboxes = List.of(failedOutbox);

            given(queryPort.search(null, null, ProductOutboxStatus.FAILED, 0L, 10))
                    .willReturn(outboxes);
            given(queryPort.count(null, null, ProductOutboxStatus.FAILED)).willReturn(1L);

            // When
            PageResponse<ProductSyncOutboxResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).hasSize(1);
            ProductSyncOutboxResponse response = result.content().get(0);
            assertThat(response.status()).isEqualTo(ProductOutboxStatus.FAILED);
            assertThat(response.retryCount()).isEqualTo(3);
            assertThat(response.errorMessage()).isEqualTo("Sync failed");
        }

        @Test
        @DisplayName("[성공] sellerId로만 필터링 조회")
        void shouldFilterBySellerIdOnly() {
            // Given
            Long sellerId = 100L;
            SearchProductSyncOutboxQuery query =
                    new SearchProductSyncOutboxQuery(null, sellerId, null, 0, 10);

            CrawledProductSyncOutbox outbox = createMockOutbox(ProductOutboxStatus.PENDING, 0);
            List<CrawledProductSyncOutbox> outboxes = List.of(outbox);

            given(queryPort.search(null, sellerId, null, 0L, 10)).willReturn(outboxes);
            given(queryPort.count(null, sellerId, null)).willReturn(1L);

            // When
            PageResponse<ProductSyncOutboxResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).hasSize(1);
            then(queryPort).should().search(null, sellerId, null, 0L, 10);
            then(queryPort).should().count(null, sellerId, null);
        }
    }

    // === Helper Methods ===

    private CrawledProductSyncOutbox createMockOutbox(ProductOutboxStatus status, int retryCount) {
        Instant now = Instant.now();
        return CrawledProductSyncOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                SyncType.CREATE,
                "sync-1-100-abc123",
                null,
                status,
                retryCount,
                null,
                now,
                null);
    }

    private CrawledProductSyncOutbox createMockOutboxWithError(
            ProductOutboxStatus status, int retryCount, String errorMessage) {
        Instant now = Instant.now();
        return CrawledProductSyncOutbox.reconstitute(
                1L,
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                SyncType.CREATE,
                "sync-1-100-abc123",
                null,
                status,
                retryCount,
                errorMessage,
                now,
                now);
    }
}
