package com.ryuqq.crawlinghub.application.seller.dto.command;

import com.ryuqq.crawlinghub.application.fixture.RegisterSellerCommandFixture;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RegisterSellerCommand DTO 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ 데이터 생성 및 접근</li>
 *   <li>✅ Record 불변성 검증</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ DTO는 비즈니스 로직 없음 (순수 데이터 전달)</li>
 *   <li>✅ Validation 테스트는 UseCase 또는 REST API Layer에서</li>
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
    void shouldAccessAllFields() {
        // Given
        String sellerId = "seller_99999";
        String name = "테스트셀러";
        Integer crawlingIntervalDays = 7;

        // When
        RegisterSellerCommand command = new RegisterSellerCommand(sellerId, name, crawlingIntervalDays);

        // Then
        assertThat(command.sellerId()).isEqualTo(sellerId);
        assertThat(command.name()).isEqualTo(name);
        assertThat(command.crawlingIntervalDays()).isEqualTo(crawlingIntervalDays);
    }

    @Test
    void shouldSupportRecordEquality() {
        // Given
        RegisterSellerCommand command1 = new RegisterSellerCommand("seller_123", "무신사", 1);
        RegisterSellerCommand command2 = new RegisterSellerCommand("seller_123", "무신사", 1);

        // Then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }
}
