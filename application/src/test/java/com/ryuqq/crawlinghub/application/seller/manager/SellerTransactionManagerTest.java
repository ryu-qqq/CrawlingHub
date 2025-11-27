package com.ryuqq.crawlinghub.application.seller.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SellerTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerTransactionManager 테스트")
class SellerTransactionManagerTest {

    @Mock
    private SellerPersistencePort sellerPersistencePort;

    @InjectMocks
    private SellerTransactionManager manager;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] Seller 저장 → SellerId 반환")
        void shouldPersistSellerAndReturnId() {
            // Given
            Seller seller = SellerFixture.anActiveSeller();
            SellerId expectedId = SellerIdFixture.anAssignedId();

            given(sellerPersistencePort.persist(seller)).willReturn(expectedId);

            // When
            SellerId result = manager.persist(seller);

            // Then
            assertThat(result).isEqualTo(expectedId);
            verify(sellerPersistencePort).persist(seller);
        }

        @Test
        @DisplayName("[성공] 비활성 Seller 저장")
        void shouldPersistInactiveSeller() {
            // Given
            Seller seller = SellerFixture.anInactiveSeller();
            SellerId expectedId = SellerIdFixture.anAssignedId();

            given(sellerPersistencePort.persist(seller)).willReturn(expectedId);

            // When
            SellerId result = manager.persist(seller);

            // Then
            assertThat(result).isEqualTo(expectedId);
        }
    }
}
