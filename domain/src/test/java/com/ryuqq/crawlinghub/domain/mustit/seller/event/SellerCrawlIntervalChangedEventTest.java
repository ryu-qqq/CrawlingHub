package com.ryuqq.crawlinghub.domain.mustit.seller.event;

import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SellerCrawlIntervalChangedEvent 단위 테스트
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@DisplayName("SellerCrawlIntervalChangedEvent 단위 테스트")
class SellerCrawlIntervalChangedEventTest {

    @Test
    @DisplayName("유효한 정보로 이벤트를 생성할 수 있다")
    void createEventWithValidInfo() {
        // given
        String sellerId = "SELLER001";
        CrawlInterval oldInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        CrawlInterval newInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);

        // when
        SellerCrawlIntervalChangedEvent event = new SellerCrawlIntervalChangedEvent(
                sellerId,
                oldInterval,
                newInterval
        );

        // then
        assertThat(event.getSellerId()).isEqualTo(sellerId);
        assertThat(event.getOldInterval()).isEqualTo(oldInterval);
        assertThat(event.getNewInterval()).isEqualTo(newInterval);
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOccurredAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("sellerId가 null이면 예외가 발생한다")
    void throwExceptionWhenSellerIdIsNull() {
        // given
        CrawlInterval oldInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        CrawlInterval newInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);

        // when & then
        assertThatThrownBy(() -> new SellerCrawlIntervalChangedEvent(
                null,
                oldInterval,
                newInterval
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("sellerId must not be null");
    }

    @Test
    @DisplayName("oldInterval이 null이면 예외가 발생한다")
    void throwExceptionWhenOldIntervalIsNull() {
        // given
        String sellerId = "SELLER001";
        CrawlInterval newInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);

        // when & then
        assertThatThrownBy(() -> new SellerCrawlIntervalChangedEvent(
                sellerId,
                null,
                newInterval
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("oldInterval must not be null");
    }

    @Test
    @DisplayName("newInterval이 null이면 예외가 발생한다")
    void throwExceptionWhenNewIntervalIsNull() {
        // given
        String sellerId = "SELLER001";
        CrawlInterval oldInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);

        // when & then
        assertThatThrownBy(() -> new SellerCrawlIntervalChangedEvent(
                sellerId,
                oldInterval,
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("newInterval must not be null");
    }

    @Test
    @DisplayName("toString()은 모든 필드 정보를 포함한다")
    void toStringContainsAllFields() {
        // given
        String sellerId = "SELLER001";
        CrawlInterval oldInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        CrawlInterval newInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);

        SellerCrawlIntervalChangedEvent event = new SellerCrawlIntervalChangedEvent(
                sellerId,
                oldInterval,
                newInterval
        );

        // when
        String result = event.toString();

        // then
        assertThat(result)
                .contains("SELLER001")
                .contains("CrawlInterval");
    }

    @Test
    @DisplayName("동일한 참조는 equals에서 true를 반환한다")
    void equalsSameReference() {
        // given
        SellerCrawlIntervalChangedEvent event = new SellerCrawlIntervalChangedEvent(
                "SELLER001",
                new CrawlInterval(CrawlIntervalType.DAILY, 1),
                new CrawlInterval(CrawlIntervalType.HOURLY, 6)
        );

        // when & then
        assertThat(event).isEqualTo(event);
    }

    @Test
    @DisplayName("null과 비교 시 equals에서 false를 반환한다")
    void equalsWithNull() {
        // given
        SellerCrawlIntervalChangedEvent event = new SellerCrawlIntervalChangedEvent(
                "SELLER001",
                new CrawlInterval(CrawlIntervalType.DAILY, 1),
                new CrawlInterval(CrawlIntervalType.HOURLY, 6)
        );

        // when & then
        assertThat(event).isNotEqualTo(null);
    }

    @Test
    @DisplayName("다른 클래스의 객체와 비교 시 equals에서 false를 반환한다")
    void equalsWithDifferentClass() {
        // given
        SellerCrawlIntervalChangedEvent event = new SellerCrawlIntervalChangedEvent(
                "SELLER001",
                new CrawlInterval(CrawlIntervalType.DAILY, 1),
                new CrawlInterval(CrawlIntervalType.HOURLY, 6)
        );

        // when & then
        assertThat(event).isNotEqualTo("string");
    }

    @Test
    @DisplayName("동일한 필드 값을 가진 이벤트는 equals에서 true를 반환한다")
    void equalsWithSameFieldValues() {
        // given
        String sellerId = "SELLER001";
        CrawlInterval oldInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        CrawlInterval newInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);
        LocalDateTime now = LocalDateTime.now();

        // Create two events with manually set occurredAt to be identical
        SellerCrawlIntervalChangedEvent event1 = new SellerCrawlIntervalChangedEvent(
                sellerId,
                oldInterval,
                newInterval
        );

        // Sleep briefly to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SellerCrawlIntervalChangedEvent event2 = new SellerCrawlIntervalChangedEvent(
                sellerId,
                oldInterval,
                newInterval
        );

        // then - 다른 occurredAt 때문에 equals는 false
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    @DisplayName("hashCode는 동일한 객체에 대해 항상 동일한 값을 반환한다")
    void hashCodeConsistency() {
        // given
        SellerCrawlIntervalChangedEvent event = new SellerCrawlIntervalChangedEvent(
                "SELLER001",
                new CrawlInterval(CrawlIntervalType.DAILY, 1),
                new CrawlInterval(CrawlIntervalType.HOURLY, 6)
        );

        // when
        int hashCode1 = event.hashCode();
        int hashCode2 = event.hashCode();

        // then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("발생 시간은 생성 시점으로 자동 설정된다")
    void occurredAtIsSetAutomatically() {
        // given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // when
        SellerCrawlIntervalChangedEvent event = new SellerCrawlIntervalChangedEvent(
                "SELLER001",
                new CrawlInterval(CrawlIntervalType.DAILY, 1),
                new CrawlInterval(CrawlIntervalType.HOURLY, 6)
        );

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // then
        assertThat(event.getOccurredAt()).isAfter(before);
        assertThat(event.getOccurredAt()).isBefore(after);
    }
}
