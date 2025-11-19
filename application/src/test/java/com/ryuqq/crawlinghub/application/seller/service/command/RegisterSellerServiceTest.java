package com.ryuqq.crawlinghub.application.seller.service.command;

import java.util.List;

import com.ryuqq.crawlinghub.application.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.fixture.seller.SellerCommandFixture;
import com.ryuqq.crawlinghub.application.fixture.seller.response.SellerResponseFixture;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryCriteria;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerService")
class RegisterSellerServiceTest {

    @Mock
    private SellerTransactionManager transactionManager;

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SellerAssembler sellerAssembler;

    @InjectMocks
    private RegisterSellerService service;

    @Captor
    private ArgumentCaptor<SellerQueryCriteria> criteriaCaptor;

    @Test
    @DisplayName("중복이 없으면 셀러를 생성해 저장하고 응답을 반환한다")
    void shouldRegisterSellerSuccessfully() {
        RegisterSellerCommand command = SellerCommandFixture.registerSeller();
        Seller persistedSeller = SellerFixture.anActiveSeller();
        SellerResponse expectedResponse = SellerResponseFixture.sample();

        given(sellerQueryPort.findByCriteria(any(SellerQueryCriteria.class)))
            .willReturn(List.of());
        given(transactionManager.persist(any(Seller.class)))
            .willReturn(persistedSeller);
        given(sellerAssembler.toSellerResponse(any(Seller.class)))
            .willReturn(expectedResponse);

        SellerResponse response = service.register(command);

        assertThat(response).isEqualTo(expectedResponse);
        verify(transactionManager).persist(any(Seller.class));
        verify(sellerAssembler).toSellerResponse(any(Seller.class));
    }

    @Test
    @DisplayName("머스트잇 셀러 ID가 중복되면 DuplicateMustItSellerIdException을 던진다")
    void shouldThrowExceptionWhenDuplicateMustItSellerId() {
        RegisterSellerCommand command = SellerCommandFixture.registerSeller();
        given(sellerQueryPort.findByCriteria(any(SellerQueryCriteria.class)))
            .willReturn(List.of(SellerFixture.anActiveSeller()));

        assertThatThrownBy(() -> service.register(command))
            .isInstanceOf(DuplicateMustItSellerIdException.class);
    }

    @Test
    @DisplayName("셀러명이 중복되면 DuplicateSellerNameException을 던진다")
    void shouldThrowExceptionWhenDuplicateSellerName() {
        RegisterSellerCommand command = SellerCommandFixture.registerSeller();
        given(sellerQueryPort.findByCriteria(any(SellerQueryCriteria.class)))
            .willReturn(List.of(), List.of(SellerFixture.anActiveSeller()));

        assertThatThrownBy(() -> service.register(command))
            .isInstanceOf(DuplicateSellerNameException.class);
    }
}

