package com.ryuqq.crawlinghub.application.dto.command;

import com.ryuqq.crawlinghub.application.fixture.RegisterSellerCommandFixture;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RegisterSellerCommand DTO 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ 유효한 데이터로 Command 생성</li>
 *   <li>✅ sellerId 빈 값 거부</li>
 *   <li>✅ crawlingIntervalDays 음수 거부</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class RegisterSellerCommandTest {

    @Test
    void shouldCreateCommandWithValidData() {
        // When
        RegisterSellerCommand command = RegisterSellerCommandFixture.aRegisterSellerCommand();

        // Then
        assertThat(command.sellerId()).isEqualTo("seller_12345");
        assertThat(command.name()).isEqualTo("무신사");
        assertThat(command.crawlingIntervalDays()).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidSellerId() {
        // Given
        String emptySellerId = "";
        String name = "무신사";
        Integer crawlingIntervalDays = 1;

        // When & Then
        assertThatThrownBy(() -> new RegisterSellerCommand(emptySellerId, name, crawlingIntervalDays))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("sellerId는 빈 값일 수 없습니다");
    }

    @Test
    void shouldRejectNullSellerId() {
        // Given
        String nullSellerId = null;
        String name = "무신사";
        Integer crawlingIntervalDays = 1;

        // When & Then
        assertThatThrownBy(() -> new RegisterSellerCommand(nullSellerId, name, crawlingIntervalDays))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("sellerId는 빈 값일 수 없습니다");
    }

    @Test
    void shouldRejectNegativeInterval() {
        // Given
        String sellerId = "seller_12345";
        String name = "무신사";
        Integer negativeInterval = -1;

        // When & Then
        assertThatThrownBy(() -> new RegisterSellerCommand(sellerId, name, negativeInterval))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("crawlingIntervalDays는 0보다 커야 합니다");
    }

    @Test
    void shouldRejectZeroInterval() {
        // Given
        String sellerId = "seller_12345";
        String name = "무신사";
        Integer zeroInterval = 0;

        // When & Then
        assertThatThrownBy(() -> new RegisterSellerCommand(sellerId, name, zeroInterval))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("crawlingIntervalDays는 0보다 커야 합니다");
    }
}
