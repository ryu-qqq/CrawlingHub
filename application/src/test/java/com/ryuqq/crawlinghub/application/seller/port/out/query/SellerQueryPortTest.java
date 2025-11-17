package com.ryuqq.crawlinghub.application.seller.port.out.query;

import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
 *   <li>✅ existsById() 메서드 계약 검증</li>
 *   <li>✅ findByCriteria() 메서드 계약 검증 (SellerSearchCriteria VO)</li>
 *   <li>✅ countByCriteria() 메서드 계약 검증 (SellerSearchCriteria VO)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Query Port 명명 규칙: Find*, Exists*, Count*</li>
 *   <li>✅ 필수 메서드: findById, existsById, findByCriteria, countByCriteria</li>
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
        Seller seller = SellerFixture.forNew();
        SellerId sellerId = seller.getSellerId();

        given(port.findById(sellerId)).willReturn(Optional.of(seller));

        // When
        Optional<Seller> result = port.findById(sellerId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSellerId()).isEqualTo(sellerId);
        verify(port).findById(sellerId);
    }

    @Test
    void shouldHaveExistsByIdMethod() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        SellerId sellerId = new SellerId(12345L);

        given(port.existsById(sellerId)).willReturn(true);

        // When
        boolean exists = port.existsById(sellerId);

        // Then
        assertThat(exists).isTrue();
        verify(port).existsById(sellerId);
    }

    @Test
    void shouldHaveFindByCriteriaMethod() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        SellerSearchCriteria criteria = SellerSearchCriteria.onlyActive();
        List<Seller> sellers = List.of(SellerFixture.forNew());

        given(port.findByCriteria(any(SellerSearchCriteria.class))).willReturn(sellers);

        // When
        List<Seller> result = port.findByCriteria(criteria);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(port).findByCriteria(criteria);
    }

    @Test
    void shouldHaveCountByCriteriaMethod() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        SellerSearchCriteria criteria = SellerSearchCriteria.onlyActive();

        given(port.countByCriteria(any(SellerSearchCriteria.class))).willReturn(10L);

        // When
        long count = port.countByCriteria(criteria);

        // Then
        assertThat(count).isEqualTo(10L);
        verify(port).countByCriteria(criteria);
    }

    @Test
    void shouldReturnEmptyWhenSellerNotFound() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        SellerId sellerId = new SellerId(99999L);

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
        SellerId sellerId = new SellerId(99999L);

        given(port.existsById(sellerId)).willReturn(false);

        // When
        boolean exists = port.existsById(sellerId);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnEmptyListWhenNoCriteriaMatch() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        SellerSearchCriteria criteria = SellerSearchCriteria.bySellerId("nonexistent");

        given(port.findByCriteria(any(SellerSearchCriteria.class))).willReturn(List.of());

        // When
        List<Seller> result = port.findByCriteria(criteria);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnZeroWhenNoCriteriaMatch() {
        // Given
        SellerQueryPort port = mock(SellerQueryPort.class);
        SellerSearchCriteria criteria = SellerSearchCriteria.bySellerId("nonexistent");

        given(port.countByCriteria(any(SellerSearchCriteria.class))).willReturn(0L);

        // When
        long count = port.countByCriteria(criteria);

        // Then
        assertThat(count).isZero();
    }
}
