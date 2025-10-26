package com.ryuqq.crawlinghub.application.mustit.seller.usecase;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RegisterMustitSellerCommand 단위 테스트
 * <p>
 * Java 21 Record의 Compact Constructor 검증을 포함합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@DisplayName("RegisterMustitSellerCommand 단위 테스트")
class RegisterMustitSellerCommandTest {

    @Test
    @DisplayName("유효한 파라미터로 Command를 생성할 수 있다")
    void createCommandWithValidParameters() {
        // given
        String sellerId = "SELLER001";
        String name = "Test Seller";
        CrawlIntervalType intervalType = CrawlIntervalType.DAILY;
        int intervalValue = 1;

        // when
        RegisterMustitSellerCommand command = new RegisterMustitSellerCommand(
                sellerId,
                name,
                intervalType,
                intervalValue
        );

        // then
        assertThat(command.sellerId()).isEqualTo(sellerId);
        assertThat(command.name()).isEqualTo(name);
        assertThat(command.intervalType()).isEqualTo(intervalType);
        assertThat(command.intervalValue()).isEqualTo(intervalValue);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("sellerId가 null이거나 빈 문자열이면 예외가 발생한다")
    void throwExceptionWhenSellerIdIsNullOrBlank(String invalidSellerId) {
        // when & then
        assertThatThrownBy(() -> new RegisterMustitSellerCommand(
                invalidSellerId,
                "Test Seller",
                CrawlIntervalType.DAILY,
                1
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId must not be null or blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("name이 null이거나 빈 문자열이면 예외가 발생한다")
    void throwExceptionWhenNameIsNullOrBlank(String invalidName) {
        // when & then
        assertThatThrownBy(() -> new RegisterMustitSellerCommand(
                "SELLER001",
                invalidName,
                CrawlIntervalType.DAILY,
                1
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be null or blank");
    }

    @Test
    @DisplayName("intervalType이 null이면 예외가 발생한다")
    void throwExceptionWhenIntervalTypeIsNull() {
        // when & then
        assertThatThrownBy(() -> new RegisterMustitSellerCommand(
                "SELLER001",
                "Test Seller",
                null,
                1
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("intervalType must not be null");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("intervalValue가 0 이하면 예외가 발생한다")
    void throwExceptionWhenIntervalValueIsZeroOrNegative(int invalidIntervalValue) {
        // when & then
        assertThatThrownBy(() -> new RegisterMustitSellerCommand(
                "SELLER001",
                "Test Seller",
                CrawlIntervalType.DAILY,
                invalidIntervalValue
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("intervalValue must be greater than 0");
    }

    @Test
    @DisplayName("동일한 파라미터로 생성된 Command는 동등하다")
    void equalsWithSameParameters() {
        // given
        RegisterMustitSellerCommand command1 = new RegisterMustitSellerCommand(
                "SELLER001",
                "Test Seller",
                CrawlIntervalType.DAILY,
                1
        );
        RegisterMustitSellerCommand command2 = new RegisterMustitSellerCommand(
                "SELLER001",
                "Test Seller",
                CrawlIntervalType.DAILY,
                1
        );

        // when & then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    @DisplayName("다른 파라미터로 생성된 Command는 동등하지 않다")
    void notEqualsWithDifferentParameters() {
        // given
        RegisterMustitSellerCommand command1 = new RegisterMustitSellerCommand(
                "SELLER001",
                "Test Seller",
                CrawlIntervalType.DAILY,
                1
        );
        RegisterMustitSellerCommand command2 = new RegisterMustitSellerCommand(
                "SELLER002",
                "Test Seller",
                CrawlIntervalType.DAILY,
                1
        );

        // when & then
        assertThat(command1).isNotEqualTo(command2);
    }

    @Test
    @DisplayName("toString()은 모든 필드 정보를 포함한다")
    void toStringContainsAllFields() {
        // given
        RegisterMustitSellerCommand command = new RegisterMustitSellerCommand(
                "SELLER001",
                "Test Seller",
                CrawlIntervalType.HOURLY,
                6
        );

        // when
        String result = command.toString();

        // then
        assertThat(result)
                .contains("SELLER001")
                .contains("Test Seller")
                .contains("HOURLY")
                .contains("6");
    }
}
