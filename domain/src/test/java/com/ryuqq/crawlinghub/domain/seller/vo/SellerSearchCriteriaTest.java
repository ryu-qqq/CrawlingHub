package com.ryuqq.crawlinghub.domain.seller.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SellerSearchCriteria Value Object 테스트
 *
 * <p>TDD Phase: Green</p>
 * <ul>
 *   <li>정적 팩토리 메서드 검증</li>
 *   <li>Null-safe 설계 검증</li>
 *   <li>Record accessor 메서드 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@DisplayName("SellerSearchCriteria 테스트")
class SellerSearchCriteriaTest {

    @Test
    @DisplayName("of() 정적 팩토리 메서드로 생성 - 모든 필드 제공")
    void shouldCreateWithAllFields() {
        // given
        String sellerId = "seller_12345";
        String sellerName = "무신사";
        Boolean active = true;
        LocalDateTime fromCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime toCreatedAt = LocalDateTime.of(2025, 12, 31, 23, 59);

        // when
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
                sellerId, sellerName, active, fromCreatedAt, toCreatedAt
        );

        // then
        assertThat(criteria.sellerId()).isEqualTo(sellerId);
        assertThat(criteria.sellerName()).isEqualTo(sellerName);
        assertThat(criteria.active()).isEqualTo(active);
        assertThat(criteria.fromCreatedAt()).isEqualTo(fromCreatedAt);
        assertThat(criteria.toCreatedAt()).isEqualTo(toCreatedAt);
    }

    @Test
    @DisplayName("of() 정적 팩토리 메서드로 생성 - Seller ID만 제공")
    void shouldCreateWithSellerIdOnly() {
        // given
        String sellerId = "seller_12345";

        // when
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
                sellerId, null, null, null, null
        );

        // then
        assertThat(criteria.sellerId()).isEqualTo(sellerId);
        assertThat(criteria.sellerName()).isNull();
        assertThat(criteria.active()).isNull();
        assertThat(criteria.fromCreatedAt()).isNull();
        assertThat(criteria.toCreatedAt()).isNull();
    }

    @Test
    @DisplayName("of() 정적 팩토리 메서드로 생성 - 활성 상태만 제공")
    void shouldCreateWithActiveOnly() {
        // given
        Boolean active = true;

        // when
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
                null, null, active, null, null
        );

        // then
        assertThat(criteria.active()).isEqualTo(active);
        assertThat(criteria.sellerId()).isNull();
        assertThat(criteria.sellerName()).isNull();
        assertThat(criteria.fromCreatedAt()).isNull();
        assertThat(criteria.toCreatedAt()).isNull();
    }

    @Test
    @DisplayName("of() 정적 팩토리 메서드로 생성 - 날짜 범위만 제공")
    void shouldCreateWithDateRangeOnly() {
        // given
        LocalDateTime fromCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime toCreatedAt = LocalDateTime.of(2025, 12, 31, 23, 59);

        // when
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
                null, null, null, fromCreatedAt, toCreatedAt
        );

        // then
        assertThat(criteria.fromCreatedAt()).isEqualTo(fromCreatedAt);
        assertThat(criteria.toCreatedAt()).isEqualTo(toCreatedAt);
        assertThat(criteria.sellerId()).isNull();
        assertThat(criteria.sellerName()).isNull();
        assertThat(criteria.active()).isNull();
    }

    @Test
    @DisplayName("of() 정적 팩토리 메서드로 생성 - 모든 필드 null")
    void shouldCreateWithAllNullFields() {
        // when
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
                null, null, null, null, null
        );

        // then
        assertThat(criteria.sellerId()).isNull();
        assertThat(criteria.sellerName()).isNull();
        assertThat(criteria.active()).isNull();
        assertThat(criteria.fromCreatedAt()).isNull();
        assertThat(criteria.toCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Record는 불변성 보장 - equals/hashCode 검증")
    void shouldBeImmutableRecord() {
        // given
        SellerSearchCriteria criteria1 = SellerSearchCriteria.of(
                "seller_1", "무신사", true, null, null
        );
        SellerSearchCriteria criteria2 = SellerSearchCriteria.of(
                "seller_1", "무신사", true, null, null
        );

        // then - 동일한 값으로 생성한 객체는 equals 및 hashCode 동일
        assertThat(criteria1).isEqualTo(criteria2);
        assertThat(criteria1.hashCode()).isEqualTo(criteria2.hashCode());
    }

    @Test
    @DisplayName("Record toString() 검증")
    void shouldHaveProperToString() {
        // given
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
                "seller_1", "무신사", true, null, null
        );

        // when
        String toString = criteria.toString();

        // then
        assertThat(toString).contains("seller_1");
        assertThat(toString).contains("무신사");
        assertThat(toString).contains("true");
    }
}
