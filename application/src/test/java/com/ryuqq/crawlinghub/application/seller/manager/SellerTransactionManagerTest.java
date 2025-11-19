package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerTransactionManager")
class SellerTransactionManagerTest {

    @Mock
    private SellerPersistencePort persistencePort;

    @InjectMocks
    private SellerTransactionManager transactionManager;

    @Test
    @DisplayName("Seller를 저장하고 저장된 Seller를 반환한다")
    void shouldPersistAndReturnSeller() {
        // Given
        Seller seller = SellerFixture.of();

        // When
        Seller result = transactionManager.persist(seller);

        // Then
        assertThat(result).isSameAs(seller);
        verify(persistencePort).persist(seller);
    }
}

