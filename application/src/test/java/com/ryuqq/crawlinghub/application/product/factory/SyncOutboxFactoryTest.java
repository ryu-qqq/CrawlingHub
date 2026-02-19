package com.ryuqq.crawlinghub.application.product.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.product.dto.bundle.SyncOutboxBundle;
import com.ryuqq.crawlinghub.application.product.port.out.query.SyncOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SyncOutboxFactory 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SyncOutboxFactory 테스트")
class SyncOutboxFactoryTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");

    @Mock private SyncOutboxQueryPort syncOutboxQueryPort;
    @Mock private TimeProvider timeProvider;

    private SyncOutboxFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SyncOutboxFactory(syncOutboxQueryPort, timeProvider);
        lenient().when(timeProvider.now()).thenReturn(FIXED_INSTANT);
    }

    @Nested
    @DisplayName("createBundle() 테스트")
    class CreateBundle {

        @Test
        @DisplayName("[성공] 신규 상품 → CREATE 타입 Bundle 생성")
        void shouldCreateBundleWithCreateTypeForNewProduct() {
            // Given
            CrawledProduct product = createSyncReadyProduct(null);
            given(syncOutboxQueryPort.findByIdempotencyKey(anyString()))
                    .willReturn(Optional.empty());

            // When
            Optional<SyncOutboxBundle> result = factory.createBundle(product);

            // Then
            assertThat(result).isPresent();
            SyncOutboxBundle bundle = result.get();
            assertThat(bundle.isCreateRequest()).isTrue();
            assertThat(bundle.isUpdateRequest()).isFalse();

            CrawledProductSyncOutbox outbox = bundle.outbox();
            assertThat(outbox.getCrawledProductId()).isEqualTo(product.getId());
            assertThat(outbox.getSellerId()).isEqualTo(product.getSellerId());
            assertThat(outbox.getItemNo()).isEqualTo(product.getItemNo());
            assertThat(outbox.getSyncType()).isEqualTo(CrawledProductSyncOutbox.SyncType.CREATE);
            assertThat(outbox.getExternalProductId()).isNull();

            ExternalSyncRequestedEvent event = bundle.event();
            assertThat(event.syncType()).isEqualTo(ExternalSyncRequestedEvent.SyncType.CREATE);
            assertThat(event.crawledProductId()).isEqualTo(product.getId());
            assertThat(event.sellerId()).isEqualTo(product.getSellerId());
            assertThat(event.idempotencyKey()).isEqualTo(outbox.getIdempotencyKey());
        }

        @Test
        @DisplayName("[성공] 기존 상품 → UPDATE 타입 Bundle 생성")
        void shouldCreateBundleWithUpdateTypeForExistingProduct() {
            // Given
            Long externalProductId = 99999L;
            CrawledProduct product = createSyncReadyProduct(externalProductId);
            given(syncOutboxQueryPort.findByIdempotencyKey(anyString()))
                    .willReturn(Optional.empty());

            // When
            Optional<SyncOutboxBundle> result = factory.createBundle(product);

            // Then
            assertThat(result).isPresent();
            SyncOutboxBundle bundle = result.get();
            assertThat(bundle.isCreateRequest()).isFalse();
            assertThat(bundle.isUpdateRequest()).isTrue();

            CrawledProductSyncOutbox outbox = bundle.outbox();
            assertThat(outbox.getSyncType()).isEqualTo(CrawledProductSyncOutbox.SyncType.UPDATE);
            assertThat(outbox.getExternalProductId()).isEqualTo(externalProductId);

            ExternalSyncRequestedEvent event = bundle.event();
            assertThat(event.syncType()).isEqualTo(ExternalSyncRequestedEvent.SyncType.UPDATE);
            assertThat(event.idempotencyKey()).isEqualTo(outbox.getIdempotencyKey());
        }

        @Test
        @DisplayName("[스킵] 이미 존재하는 Outbox → Optional.empty 반환")
        void shouldReturnEmptyWhenDuplicateOutboxExists() {
            // Given
            CrawledProduct product = createSyncReadyProduct(null);
            CrawledProductSyncOutbox existingOutbox = mock(CrawledProductSyncOutbox.class);
            given(existingOutbox.isPending()).willReturn(true);
            given(syncOutboxQueryPort.findByIdempotencyKey(anyString()))
                    .willReturn(Optional.of(existingOutbox));

            // When
            Optional<SyncOutboxBundle> result = factory.createBundle(product);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // === Helper Methods ===

    private CrawledProduct createSyncReadyProduct(Long externalProductId) {
        Instant now = FIXED_INSTANT;
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "Test Product",
                "Test Brand",
                ProductPrice.of(10000, 10000, 10000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                "ACTIVE",
                "Korea",
                "Seoul",
                ProductOptions.empty(),
                createAllCrawledStatus(now),
                externalProductId,
                null,
                true,
                now,
                now);
    }

    private CrawlCompletionStatus createAllCrawledStatus(Instant now) {
        return CrawlCompletionStatus.initial()
                .withMiniShopCrawled(now)
                .withDetailCrawled(now)
                .withOptionCrawled(now);
    }
}
