package com.ryuqq.crawlinghub.application.product.dto.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProductSyncPayload 단위 테스트
 *
 * <p>from() 팩토리 메서드 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSyncPayload 테스트")
class ProductSyncPayloadTest {

    @Mock private CrawledProductSyncOutbox outbox;

    @Nested
    @DisplayName("from() 팩토리 메서드 테스트")
    class From {

        @Test
        @DisplayName("[성공] CrawledProductSyncOutbox로부터 페이로드 생성 - CREATE 타입")
        void shouldCreateFromOutboxWithCreateType() {
            given(outbox.getIdValue()).willReturn(1L);
            given(outbox.getCrawledProductIdValue()).willReturn(100L);
            given(outbox.getSellerIdValue()).willReturn(10L);
            given(outbox.getItemNo()).willReturn(999L);
            given(outbox.getSyncType()).willReturn(SyncType.CREATE);
            given(outbox.getExternalProductId()).willReturn(null);
            given(outbox.getIdempotencyKey()).willReturn("key-abc");

            ProductSyncPayload payload = ProductSyncPayload.from(outbox);

            assertThat(payload.outboxId()).isEqualTo(1L);
            assertThat(payload.crawledProductId()).isEqualTo(100L);
            assertThat(payload.sellerId()).isEqualTo(10L);
            assertThat(payload.itemNo()).isEqualTo(999L);
            assertThat(payload.syncType()).isEqualTo("CREATE");
            assertThat(payload.externalProductId()).isNull();
            assertThat(payload.idempotencyKey()).isEqualTo("key-abc");
        }

        @Test
        @DisplayName("[성공] CrawledProductSyncOutbox로부터 페이로드 생성 - UPDATE_PRICE 타입")
        void shouldCreateFromOutboxWithUpdatePriceType() {
            given(outbox.getIdValue()).willReturn(2L);
            given(outbox.getCrawledProductIdValue()).willReturn(200L);
            given(outbox.getSellerIdValue()).willReturn(20L);
            given(outbox.getItemNo()).willReturn(888L);
            given(outbox.getSyncType()).willReturn(SyncType.UPDATE_PRICE);
            given(outbox.getExternalProductId()).willReturn(555L);
            given(outbox.getIdempotencyKey()).willReturn("key-xyz");

            ProductSyncPayload payload = ProductSyncPayload.from(outbox);

            assertThat(payload.syncType()).isEqualTo("UPDATE_PRICE");
            assertThat(payload.externalProductId()).isEqualTo(555L);
        }
    }
}
