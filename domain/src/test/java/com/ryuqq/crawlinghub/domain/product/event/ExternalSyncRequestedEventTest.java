package com.ryuqq.crawlinghub.domain.product.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent.SyncType;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
@Tag("domain")
@Tag("event")
@DisplayName("ExternalSyncRequestedEvent 단위 테스트")
class ExternalSyncRequestedEventTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;
    private static final String IDEMPOTENCY_KEY = "sync-1-create-abc12345";

    @Nested
    @DisplayName("SyncType Enum")
    class SyncTypeEnum {

        @Test
        @DisplayName("CREATE 타입이 존재한다")
        void shouldHaveCreateType() {
            assertThat(SyncType.CREATE).isNotNull();
            assertThat(SyncType.CREATE.name()).isEqualTo("CREATE");
        }

        @Test
        @DisplayName("UPDATE 타입이 존재한다")
        void shouldHaveUpdateType() {
            assertThat(SyncType.UPDATE).isNotNull();
            assertThat(SyncType.UPDATE.name()).isEqualTo("UPDATE");
        }

        @Test
        @DisplayName("총 2개의 타입이 존재한다")
        void shouldHaveTwoTypes() {
            assertThat(SyncType.values()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("유효한 값으로 이벤트를 생성한다")
        void shouldCreateEventWithValidValues() {
            // When
            ExternalSyncRequestedEvent event =
                    new ExternalSyncRequestedEvent(
                            PRODUCT_ID,
                            SELLER_ID,
                            ITEM_NO,
                            IDEMPOTENCY_KEY,
                            SyncType.CREATE,
                            FIXED_INSTANT);

            // Then
            assertThat(event.crawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(event.sellerId()).isEqualTo(SELLER_ID);
            assertThat(event.itemNo()).isEqualTo(ITEM_NO);
            assertThat(event.idempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
            assertThat(event.syncType()).isEqualTo(SyncType.CREATE);
            assertThat(event.occurredAt()).isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("crawledProductId가 null이면 예외를 던진다")
        void shouldThrowWhenProductIdIsNull() {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    new ExternalSyncRequestedEvent(
                                            null,
                                            SELLER_ID,
                                            ITEM_NO,
                                            IDEMPOTENCY_KEY,
                                            SyncType.CREATE,
                                            FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("crawledProductId");
        }

        @Test
        @DisplayName("sellerId가 null이면 예외를 던진다")
        void shouldThrowWhenSellerIdIsNull() {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    new ExternalSyncRequestedEvent(
                                            PRODUCT_ID,
                                            null,
                                            ITEM_NO,
                                            IDEMPOTENCY_KEY,
                                            SyncType.CREATE,
                                            FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId");
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L, Long.MIN_VALUE})
        @DisplayName("itemNo가 0 이하면 예외를 던진다")
        void shouldThrowWhenItemNoIsNotPositive(long itemNo) {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    new ExternalSyncRequestedEvent(
                                            PRODUCT_ID,
                                            SELLER_ID,
                                            itemNo,
                                            IDEMPOTENCY_KEY,
                                            SyncType.CREATE,
                                            FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemNo");
        }

        @Test
        @DisplayName("idempotencyKey가 null이면 예외를 던진다")
        void shouldThrowWhenIdempotencyKeyIsNull() {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    new ExternalSyncRequestedEvent(
                                            PRODUCT_ID,
                                            SELLER_ID,
                                            ITEM_NO,
                                            null,
                                            SyncType.CREATE,
                                            FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("idempotencyKey");
        }

        @Test
        @DisplayName("idempotencyKey가 빈 문자열이면 예외를 던진다")
        void shouldThrowWhenIdempotencyKeyIsBlank() {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    new ExternalSyncRequestedEvent(
                                            PRODUCT_ID,
                                            SELLER_ID,
                                            ITEM_NO,
                                            "   ",
                                            SyncType.CREATE,
                                            FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("idempotencyKey");
        }

        @Test
        @DisplayName("syncType이 null이면 예외를 던진다")
        void shouldThrowWhenSyncTypeIsNull() {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    new ExternalSyncRequestedEvent(
                                            PRODUCT_ID,
                                            SELLER_ID,
                                            ITEM_NO,
                                            IDEMPOTENCY_KEY,
                                            null,
                                            FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("syncType");
        }

        @Test
        @DisplayName("occurredAt이 null이면 예외를 던진다")
        void shouldThrowWhenOccurredAtIsNull() {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    new ExternalSyncRequestedEvent(
                                            PRODUCT_ID,
                                            SELLER_ID,
                                            ITEM_NO,
                                            IDEMPOTENCY_KEY,
                                            SyncType.CREATE,
                                            null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("occurredAt");
        }
    }

    @Nested
    @DisplayName("forCreate 팩토리 메서드")
    class ForCreateMethod {

        @Test
        @DisplayName("신규 등록용 이벤트를 생성한다")
        void shouldCreateEventForCreate() {
            // When
            ExternalSyncRequestedEvent event =
                    ExternalSyncRequestedEvent.forCreate(
                            PRODUCT_ID, SELLER_ID, ITEM_NO, IDEMPOTENCY_KEY, FIXED_INSTANT);

            // Then
            assertThat(event.crawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(event.sellerId()).isEqualTo(SELLER_ID);
            assertThat(event.itemNo()).isEqualTo(ITEM_NO);
            assertThat(event.idempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
            assertThat(event.syncType()).isEqualTo(SyncType.CREATE);
            assertThat(event.occurredAt()).isEqualTo(FIXED_INSTANT);
        }
    }

    @Nested
    @DisplayName("forUpdate 팩토리 메서드")
    class ForUpdateMethod {

        @Test
        @DisplayName("갱신용 이벤트를 생성한다")
        void shouldCreateEventForUpdate() {
            // When
            ExternalSyncRequestedEvent event =
                    ExternalSyncRequestedEvent.forUpdate(
                            PRODUCT_ID, SELLER_ID, ITEM_NO, IDEMPOTENCY_KEY, FIXED_INSTANT);

            // Then
            assertThat(event.crawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(event.sellerId()).isEqualTo(SELLER_ID);
            assertThat(event.itemNo()).isEqualTo(ITEM_NO);
            assertThat(event.idempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
            assertThat(event.syncType()).isEqualTo(SyncType.UPDATE);
            assertThat(event.occurredAt()).isEqualTo(FIXED_INSTANT);
        }
    }

    @Nested
    @DisplayName("DomainEvent 인터페이스")
    class DomainEventInterface {

        @Test
        @DisplayName("DomainEvent 인터페이스를 구현한다")
        void shouldImplementDomainEvent() {
            // Given
            ExternalSyncRequestedEvent event =
                    new ExternalSyncRequestedEvent(
                            PRODUCT_ID,
                            SELLER_ID,
                            ITEM_NO,
                            IDEMPOTENCY_KEY,
                            SyncType.CREATE,
                            FIXED_INSTANT);

            // When & Then
            assertThat(event)
                    .isInstanceOf(com.ryuqq.crawlinghub.domain.common.event.DomainEvent.class);
        }
    }
}
