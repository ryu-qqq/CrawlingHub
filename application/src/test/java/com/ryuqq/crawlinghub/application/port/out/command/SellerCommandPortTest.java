package com.ryuqq.crawlinghub.application.port.out.command;

import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * SellerCommandPort 인터페이스 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Port 메서드 시그니처 존재 확인</li>
 *   <li>✅ save() 메서드 계약 검증</li>
 *   <li>✅ delete() 메서드 계약 검증</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Command Port 명명 규칙: Save*, Delete*</li>
 *   <li>✅ Infrastructure Layer 의존 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class SellerCommandPortTest {

    @Test
    void shouldHaveSaveMethod() {
        // Given
        SellerCommandPort port = mock(SellerCommandPort.class);
        Seller seller = SellerFixture.forNew();

        given(port.save(any(Seller.class))).willReturn(seller);

        // When
        Seller result = port.save(seller);

        // Then
        assertThat(result).isNotNull();
        verify(port).save(seller);
    }

    @Test
    void shouldHaveDeleteMethod() {
        // Given
        SellerCommandPort port = mock(SellerCommandPort.class);
        String sellerId = "seller_12345";

        // When
        port.delete(sellerId);

        // Then
        verify(port).delete(sellerId);
    }

    @Test
    void shouldReturnSavedSeller() {
        // Given
        SellerCommandPort port = mock(SellerCommandPort.class);
        Seller seller = SellerFixture.forNew();

        given(port.save(seller)).willReturn(seller);

        // When
        Seller savedSeller = port.save(seller);

        // Then
        assertThat(savedSeller).isEqualTo(seller);
        assertThat(savedSeller.getSellerId()).isEqualTo(seller.getSellerId());
    }
}
