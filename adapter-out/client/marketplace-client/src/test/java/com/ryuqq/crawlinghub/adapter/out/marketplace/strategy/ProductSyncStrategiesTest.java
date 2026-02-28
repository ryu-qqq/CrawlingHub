package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.crawlinghub.adapter.out.marketplace.client.MarketPlaceClient;
import com.ryuqq.crawlinghub.adapter.out.marketplace.client.MarketPlaceClientException;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateDescriptionRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateImagesRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdatePriceRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateProductsRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response.InboundProductConversionResponse;
import com.ryuqq.crawlinghub.adapter.out.marketplace.mapper.InboundProductRequestMapper;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductSyncOutboxId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProductSyncStrategy 구현체 테스트")
class ProductSyncStrategiesTest {

    private final MarketPlaceClient mockClient = mock(MarketPlaceClient.class);
    private final InboundProductRequestMapper mockMapper = mock(InboundProductRequestMapper.class);

    // --- 공통 헬퍼 ---

    private CrawledProductSyncOutbox createOutbox(SyncType syncType) {
        Long externalProductId = syncType.isUpdate() ? 99999L : null;
        return CrawledProductSyncOutbox.reconstitute(
                CrawledProductSyncOutboxId.of(1L),
                CrawledProductId.of(100L),
                SellerId.of(200L),
                12345L,
                syncType,
                "test-idem-key",
                externalProductId,
                ProductOutboxStatus.PENDING,
                0,
                null,
                Instant.now(),
                null);
    }

    private CrawledProduct createProduct() {
        return CrawledProduct.fromMiniShop(
                SellerId.of(200L),
                12345L,
                "테스트 상품명",
                "테스트 브랜드",
                new ProductPrice(10000, 0, 12000, 0, 0, 0),
                null,
                false,
                Instant.now());
    }

    private Seller createSeller() {
        return Seller.reconstitute(
                SellerId.of(200L),
                MustItSellerName.of("mustit-seller"),
                SellerName.of("test-seller"),
                999L,
                SellerStatus.ACTIVE,
                0,
                Instant.now(),
                Instant.now());
    }

    // ================================================================
    // CreateProductSyncStrategy
    // ================================================================

    @Nested
    @DisplayName("CreateProductSyncStrategy 테스트")
    class CreateProductSyncStrategyTest {

        private final CreateProductSyncStrategy strategy =
                new CreateProductSyncStrategy(mockClient, mockMapper);

        @Test
        @DisplayName("supportedType은 CREATE를 반환한다")
        void supportedType_returnsCreate() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.CREATE);
        }

        @Test
        @DisplayName("execute 성공 시 inboundProductId를 포함한 성공 결과를 반환한다")
        void execute_success_returnsSuccessResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.CREATE);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();

            ReceiveInboundProductRequest mockRequest =
                    new ReceiveInboundProductRequest(
                            1L,
                            "12345",
                            "상품",
                            "브랜드",
                            "M001",
                            999L,
                            10000,
                            8000,
                            "NONE",
                            List.of(),
                            List.of(),
                            List.of(),
                            new ReceiveInboundProductRequest.DescriptionRequest("desc"),
                            null);
            InboundProductConversionResponse mockResponse =
                    new InboundProductConversionResponse(500001L, 1L, "CONVERTED", "CREATE");

            when(mockMapper.toReceiveRequest(outbox, product, seller)).thenReturn(mockRequest);
            when(mockClient.receiveInboundProduct(mockRequest)).thenReturn(mockResponse);

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(500001L);
            verify(mockClient).receiveInboundProduct(mockRequest);
        }

        @Test
        @DisplayName("execute 실패 시 CREATE_FAILED 에러 코드를 반환한다")
        void execute_failure_returnsFailureResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.CREATE);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();

            when(mockMapper.toReceiveRequest(any(), any(), any()))
                    .thenThrow(new MarketPlaceClientException("API 호출 실패"));

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("CREATE_FAILED");
            assertThat(result.errorMessage()).contains("API 호출 실패");
        }
    }

    // ================================================================
    // UpdatePriceSyncStrategy
    // ================================================================

    @Nested
    @DisplayName("UpdatePriceSyncStrategy 테스트")
    class UpdatePriceSyncStrategyTest {

        private final UpdatePriceSyncStrategy strategy =
                new UpdatePriceSyncStrategy(mockClient, mockMapper);

        @Test
        @DisplayName("supportedType은 UPDATE_PRICE를 반환한다")
        void supportedType_returnsUpdatePrice() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_PRICE);
        }

        @Test
        @DisplayName("execute 성공 시 externalProductId를 포함한 성공 결과를 반환한다")
        void execute_success_returnsSuccessResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_PRICE);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdatePriceRequest mockRequest = new UpdatePriceRequest(10000, 8000);

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdatePriceRequest(product)).thenReturn(mockRequest);

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
            verify(mockClient).updatePrice(1L, "12345", mockRequest);
        }

        @Test
        @DisplayName("execute 실패 시 UPDATE_PRICE_FAILED 에러 코드를 반환한다")
        void execute_failure_returnsFailureResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_PRICE);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdatePriceRequest mockRequest = new UpdatePriceRequest(10000, 8000);

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdatePriceRequest(product)).thenReturn(mockRequest);
            doThrow(new MarketPlaceClientException("API 호출 실패"))
                    .when(mockClient)
                    .updatePrice(anyLong(), anyString(), any());

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("UPDATE_PRICE_FAILED");
        }
    }

    // ================================================================
    // UpdateImageSyncStrategy
    // ================================================================

    @Nested
    @DisplayName("UpdateImageSyncStrategy 테스트")
    class UpdateImageSyncStrategyTest {

        private final UpdateImageSyncStrategy strategy =
                new UpdateImageSyncStrategy(mockClient, mockMapper);

        @Test
        @DisplayName("supportedType은 UPDATE_IMAGE를 반환한다")
        void supportedType_returnsUpdateImage() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_IMAGE);
        }

        @Test
        @DisplayName("execute 성공 시 externalProductId를 포함한 성공 결과를 반환한다")
        void execute_success_returnsSuccessResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_IMAGE);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdateImagesRequest mockRequest = new UpdateImagesRequest(List.of());

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdateImagesRequest(product)).thenReturn(mockRequest);

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
            verify(mockClient).updateImages(1L, "12345", mockRequest);
        }

        @Test
        @DisplayName("execute 실패 시 UPDATE_IMAGE_FAILED 에러 코드를 반환한다")
        void execute_failure_returnsFailureResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_IMAGE);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdateImagesRequest mockRequest = new UpdateImagesRequest(List.of());

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdateImagesRequest(product)).thenReturn(mockRequest);
            doThrow(new MarketPlaceClientException("API 호출 실패"))
                    .when(mockClient)
                    .updateImages(anyLong(), anyString(), any());

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("UPDATE_IMAGE_FAILED");
        }
    }

    // ================================================================
    // UpdateDescriptionSyncStrategy
    // ================================================================

    @Nested
    @DisplayName("UpdateDescriptionSyncStrategy 테스트")
    class UpdateDescriptionSyncStrategyTest {

        private final UpdateDescriptionSyncStrategy strategy =
                new UpdateDescriptionSyncStrategy(mockClient, mockMapper);

        @Test
        @DisplayName("supportedType은 UPDATE_DESCRIPTION을 반환한다")
        void supportedType_returnsUpdateDescription() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_DESCRIPTION);
        }

        @Test
        @DisplayName("execute 성공 시 externalProductId를 포함한 성공 결과를 반환한다")
        void execute_success_returnsSuccessResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_DESCRIPTION);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdateDescriptionRequest mockRequest = new UpdateDescriptionRequest("<p>desc</p>");

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdateDescriptionRequest(product)).thenReturn(mockRequest);

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
            verify(mockClient).updateDescription(1L, "12345", mockRequest);
        }

        @Test
        @DisplayName("execute 실패 시 UPDATE_DESCRIPTION_FAILED 에러 코드를 반환한다")
        void execute_failure_returnsFailureResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_DESCRIPTION);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdateDescriptionRequest mockRequest = new UpdateDescriptionRequest("<p>desc</p>");

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdateDescriptionRequest(product)).thenReturn(mockRequest);
            doThrow(new MarketPlaceClientException("API 호출 실패"))
                    .when(mockClient)
                    .updateDescription(anyLong(), anyString(), any());

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("UPDATE_DESCRIPTION_FAILED");
        }
    }

    // ================================================================
    // UpdateOptionStockSyncStrategy
    // ================================================================

    @Nested
    @DisplayName("UpdateOptionStockSyncStrategy 테스트")
    class UpdateOptionStockSyncStrategyTest {

        private final UpdateOptionStockSyncStrategy strategy =
                new UpdateOptionStockSyncStrategy(mockClient, mockMapper);

        @Test
        @DisplayName("supportedType은 UPDATE_OPTION_STOCK을 반환한다")
        void supportedType_returnsUpdateOptionStock() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_OPTION_STOCK);
        }

        @Test
        @DisplayName("execute 성공 시 externalProductId를 포함한 성공 결과를 반환한다")
        void execute_success_returnsSuccessResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_OPTION_STOCK);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdateProductsRequest mockRequest = new UpdateProductsRequest(List.of(), List.of());

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdateProductsRequest(product)).thenReturn(mockRequest);

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
            verify(mockClient).updateProducts(1L, "12345", mockRequest);
        }

        @Test
        @DisplayName("execute 실패 시 UPDATE_OPTION_STOCK_FAILED 에러 코드를 반환한다")
        void execute_failure_returnsFailureResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_OPTION_STOCK);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdateProductsRequest mockRequest = new UpdateProductsRequest(List.of(), List.of());

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdateProductsRequest(product)).thenReturn(mockRequest);
            doThrow(new MarketPlaceClientException("API 호출 실패"))
                    .when(mockClient)
                    .updateProducts(anyLong(), anyString(), any());

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("UPDATE_OPTION_STOCK_FAILED");
        }
    }

    // ================================================================
    // UpdateProductInfoSyncStrategy
    // ================================================================

    @Nested
    @DisplayName("UpdateProductInfoSyncStrategy 테스트")
    class UpdateProductInfoSyncStrategyTest {

        private final UpdateProductInfoSyncStrategy strategy =
                new UpdateProductInfoSyncStrategy(mockClient, mockMapper);

        @Test
        @DisplayName("supportedType은 UPDATE_PRODUCT_INFO를 반환한다")
        void supportedType_returnsUpdateProductInfo() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_PRODUCT_INFO);
        }

        @Test
        @DisplayName("execute 성공 시 externalProductId를 포함한 성공 결과를 반환한다")
        void execute_success_returnsSuccessResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_PRODUCT_INFO);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdateProductsRequest mockRequest = new UpdateProductsRequest(List.of(), List.of());

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdateProductsRequest(product)).thenReturn(mockRequest);

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
            verify(mockClient).updateProducts(1L, "12345", mockRequest);
        }

        @Test
        @DisplayName("execute 실패 시 UPDATE_PRODUCT_INFO_FAILED 에러 코드를 반환한다")
        void execute_failure_returnsFailureResult() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_PRODUCT_INFO);
            CrawledProduct product = createProduct();
            Seller seller = createSeller();
            UpdateProductsRequest mockRequest = new UpdateProductsRequest(List.of(), List.of());

            when(mockMapper.getInboundSourceId()).thenReturn(1L);
            when(mockMapper.getExternalProductCode(outbox)).thenReturn("12345");
            when(mockMapper.toUpdateProductsRequest(product)).thenReturn(mockRequest);
            doThrow(new MarketPlaceClientException("API 호출 실패"))
                    .when(mockClient)
                    .updateProducts(anyLong(), anyString(), any());

            // when
            ProductSyncResult result = strategy.execute(outbox, product, seller);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("UPDATE_PRODUCT_INFO_FAILED");
        }
    }
}
