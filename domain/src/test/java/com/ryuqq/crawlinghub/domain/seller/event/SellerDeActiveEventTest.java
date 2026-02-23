package com.ryuqq.crawlinghub.domain.seller.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("event")
@DisplayName("SellerDeActiveEvent 단위 테스트")
class SellerDeActiveEventTest {

    private static final SellerId SELLER_ID = SellerId.of(1L);
    private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 값으로 생성한다")
        void createWithValidValues() {
            SellerDeActiveEvent event = new SellerDeActiveEvent(SELLER_ID, NOW);
            assertThat(event.sellerId()).isEqualTo(SELLER_ID);
            assertThat(event.occurredAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("sellerId가 null이면 예외가 발생한다")
        void nullSellerIdThrowsException() {
            assertThatThrownBy(() -> new SellerDeActiveEvent(null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId");
        }

        @Test
        @DisplayName("occurredAt이 null이면 예외가 발생한다")
        void nullOccurredAtThrowsException() {
            assertThatThrownBy(() -> new SellerDeActiveEvent(SELLER_ID, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("occurredAt");
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("팩토리 메서드로 생성한다")
        void createWithFactory() {
            SellerDeActiveEvent event = SellerDeActiveEvent.of(SELLER_ID, NOW);
            assertThat(event.sellerId()).isEqualTo(SELLER_ID);
            assertThat(event.occurredAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("getSellerIdValue() 테스트")
    class GetSellerIdValueTest {

        @Test
        @DisplayName("sellerId의 long 값을 반환한다")
        void returnsSellerIdLongValue() {
            SellerDeActiveEvent event = SellerDeActiveEvent.of(SELLER_ID, NOW);
            assertThat(event.getSellerIdValue()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("eventType() 테스트")
    class EventTypeTest {

        @Test
        @DisplayName("이벤트 타입은 클래스 단순명을 반환한다")
        void returnsClassSimpleName() {
            SellerDeActiveEvent event = SellerDeActiveEvent.of(SELLER_ID, NOW);
            assertThat(event.eventType()).isEqualTo("SellerDeActiveEvent");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            SellerDeActiveEvent event1 = new SellerDeActiveEvent(SELLER_ID, NOW);
            SellerDeActiveEvent event2 = new SellerDeActiveEvent(SELLER_ID, NOW);
            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("다른 sellerId이면 다르다")
        void differentSellerIdAreNotEqual() {
            SellerDeActiveEvent event1 = SellerDeActiveEvent.of(SellerId.of(1L), NOW);
            SellerDeActiveEvent event2 = SellerDeActiveEvent.of(SellerId.of(2L), NOW);
            assertThat(event1).isNotEqualTo(event2);
        }
    }
}
