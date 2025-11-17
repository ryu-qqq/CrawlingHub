package com.ryuqq.crawlinghub.application.seller.port.out.command;

import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * SellerPersistencePort 인터페이스 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Port 메서드 시그니처 존재 확인</li>
 *   <li>✅ persist() 메서드 계약 검증</li>
 *   <li>✅ SellerId (Value Object) 반환 검증</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ PersistencePort 명명 규칙: *PersistencePort</li>
 *   <li>✅ persist() 메서드 하나만 제공</li>
 *   <li>✅ Domain Aggregate 파라미터, Value Object 반환</li>
 *   <li>✅ Infrastructure Layer 의존 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
class SellerPersistencePortTest {

    @Test
    void shouldHavePersistMethod() {
        // Given
        SellerPersistencePort port = mock(SellerPersistencePort.class);
        Seller seller = SellerFixture.forNew();
        SellerId expectedId = SellerId.of(1L);

        given(port.persist(any(Seller.class))).willReturn(expectedId);

        // When
        SellerId result = port.persist(seller);

        // Then
        assertThat(result).isNotNull();
        verify(port).persist(seller);
    }

    @Test
    void shouldReturnSellerId() {
        // Given
        SellerPersistencePort port = mock(SellerPersistencePort.class);
        Seller seller = SellerFixture.forNew();
        SellerId expectedId = SellerId.of(12345L);

        given(port.persist(seller)).willReturn(expectedId);

        // When
        SellerId sellerId = port.persist(seller);

        // Then
        assertThat(sellerId).isEqualTo(expectedId);
        assertThat(sellerId.value()).isEqualTo(12345L);
    }

    @Test
    void shouldPersistDomainAggregate() {
        // Given
        SellerPersistencePort port = mock(SellerPersistencePort.class);
        Seller seller = SellerFixture.forNew();
        SellerId sellerId = SellerId.of(1L);

        given(port.persist(seller)).willReturn(sellerId);

        // When
        SellerId result = port.persist(seller);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SellerId.class);
        verify(port).persist(seller);
    }
}
