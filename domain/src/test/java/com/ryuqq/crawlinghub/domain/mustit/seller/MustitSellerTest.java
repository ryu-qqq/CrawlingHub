package com.ryuqq.crawlinghub.domain.mustit.seller;

import com.ryuqq.crawlinghub.domain.common.DomainEvent;
import com.ryuqq.crawlinghub.domain.mustit.seller.event.SellerCrawlIntervalChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MustitSeller Aggregate 단위 테스트
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@DisplayName("MustitSeller 단위 테스트")
class MustitSellerTest {

    @Test
    @DisplayName("유효한 정보로 새로운 셀러를 생성할 수 있다")
    void createNewSellerWithValidInfo() {
        // given
        String sellerId = "SELLER001";
        String name = "Test Seller";
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);

        // when
        MustitSeller seller = new MustitSeller(sellerId, name, crawlInterval);

        // then
        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.isActive()).isTrue(); // 기본값: 활성
        assertThat(seller.getCrawlInterval()).isEqualTo(crawlInterval);
        assertThat(seller.getCreatedAt()).isNotNull();
        assertThat(seller.getUpdatedAt()).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("sellerId가 null이거나 빈 문자열이면 예외가 발생한다")
    void throwExceptionWhenSellerIdIsNullOrBlank(String invalidSellerId) {
        // given
        String name = "Test Seller";
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);

        // when & then
        assertThatThrownBy(() -> new MustitSeller(invalidSellerId, name, crawlInterval))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId must not be null or blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("name이 null이거나 빈 문자열이면 예외가 발생한다")
    void throwExceptionWhenNameIsNullOrBlank(String invalidName) {
        // given
        String sellerId = "SELLER001";
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);

        // when & then
        assertThatThrownBy(() -> new MustitSeller(sellerId, invalidName, crawlInterval))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be null or blank");
    }

    @Test
    @DisplayName("crawlInterval이 null이면 예외가 발생한다")
    void throwExceptionWhenCrawlIntervalIsNull() {
        // given
        String sellerId = "SELLER001";
        String name = "Test Seller";

        // when & then
        assertThatThrownBy(() -> new MustitSeller(sellerId, name, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("crawlInterval must not be null");
    }

    @Test
    @DisplayName("크롤링 주기를 변경할 수 있다")
    void updateCrawlInterval() {
        // given
        MustitSeller seller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );
        LocalDateTime originalUpdatedAt = seller.getUpdatedAt();

        // when
        CrawlInterval newInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);
        seller.updateCrawlInterval(newInterval);

        // then
        assertThat(seller.getCrawlInterval()).isEqualTo(newInterval);
        assertThat(seller.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("크롤링 주기 변경 시 null이면 예외가 발생한다")
    void throwExceptionWhenUpdatingWithNullCrawlInterval() {
        // given
        MustitSeller seller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );

        // when & then
        assertThatThrownBy(() -> seller.updateCrawlInterval(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("newCrawlInterval must not be null");
    }

    @Test
    @DisplayName("셀러를 활성화할 수 있다")
    void activateSeller() {
        // given
        SellerBasicInfo basicInfo = SellerBasicInfo.of(
                1L,  // id
                "SELLER001",
                "Test Seller",
                true
        );
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        SellerTimeInfo timeInfo = SellerTimeInfo.of(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        MustitSeller seller = MustitSeller.reconstitute(basicInfo, crawlInterval, timeInfo);
        seller.deactivate();
        LocalDateTime deactivatedUpdatedAt = seller.getUpdatedAt();

        // when
        seller.activate();

        // then
        assertThat(seller.isActive()).isTrue();
        assertThat(seller.getUpdatedAt()).isAfter(deactivatedUpdatedAt);
    }

    @Test
    @DisplayName("셀러를 비활성화할 수 있다")
    void deactivateSeller() {
        // given
        MustitSeller seller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );
        LocalDateTime originalUpdatedAt = seller.getUpdatedAt();

        // when
        seller.deactivate();

        // then
        assertThat(seller.isActive()).isFalse();
        assertThat(seller.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("동일한 sellerId를 가진 셀러는 동등하다")
    void equalsWithSameSellerId() {
        // given
        String sellerId = "SELLER001";
        MustitSeller seller1 = new MustitSeller(
                sellerId,
                "Seller One",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );
        MustitSeller seller2 = new MustitSeller(
                sellerId,
                "Seller Two",
                new CrawlInterval(CrawlIntervalType.HOURLY, 2)
        );

        // when & then
        assertThat(seller1).isEqualTo(seller2);
        assertThat(seller1.hashCode()).isEqualTo(seller2.hashCode());
    }

    @Test
    @DisplayName("다른 sellerId를 가진 셀러는 동등하지 않다")
    void notEqualsWithDifferentSellerId() {
        // given
        MustitSeller seller1 = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );
        MustitSeller seller2 = new MustitSeller(
                "SELLER002",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );

        // when & then
        assertThat(seller1).isNotEqualTo(seller2);
    }

    @Test
    @DisplayName("기존 셀러 정보를 재구성할 수 있다 (Persistence 로드용)")
    void reconstructExistingSeller() {
        // given
        Long id = 2L;
        String sellerId = "SELLER001";
        String name = "Test Seller";
        boolean isActive = false;
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.WEEKLY, 2);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(5);

        // when
        SellerBasicInfo basicInfo = SellerBasicInfo.of(
                id,
                sellerId,
                name,
                isActive
        );
        SellerTimeInfo timeInfo = SellerTimeInfo.of(createdAt, updatedAt);
        MustitSeller seller = MustitSeller.reconstitute(basicInfo, crawlInterval, timeInfo);

        // then
        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.isActive()).isFalse();
        assertThat(seller.getCrawlInterval()).isEqualTo(crawlInterval);
        assertThat(seller.getCreatedAt()).isEqualTo(createdAt);
        assertThat(seller.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("toString()은 모든 필드 정보를 포함한다")
    void toStringContainsAllFields() {
        // given
        MustitSeller seller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );

        // when
        String result = seller.toString();

        // then
        assertThat(result)
                .contains("SELLER001")
                .contains("Test Seller")
                .contains("true") // isActive
                .contains("CrawlInterval");
    }

    @Test
    @DisplayName("크롤링 주기 변경 시 Domain Event가 발행된다")
    void publishDomainEventWhenCrawlIntervalChanged() {
        // given
        MustitSeller seller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );

        // when
        CrawlInterval newInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);
        seller.updateCrawlInterval(newInterval);

        // then
        List<DomainEvent> domainEvents = seller.getDomainEvents();
        assertThat(domainEvents).hasSize(1);
        assertThat(domainEvents.get(0)).isInstanceOf(SellerCrawlIntervalChangedEvent.class);

        SellerCrawlIntervalChangedEvent event = (SellerCrawlIntervalChangedEvent) domainEvents.get(0);
        assertThat(event.getSellerId()).isEqualTo("SELLER001");
        assertThat(event.getOldInterval().getIntervalType()).isEqualTo(CrawlIntervalType.DAILY);
        assertThat(event.getOldInterval().getIntervalValue()).isEqualTo(1);
        assertThat(event.getNewInterval().getIntervalType()).isEqualTo(CrawlIntervalType.HOURLY);
        assertThat(event.getNewInterval().getIntervalValue()).isEqualTo(6);
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    @DisplayName("동일한 크롤링 주기로 변경 시 Domain Event가 발행되지 않는다")
    void notPublishDomainEventWhenCrawlIntervalNotChanged() {
        // given
        CrawlInterval interval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        MustitSeller seller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                interval
        );

        // when
        CrawlInterval sameInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        seller.updateCrawlInterval(sameInterval);

        // then
        List<DomainEvent> domainEvents = seller.getDomainEvents();
        assertThat(domainEvents).isEmpty();
    }

    @Test
    @DisplayName("Domain Event를 정리할 수 있다")
    void clearDomainEvents() {
        // given
        MustitSeller seller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );
        CrawlInterval newInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);
        seller.updateCrawlInterval(newInterval);

        assertThat(seller.getDomainEvents()).isNotEmpty();

        // when
        seller.clearDomainEvents();

        // then
        assertThat(seller.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("크롤링 주기가 변경된 경우 isModified()는 true를 반환한다")
    void isModifiedReturnsTrueWhenCrawlIntervalChanged() {
        // given
        SellerBasicInfo basicInfo = SellerBasicInfo.of(
                3L,  // id
                "SELLER001",
                "Test Seller",
                true
        );
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        SellerTimeInfo timeInfo = SellerTimeInfo.of(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        MustitSeller seller = MustitSeller.reconstitute(basicInfo, crawlInterval, timeInfo);

        // when
        CrawlInterval newInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);
        seller.updateCrawlInterval(newInterval);

        // then
        assertThat(seller.isModified()).isTrue();
    }

    @Test
    @DisplayName("크롤링 주기가 변경되지 않은 경우 isModified()는 false를 반환한다")
    void isModifiedReturnsFalseWhenCrawlIntervalNotChanged() {
        // given
        SellerBasicInfo basicInfo = SellerBasicInfo.of(
                4L,  // id
                "SELLER001",
                "Test Seller",
                true
        );
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        SellerTimeInfo timeInfo = SellerTimeInfo.of(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        MustitSeller seller = MustitSeller.reconstitute(basicInfo, crawlInterval, timeInfo);

        // when - 활성화 상태만 변경
        seller.deactivate();

        // then
        assertThat(seller.isModified()).isFalse();
    }

    @Test
    @DisplayName("새로 생성된 셀러의 경우 isModified()는 false를 반환한다")
    void isModifiedReturnsFalseForNewSeller() {
        // given
        MustitSeller seller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );

        // when & then
        assertThat(seller.isModified()).isFalse();
    }
}
