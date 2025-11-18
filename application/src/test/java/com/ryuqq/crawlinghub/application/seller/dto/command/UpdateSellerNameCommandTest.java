package com.ryuqq.crawlinghub.application.seller.dto.command;

import com.ryuqq.crawlinghub.application.fixture.UpdateSellerNameCommandFixture;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UpdateSellerNameCommand DTO 테스트
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
class UpdateSellerNameCommandTest {

    @Test
    void shouldCreateCommandWithValidData() {
        // When
        UpdateSellerNameCommand command = UpdateSellerNameCommandFixture.anUpdateSellerNameCommand();

        // Then
        assertThat(command.sellerId()).isEqualTo("seller_12345");
        assertThat(command.newName()).isEqualTo("무신사 업데이트");
    }

    @Test
    void shouldAccessAllFields() {
        // Given
        String sellerId = "seller_99999";
        String newName = "테스트셀러";

        // When
        UpdateSellerNameCommand command = new UpdateSellerNameCommand(sellerId, newName);

        // Then
        assertThat(command.sellerId()).isEqualTo(sellerId);
        assertThat(command.newName()).isEqualTo(newName);
    }

    @Test
    void shouldSupportRecordEquality() {
        // Given
        UpdateSellerNameCommand command1 = new UpdateSellerNameCommand("seller_123", "무신사");
        UpdateSellerNameCommand command2 = new UpdateSellerNameCommand("seller_123", "무신사");

        // Then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }
}

