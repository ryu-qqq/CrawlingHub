package com.ryuqq.crawlinghub.domain.mustit.seller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * MustitSeller Unit Test
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@DisplayName("MustitSeller 테스트")
class MustitSellerTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("신규 셀러 생성 성공")
        void create_success() {
            // when
            MustitSeller seller = MustitSeller.create(
                    "SELLER001",
                    "Example Seller",
                    true,
                    CrawlIntervalType.HOURS,
                    2
            );

            // then
            assertThat(seller.getSellerId()).isEqualTo("SELLER001");
            assertThat(seller.getName()).isEqualTo("Example Seller");
            assertThat(seller.isActive()).isTrue();
            assertThat(seller.getCrawlInterval().getIntervalType()).isEqualTo(CrawlIntervalType.HOURS);
            assertThat(seller.getCrawlInterval().getIntervalValue()).isEqualTo(2);
            assertThat(seller.getCreatedAt()).isNotNull();
            assertThat(seller.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("sellerId가 null이면 예외 발생")
        void create_fail_nullSellerId() {
            // when & then
            assertThatThrownBy(() -> MustitSeller.create(null, "Name", true, CrawlIntervalType.HOURS, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller ID must not be null or empty");
        }

        @Test
        @DisplayName("name이 null이면 예외 발생")
        void create_fail_nullName() {
            // when & then
            assertThatThrownBy(() -> MustitSeller.create("SELLER001", null, true, CrawlIntervalType.HOURS, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller name must not be null or empty");
        }

        @Test
        @DisplayName("DB 재구성 성공")
        void reconstitute_success() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 10, 30, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 10, 30, 12, 0);
            CrawlInterval interval = CrawlInterval.of(CrawlIntervalType.DAYS, 1);

            // when
            MustitSeller seller = MustitSeller.reconstitute(
                    "SELLER001",
                    "Example Seller",
                    true,
                    interval,
                    createdAt,
                    updatedAt
            );

            // then
            assertThat(seller.getSellerId()).isEqualTo("SELLER001");
            assertThat(seller.getCreatedAt()).isEqualTo(createdAt);
            assertThat(seller.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    class BusinessMethodTest {

        @Test
        @DisplayName("크롤링 주기 변경 성공")
        void changeCrawlInterval_success() {
            // given
            MustitSeller seller = MustitSeller.create("SELLER001", "Name", true, CrawlIntervalType.HOURS, 2);
            LocalDateTime beforeUpdate = seller.getUpdatedAt();

            // when
            seller.changeCrawlInterval(CrawlIntervalType.DAYS, 1);

            // then
            assertThat(seller.getCrawlInterval().getIntervalType()).isEqualTo(CrawlIntervalType.DAYS);
            assertThat(seller.getCrawlInterval().getIntervalValue()).isEqualTo(1);
            assertThat(seller.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("활성화 성공")
        void activate_success() {
            // given
            MustitSeller seller = MustitSeller.create("SELLER001", "Name", false, CrawlIntervalType.HOURS, 2);
            LocalDateTime beforeUpdate = seller.getUpdatedAt();

            // when
            seller.activate();

            // then
            assertThat(seller.isActive()).isTrue();
            assertThat(seller.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("비활성화 성공")
        void deactivate_success() {
            // given
            MustitSeller seller = MustitSeller.create("SELLER001", "Name", true, CrawlIntervalType.HOURS, 2);
            LocalDateTime beforeUpdate = seller.getUpdatedAt();

            // when
            seller.deactivate();

            // then
            assertThat(seller.isActive()).isFalse();
            assertThat(seller.getUpdatedAt()).isAfter(beforeUpdate);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("sellerId가 같으면 동등함")
        void equals_sameSellerId() {
            // given
            MustitSeller seller1 = MustitSeller.create("SELLER001", "Name1", true, CrawlIntervalType.HOURS, 2);
            MustitSeller seller2 = MustitSeller.create("SELLER001", "Name2", false, CrawlIntervalType.DAYS, 1);

            // then
            assertThat(seller1).isEqualTo(seller2);
            assertThat(seller1.hashCode()).isEqualTo(seller2.hashCode());
        }

        @Test
        @DisplayName("sellerId가 다르면 동등하지 않음")
        void equals_differentSellerId() {
            // given
            MustitSeller seller1 = MustitSeller.create("SELLER001", "Name", true, CrawlIntervalType.HOURS, 2);
            MustitSeller seller2 = MustitSeller.create("SELLER002", "Name", true, CrawlIntervalType.HOURS, 2);

            // then
            assertThat(seller1).isNotEqualTo(seller2);
        }
    }
}
