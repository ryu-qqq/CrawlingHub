package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.fixture.UpdateSellerNameCommandFixture;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerManager;
import com.ryuqq.crawlinghub.application.seller.port.out.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSellerNameService 테스트")
class UpdateSellerNameServiceTest {

    @Mock
    private SellerManager sellerManager;

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SellerAssembler sellerAssembler;

    @InjectMocks
    private UpdateSellerNameService updateSellerNameService;

    @Test
    @DisplayName("Seller 이름 변경 성공")
    void shouldUpdateSellerNameSuccessfully() {
        // Given
        UpdateSellerNameCommand command = UpdateSellerNameCommandFixture.anUpdateSellerNameCommand();
        Seller seller = Seller.of(SellerId.of(command.sellerId()), "Old Name");
        SellerResponse expectedResponse = SellerResponse.of(command.sellerId(), command.newName(), SellerStatus.INACTIVE);

        given(sellerQueryPort.findBySellerId(SellerId.of(command.sellerId())))
                .willReturn(Optional.of(seller));
        given(sellerAssembler.toResponse(seller))
                .willReturn(expectedResponse);

        // When
        SellerResponse response = updateSellerNameService.execute(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(command.newName());
        then(sellerQueryPort).should().findBySellerId(SellerId.of(command.sellerId()));
        then(sellerManager).should().save(seller);
        then(sellerAssembler).should().toResponse(seller);
    }

    @Test
    @DisplayName("존재하지 않는 Seller ID로 변경 시 SellerNotFoundException 발생")
    void shouldThrowExceptionWhenSellerNotFound() {
        // Given
        UpdateSellerNameCommand command = UpdateSellerNameCommandFixture.anUpdateSellerNameCommand();

        given(sellerQueryPort.findBySellerId(SellerId.of(command.sellerId())))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> updateSellerNameService.execute(command))
                .isInstanceOf(SellerNotFoundException.class)
                .hasMessage("Seller를 찾을 수 없습니다: " + command.sellerId());

        then(sellerManager).should(never()).save(any());
        then(sellerAssembler).should(never()).toResponse(any());
    }
}
