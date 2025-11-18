package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.seller.port.out.SellerPersistencePort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerManager 테스트")
class SellerManagerTest {

    @Mock
    private SellerPersistencePort persistencePort;

    @InjectMocks
    private SellerManager sellerManager;

    @Test
    @DisplayName("Seller 저장 성공")
    void shouldSaveSellerSuccessfully() {
        // Given
        Seller seller = Seller.forNew(SellerId.forNew(), "Test Seller");
        SellerId expectedId = SellerId.of(1L);

        given(persistencePort.persist(any(Seller.class)))
                .willReturn(expectedId);

        // When
        SellerId savedId = sellerManager.save(seller);

        // Then
        assertThat(savedId).isNotNull();
        assertThat(savedId.value()).isEqualTo(1L);
        then(persistencePort).should().persist(seller);
    }
}
