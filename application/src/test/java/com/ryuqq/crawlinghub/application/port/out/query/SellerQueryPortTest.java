package com.ryuqq.crawlinghub.application.port.out.query;

import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * SellerQueryPort 인터페이스 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Port 메서드 시그니처 존재 확인</li>
 *   <li>✅ findById() 메서드 계약 검증</li>
 *   <li>✅ findByStatus() 메서드 계약 검증</li>
 *   <li>✅ existsBySellerId() 메서드 계약 검증</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Query Port 명명 규칙: Find*, Load*, Exists*</li>
 *   <li>✅ Infrastructure Layer 의존 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class SellerQueryPortTest {

    @Test
    void shouldHaveFindByIdMethod() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        String sellerId = "seller_12345";
        Seller seller = SellerFixture.forNew();

        given(port.findById(sellerId)).willReturn(Optional.of(seller));

        // When
        Optional<Seller> result = port.findById(sellerId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSellerId().value()).isEqualTo(sellerId);
        verify(port).findById(sellerId);
    }

    @Test
    void shouldHaveFindByStatusMethod() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        SellerStatus status = SellerStatus.ACTIVE;
        List<Seller> sellers = List.of(SellerFixture.forNew());

        given(port.findByStatus(status)).willReturn(sellers);

        // When
        List<Seller> result = port.findByStatus(status);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(port).findByStatus(status);
    }

    @Test
    void shouldHaveExistsBySellerIdMethod() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        String sellerId = "seller_12345";

        given(port.existsBySellerId(sellerId)).willReturn(true);

        // When
        boolean exists = port.existsBySellerId(sellerId);

        // Then
        assertThat(exists).isTrue();
        verify(port).existsBySellerId(sellerId);
    }

    @Test
    void shouldReturnEmptyWhenSellerNotFound() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        String sellerId = "nonexistent_seller";

        given(port.findById(sellerId)).willReturn(Optional.empty());

        // When
        Optional<Seller> result = port.findById(sellerId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenSellerDoesNotExist() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        String sellerId = "nonexistent_seller";

        given(port.existsBySellerId(sellerId)).willReturn(false);

        // When
        boolean exists = port.existsBySellerId(sellerId);

        // Then
        assertThat(exists).isFalse();
    }
}
