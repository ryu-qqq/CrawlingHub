package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.product.dto.bundle.SyncOutboxBundle;
import com.ryuqq.crawlinghub.application.product.dto.command.TriggerManualSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.ManualSyncTriggerResponse;
import com.ryuqq.crawlinghub.application.product.factory.SyncOutboxFactory;
import com.ryuqq.crawlinghub.application.product.manager.SyncOutboxManager;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.exception.CrawledProductNotFoundException;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ImageUploadStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImage;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TriggerManualSyncService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TriggerManualSyncService 테스트")
class TriggerManualSyncServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductQueryPort crawledProductQueryPort;
    @Mock private SyncOutboxFactory syncOutboxFactory;
    @Mock private SyncOutboxManager syncOutboxManager;

    private TriggerManualSyncService service;

    @BeforeEach
    void setUp() {
        service =
                new TriggerManualSyncService(
                        crawledProductQueryPort, syncOutboxFactory, syncOutboxManager);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 신규 등록 상품 → CREATE 타입 Outbox 생성")
        void shouldCreateOutboxForNewProduct() {
            // Given
            TriggerManualSyncCommand command = new TriggerManualSyncCommand(1L);
            CrawledProduct product = createSyncableProduct(false);
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForCreate();
            ExternalSyncRequestedEvent event =
                    new ExternalSyncRequestedEvent(
                            outbox.getCrawledProductId(),
                            outbox.getSellerId(),
                            outbox.getItemNo(),
                            outbox.getIdempotencyKey(),
                            ExternalSyncRequestedEvent.SyncType.CREATE,
                            FIXED_INSTANT);
            SyncOutboxBundle bundle = new SyncOutboxBundle(outbox, event);

            given(crawledProductQueryPort.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(syncOutboxFactory.createBundle(any())).willReturn(Optional.of(bundle));

            // When
            ManualSyncTriggerResponse response = service.execute(command);

            // Then
            assertThat(response.crawledProductId()).isEqualTo(1L);
            assertThat(response.syncType()).isEqualTo("CREATE");
            assertThat(response.message()).isEqualTo("동기화 요청이 등록되었습니다.");

            verify(syncOutboxManager).persist(bundle);
        }

        @Test
        @DisplayName("[성공] 기등록 상품 → UPDATE 타입 Outbox 생성")
        void shouldCreateUpdateOutboxForRegisteredProduct() {
            // Given
            TriggerManualSyncCommand command = new TriggerManualSyncCommand(1L);
            CrawledProduct product = createSyncableProduct(true);
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForUpdate();
            ExternalSyncRequestedEvent event =
                    new ExternalSyncRequestedEvent(
                            outbox.getCrawledProductId(),
                            outbox.getSellerId(),
                            outbox.getItemNo(),
                            outbox.getIdempotencyKey(),
                            ExternalSyncRequestedEvent.SyncType.UPDATE,
                            FIXED_INSTANT);
            SyncOutboxBundle bundle = new SyncOutboxBundle(outbox, event);

            given(crawledProductQueryPort.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(syncOutboxFactory.createBundle(any())).willReturn(Optional.of(bundle));

            // When
            ManualSyncTriggerResponse response = service.execute(command);

            // Then
            assertThat(response.syncType()).isEqualTo("UPDATE");
            assertThat(response.message()).isEqualTo("동기화 요청이 등록되었습니다.");

            verify(syncOutboxManager).persist(bundle);
        }

        @Test
        @DisplayName("[스킵] 중복 Outbox 존재 시 스킵 응답 반환")
        void shouldReturnSkippedResponseWhenDuplicateOutbox() {
            // Given
            TriggerManualSyncCommand command = new TriggerManualSyncCommand(1L);
            CrawledProduct product = createSyncableProduct(false);

            given(crawledProductQueryPort.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(syncOutboxFactory.createBundle(any())).willReturn(Optional.empty());

            // When
            ManualSyncTriggerResponse response = service.execute(command);

            // Then
            assertThat(response.crawledProductId()).isEqualTo(1L);
            assertThat(response.syncType()).isNull();
            assertThat(response.message()).contains("이미 PENDING 또는 PROCESSING 상태");

            verifyNoInteractions(syncOutboxManager);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 상품 → CrawledProductNotFoundException")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            TriggerManualSyncCommand command = new TriggerManualSyncCommand(999L);
            given(crawledProductQueryPort.findById(CrawledProductId.of(999L)))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(CrawledProductNotFoundException.class);
        }

        @Test
        @DisplayName("[실패] 동기화 불가 상품 → IllegalStateException")
        void shouldThrowExceptionWhenCannotSync() {
            // Given
            TriggerManualSyncCommand command = new TriggerManualSyncCommand(1L);
            CrawledProduct product = createNonSyncableProduct();
            given(crawledProductQueryPort.findById(PRODUCT_ID)).willReturn(Optional.of(product));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("동기화할 수 없는 상품입니다");
        }
    }

    private CrawledProduct createSyncableProduct(boolean isRegistered) {
        // 업로드 완료된 이미지 생성
        ProductImage uploadedImage =
                new ProductImage(
                        "http://img.test.com/1.jpg",
                        "http://cdn.test.com/1.jpg",
                        ImageType.THUMBNAIL,
                        ImageUploadStatus.UPLOADED,
                        0);
        ProductImages images = ProductImages.of(List.of(uploadedImage));

        // 모든 크롤링 완료 상태
        CrawlCompletionStatus completedStatus =
                CrawlCompletionStatus.initial()
                        .withMiniShopCrawled(FIXED_INSTANT)
                        .withDetailCrawled(FIXED_INSTANT)
                        .withOptionCrawled(FIXED_INSTANT);

        Long externalProductId = isRegistered ? 99999L : null;

        return CrawledProduct.reconstitute(
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                "테스트 상품",
                "테스트 브랜드",
                ProductPrice.of(10000, 12000, 12000, 9000, 10, 15),
                images,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                completedStatus,
                externalProductId,
                null,
                false,
                FIXED_INSTANT,
                null);
    }

    private CrawledProduct createNonSyncableProduct() {
        // 업로드 대기 중인 이미지 (동기화 불가)
        ProductImage pendingImage =
                new ProductImage(
                        "http://img.test.com/1.jpg",
                        null,
                        ImageType.THUMBNAIL,
                        ImageUploadStatus.PENDING,
                        0);
        ProductImages images = ProductImages.of(List.of(pendingImage));

        // MINI_SHOP만 완료된 상태 (동기화 불가)
        CrawlCompletionStatus incompleteStatus =
                CrawlCompletionStatus.initial().withMiniShopCrawled(FIXED_INSTANT);

        return CrawledProduct.reconstitute(
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                "테스트 상품",
                "테스트 브랜드",
                ProductPrice.of(10000, 12000, 12000, 9000, 10, 15),
                images,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                incompleteStatus,
                null,
                null,
                false,
                FIXED_INSTANT,
                null);
    }
}
