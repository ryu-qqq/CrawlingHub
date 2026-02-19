package com.ryuqq.crawlinghub.application.seller.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UpdateSellerProductCountService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSellerProductCountService 테스트")
class UpdateSellerProductCountServiceTest {

    @Mock private SellerTransactionManager sellerTransactionManager;

    @Mock private SellerReadManager sellerReadManager;

    @Mock private TimeProvider timeProvider;

    @InjectMocks private UpdateSellerProductCountService service;

    @BeforeEach
    void setUp() {
        java.time.Instant fixedInstant = FixedClock.aDefaultClock().instant();
        org.mockito.Mockito.lenient().when(timeProvider.now()).thenReturn(fixedInstant);
    }

    @Nested
    @DisplayName("execute() 상품 수 업데이트 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 존재하는 셀러의 상품 수 업데이트")
        void shouldUpdateProductCountWhenSellerExists() {
            // Given
            Long sellerId = 1L;
            int productCount = 100;
            Seller seller = SellerFixture.anActiveSeller();

            given(sellerReadManager.findById(any(SellerId.class))).willReturn(Optional.of(seller));
            given(sellerTransactionManager.persist(seller)).willReturn(SellerId.of(sellerId));

            // When
            service.execute(sellerId, productCount);

            // Then
            then(sellerReadManager).should().findById(SellerId.of(sellerId));
            then(sellerTransactionManager).should().persist(seller);
        }

        @Test
        @DisplayName("[성공] 상품 수가 0인 경우도 정상 업데이트")
        void shouldUpdateProductCountToZero() {
            // Given
            Long sellerId = 1L;
            int productCount = 0;
            Seller seller = SellerFixture.anActiveSellerWithProducts(50);

            given(sellerReadManager.findById(any(SellerId.class))).willReturn(Optional.of(seller));
            given(sellerTransactionManager.persist(seller)).willReturn(SellerId.of(sellerId));

            // When
            service.execute(sellerId, productCount);

            // Then
            then(sellerReadManager).should().findById(SellerId.of(sellerId));
            then(sellerTransactionManager).should().persist(seller);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 셀러의 상품 수 업데이트 시 SellerNotFoundException 발생")
        void shouldThrowExceptionWhenSellerNotFound() {
            // Given
            Long sellerId = 999L;
            int productCount = 100;

            given(sellerReadManager.findById(any(SellerId.class))).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(sellerId, productCount))
                    .isInstanceOf(SellerNotFoundException.class);

            then(sellerTransactionManager).should(never()).persist(any());
        }
    }
}
