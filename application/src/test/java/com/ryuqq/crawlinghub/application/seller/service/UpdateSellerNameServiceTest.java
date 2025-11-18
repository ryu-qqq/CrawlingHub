package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.fixture.UpdateSellerNameCommandFixture;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;
import com.ryuqq.crawlinghub.application.seller.manager.SellerManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSellerNameService 테스트")
class UpdateSellerNameServiceTest {

    @Mock
    private SellerManager sellerManager;

    @InjectMocks
    private UpdateSellerNameService updateSellerNameService;

    @Test
    @DisplayName("Seller 이름 변경 성공")
    void shouldUpdateSellerNameSuccessfully() {
        // Given
        UpdateSellerNameCommand command = UpdateSellerNameCommandFixture.anUpdateSellerNameCommand();

        // When
        updateSellerNameService.execute(command);

        // Then
        then(sellerManager).should().updateName(command.sellerId(), command.newName());
    }
}
