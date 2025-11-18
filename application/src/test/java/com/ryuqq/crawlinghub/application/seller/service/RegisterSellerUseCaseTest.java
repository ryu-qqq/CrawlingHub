package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerCommandPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerDuplicatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerUseCase 테스트")
class RegisterSellerUseCaseTest {

    @Mock
    private SellerCommandPort sellerCommandPort;

    @Mock
    private SellerQueryPort sellerQueryPort;

    @InjectMocks
    private RegisterSellerUseCase registerSellerUseCase;

    private RegisterSellerCommand command;

    @BeforeEach
    void setUp() {
        command = new RegisterSellerCommand("무신사");
    }

    @Test
    @DisplayName("Seller 등록 성공")
    void shouldRegisterSellerSuccessfully() {
        // Given
        given(sellerQueryPort.existsByName(command.name()))
                .willReturn(false);

        // When
        registerSellerUseCase.execute(command);

        // Then
        then(sellerQueryPort).should().existsByName(command.name());
        then(sellerCommandPort).should().save(any(Seller.class));
    }

    @Test
    @DisplayName("중복된 Seller 이름으로 등록 시 SellerDuplicatedException 발생")
    void shouldThrowExceptionWhenDuplicateSellerId() {
        // Given
        given(sellerQueryPort.existsByName(command.name()))
                .willReturn(true);

        // When & Then
        assertThatThrownBy(() -> registerSellerUseCase.execute(command))
                .isInstanceOf(SellerDuplicatedException.class)
                .hasMessage("이미 존재하는 Seller 이름입니다: " + command.name());

        then(sellerCommandPort).should(never()).save(any(Seller.class));
    }
}
