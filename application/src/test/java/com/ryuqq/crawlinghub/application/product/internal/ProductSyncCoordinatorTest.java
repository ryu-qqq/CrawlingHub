package com.ryuqq.crawlinghub.application.product.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.dto.command.ProcessProductSyncCommand;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient;
import com.ryuqq.crawlinghub.application.product.validator.ProductSyncValidator;
import com.ryuqq.crawlinghub.application.product.validator.ProductSyncValidator.SyncTarget;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductSyncOutboxId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProductSyncCoordinator 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSyncCoordinator 테스트")
class ProductSyncCoordinatorTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;
    private static final Long EXTERNAL_PRODUCT_ID = 99999L;

    @Mock private ProductSyncValidator validator;
    @Mock private CrawledProductSyncOutboxCommandManager syncOutboxCommandManager;
    @Mock private CrawledProductCommandFacade commandFacade;
    @Mock private ExternalProductServerClient externalProductServerClient;
    @Mock private SellerReadManager sellerReadManager;

    private ProductSyncCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator =
                new ProductSyncCoordinator(
                        validator,
                        syncOutboxCommandManager,
                        commandFacade,
                        externalProductServerClient,
                        sellerReadManager);
    }

    @Nested
    @DisplayName("processSyncRequest() 테스트")
    class ProcessSyncRequest {

        @Test
        @DisplayName("[실패] 검증 실패 (Outbox/Product) → false 반환")
        void shouldReturnFalseWhenValidationFails() {
            // Given
            ProcessProductSyncCommand command = createCommand();
            given(validator.validateAndResolve(command.outboxId())).willReturn(Optional.empty());

            // When
            boolean result = coordinator.processSyncRequest(command);

            // Then
            assertThat(result).isFalse();
            verify(syncOutboxCommandManager, never()).markAsProcessing(any());
        }

        @Test
        @DisplayName("[성공] CREATE 동기화 성공 → COMPLETED + markAsSynced + persist")
        void shouldCompleteSyncForCreate() {
            // Given
            ProcessProductSyncCommand command = createCommand();
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.CREATE);
            CrawledProduct product = createMockProduct(null);
            Seller seller = createSeller();
            Long newExternalProductId = 55555L;

            given(validator.validateAndResolve(command.outboxId()))
                    .willReturn(Optional.of(new SyncTarget(outbox, product)));
            given(sellerReadManager.findById(SELLER_ID)).willReturn(Optional.of(seller));
            given(externalProductServerClient.sync(outbox, product, seller))
                    .willReturn(ProductSyncResult.success(newExternalProductId));

            // When
            boolean result = coordinator.processSyncRequest(command);

            // Then
            assertThat(result).isTrue();
            verify(syncOutboxCommandManager, times(1)).markAsProcessing(outbox);
            verify(commandFacade, times(1))
                    .completeSyncAndPersist(outbox, product, newExternalProductId);
        }

        @Test
        @DisplayName("[성공] UPDATE_PRICE 동기화 성공 → COMPLETED + markChangesSynced + persist")
        void shouldCompleteSyncForUpdatePrice() {
            // Given
            ProcessProductSyncCommand command = createCommand();
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_PRICE);
            CrawledProduct product = createMockProduct(EXTERNAL_PRODUCT_ID);
            Seller seller = createSeller();

            given(validator.validateAndResolve(command.outboxId()))
                    .willReturn(Optional.of(new SyncTarget(outbox, product)));
            given(sellerReadManager.findById(SELLER_ID)).willReturn(Optional.of(seller));
            given(externalProductServerClient.sync(outbox, product, seller))
                    .willReturn(ProductSyncResult.success(EXTERNAL_PRODUCT_ID));

            // When
            boolean result = coordinator.processSyncRequest(command);

            // Then
            assertThat(result).isTrue();
            verify(syncOutboxCommandManager, times(1)).markAsProcessing(outbox);
            verify(commandFacade, times(1))
                    .completeSyncAndPersist(outbox, product, EXTERNAL_PRODUCT_ID);
        }

        @Test
        @DisplayName("[실패] API 호출 실패 (result.success=false) → FAILED + false 반환")
        void shouldMarkAsFailedWhenApiCallFails() {
            // Given
            ProcessProductSyncCommand command = createCommand();
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.CREATE);
            CrawledProduct product = createMockProduct(null);
            Seller seller = createSeller();

            given(validator.validateAndResolve(command.outboxId()))
                    .willReturn(Optional.of(new SyncTarget(outbox, product)));
            given(sellerReadManager.findById(SELLER_ID)).willReturn(Optional.of(seller));
            given(externalProductServerClient.sync(outbox, product, seller))
                    .willReturn(ProductSyncResult.failure("ERR_001", "Server error"));

            // When
            boolean result = coordinator.processSyncRequest(command);

            // Then
            assertThat(result).isFalse();
            verify(syncOutboxCommandManager, times(1)).markAsProcessing(outbox);
            verify(syncOutboxCommandManager, times(1)).markAsFailed(eq(outbox), anyString());
            verify(commandFacade, never()).completeSyncAndPersist(any(), any(), any());
        }

        @Test
        @DisplayName("[실패] API 호출 예외 발생 → FAILED + false 반환")
        void shouldMarkAsFailedWhenApiThrowsException() {
            // Given
            ProcessProductSyncCommand command = createCommand();
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.CREATE);
            CrawledProduct product = createMockProduct(null);
            Seller seller = createSeller();

            given(validator.validateAndResolve(command.outboxId()))
                    .willReturn(Optional.of(new SyncTarget(outbox, product)));
            given(sellerReadManager.findById(SELLER_ID)).willReturn(Optional.of(seller));
            given(externalProductServerClient.sync(outbox, product, seller))
                    .willThrow(new RuntimeException("Connection refused"));

            // When
            boolean result = coordinator.processSyncRequest(command);

            // Then
            assertThat(result).isFalse();
            verify(syncOutboxCommandManager, times(1)).markAsProcessing(outbox);
            verify(syncOutboxCommandManager, times(1)).markAsFailed(eq(outbox), anyString());
            verify(commandFacade, never()).completeSyncAndPersist(any(), any(), any());
        }
    }

    // === Helper Methods ===

    private ProcessProductSyncCommand createCommand() {
        return new ProcessProductSyncCommand(
                1L, 1L, 100L, 12345L, "CREATE", null, "idempotency-key-123");
    }

    private CrawledProductSyncOutbox createOutbox(SyncType syncType) {
        Long externalProductId = syncType.isCreate() ? null : EXTERNAL_PRODUCT_ID;
        return CrawledProductSyncOutbox.reconstitute(
                CrawledProductSyncOutboxId.of(1L),
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                syncType,
                "sync-key-123",
                externalProductId,
                ProductOutboxStatus.SENT,
                0,
                null,
                FIXED_INSTANT,
                FIXED_INSTANT);
    }

    private Seller createSeller() {
        return Seller.reconstitute(
                SELLER_ID,
                MustItSellerName.of("mustit-seller"),
                SellerName.of("test-seller"),
                999L,
                SellerStatus.ACTIVE,
                0,
                FIXED_INSTANT,
                FIXED_INSTANT);
    }

    private CrawledProduct createMockProduct(Long externalProductId) {
        return CrawledProduct.reconstitute(
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                "Test Product",
                "Test Brand",
                0L,
                ProductPrice.of(10000, 10000, 10000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"),
                null,
                "<p>Test Description</p>",
                "<p>Test Description</p>",
                "ACTIVE",
                "Korea",
                "Seoul",
                ProductOptions.empty(),
                CrawlCompletionStatus.initial()
                        .withMiniShopCrawled(FIXED_INSTANT)
                        .withDetailCrawled(FIXED_INSTANT)
                        .withOptionCrawled(FIXED_INSTANT),
                externalProductId,
                null,
                true,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                FIXED_INSTANT,
                FIXED_INSTANT,
                null);
    }
}
