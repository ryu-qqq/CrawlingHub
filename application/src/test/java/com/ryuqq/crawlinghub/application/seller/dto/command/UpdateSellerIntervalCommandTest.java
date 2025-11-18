package com.ryuqq.crawlinghub.application.seller.dto.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UpdateSellerIntervalCommand DTO 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Record 타입 검증</li>
 *   <li>✅ 필드 존재 확인 (sellerId, newIntervalDays)</li>
 *   <li>✅ 불변성 검증</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Java 21 Record 사용</li>
 *   <li>✅ 비즈니스 메서드 금지 - 순수 데이터 전달 객체</li>
 *   <li>✅ Validation 금지 - REST API Layer 또는 UseCase에서 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@DisplayName("UpdateSellerIntervalCommand DTO 테스트")
class UpdateSellerIntervalCommandTest {

    @Test
    @DisplayName("유효한 데이터로 Command를 생성하면 성공해야 한다")
    void shouldCreateCommandWithValidData() {
        // Given
        String sellerId = "seller_12345";
        Integer newIntervalDays = 7;

        // When
        UpdateSellerIntervalCommand command = new UpdateSellerIntervalCommand(
            sellerId,
            newIntervalDays
        );

        // Then
        assertThat(command).isNotNull();
        assertThat(command.sellerId()).isEqualTo(sellerId);
        assertThat(command.newIntervalDays()).isEqualTo(newIntervalDays);
    }

    @Test
    @DisplayName("Record 타입이어야 한다")
    void shouldBeRecordType() {
        // When & Then
        assertThat(UpdateSellerIntervalCommand.class.isRecord()).isTrue();
    }

    @Test
    @DisplayName("불변 객체여야 한다 (동일한 값으로 생성하면 같아야 함)")
    void shouldBeImmutable() {
        // Given
        String sellerId = "seller_12345";
        Integer newIntervalDays = 7;

        // When
        UpdateSellerIntervalCommand command1 = new UpdateSellerIntervalCommand(sellerId, newIntervalDays);
        UpdateSellerIntervalCommand command2 = new UpdateSellerIntervalCommand(sellerId, newIntervalDays);

        // Then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }
}
