package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.fixture.RegisterSellerCommandFixture;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerManager;
import com.ryuqq.crawlinghub.application.seller.port.out.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerDuplicatedException;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerService 테스트")
class RegisterSellerServiceTest {

    @Mock
    private SellerManager sellerManager;

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SellerAssembler sellerAssembler;

    @InjectMocks
    private RegisterSellerService registerSellerService;

    private RegisterSellerCommand command;

    @BeforeEach
    void setUp() {
        command = RegisterSellerCommandFixture.aRegisterSellerCommand();
    }

    @Test
    @DisplayName("Seller 등록 성공")
    void shouldRegisterSellerSuccessfully() {
        // Given
        Seller seller = Seller.forNew(SellerId.forNew(), command.name());
        SellerResponse expectedResponse = SellerResponse.of(1L, command.name(), SellerStatus.INACTIVE);

        given(sellerQueryPort.existsByName(command.name()))
                .willReturn(false);
        given(sellerAssembler.toDomain(command))
                .willReturn(seller);
        given(sellerAssembler.toResponse(seller))
                .willReturn(expectedResponse);

        // When
        SellerResponse response = registerSellerService.execute(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(command.name());
        then(sellerQueryPort).should().existsByName(command.name());
        then(sellerManager).should().save(any());
        then(sellerAssembler).should().toDomain(command);
        then(sellerAssembler).should().toResponse(seller);
    }

    @Test
    @DisplayName("중복된 Seller 이름으로 등록 시 SellerDuplicatedException 발생")
    void shouldThrowExceptionWhenDuplicateSellerId() {
        // Given
        given(sellerQueryPort.existsByName(command.name()))
                .willReturn(true);

        // When & Then
        assertThatThrownBy(() -> registerSellerService.execute(command))
                .isInstanceOf(SellerDuplicatedException.class)
                .hasMessage("이미 존재하는 Seller 이름입니다: " + command.name());

        then(sellerManager).should(never()).save(any());
        then(sellerAssembler).should(never()).toDomain(any());
    }
}
