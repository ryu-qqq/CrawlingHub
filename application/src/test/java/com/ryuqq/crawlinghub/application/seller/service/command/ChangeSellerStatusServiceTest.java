package com.ryuqq.crawlinghub.application.seller.service.command;

import com.ryuqq.crawlinghub.application.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.fixture.seller.SellerCommandFixture;
import com.ryuqq.crawlinghub.application.fixture.seller.response.SellerResponseFixture;
import com.ryuqq.crawlinghub.application.seller.dto.command.ChangeSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.ChangeSellerStatusUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerHasActiveSchedulersException;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeSellerStatusService")
class ChangeSellerStatusServiceTest {

    @Mock
    private SellerTransactionManager transactionManager;

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SchedulerQueryPort schedulerQueryPort;

    @Mock
    private SellerAssembler sellerAssembler;

    @InjectMocks
    private ChangeSellerStatusService service;

    @Test
    @DisplayName("ACTIVE -> INACTIVE 전환 시 활성 스케줄이 없으면 비활성화한다")
    void shouldDeactivateSellerWhenNoActiveSchedulers() {
        ChangeSellerStatusCommand command = new ChangeSellerStatusCommand(1L, SellerStatus.INACTIVE);
        Seller seller = SellerFixture.anActiveSeller();
        SellerResponse expected = SellerResponseFixture.sample();

        given(sellerQueryPort.findById(SellerId.of(command.sellerId())))
            .willReturn(Optional.of(seller));
        given(schedulerQueryPort.countActiveSchedulersBySellerId(command.sellerId()))
            .willReturn(0);
        given(transactionManager.persist(any(Seller.class)))
            .willReturn(seller);
        given(sellerAssembler.toSellerResponse(any(Seller.class)))
            .willReturn(expected);

        SellerResponse response = service.changeStatus(command);

        assertThat(response).isEqualTo(expected);
        verify(schedulerQueryPort).countActiveSchedulersBySellerId(command.sellerId());
        verify(transactionManager).persist(seller);
    }

    @Test
    @DisplayName("활성 스케줄이 있으면 비활성화 시 예외를 던진다")
    void shouldThrowExceptionWhenActiveSchedulersExist() {
        ChangeSellerStatusCommand command = new ChangeSellerStatusCommand(1L, SellerStatus.INACTIVE);
        Seller seller = SellerFixture.anActiveSeller();

        given(sellerQueryPort.findById(SellerId.of(command.sellerId())))
            .willReturn(Optional.of(seller));
        given(schedulerQueryPort.countActiveSchedulersBySellerId(command.sellerId()))
            .willReturn(2);

        assertThatThrownBy(() -> service.changeStatus(command))
            .isInstanceOf(SellerHasActiveSchedulersException.class);
    }

    @Test
    @DisplayName("INACTIVE -> ACTIVE 전환 시 셀러를 활성화한다")
    void shouldActivateInactiveSeller() {
        ChangeSellerStatusCommand command = new ChangeSellerStatusCommand(1L, SellerStatus.ACTIVE);
        Seller seller = SellerFixture.anInactiveSeller();
        SellerResponse expected = SellerResponseFixture.sample();

        given(sellerQueryPort.findById(SellerId.of(command.sellerId())))
            .willReturn(Optional.of(seller));
        given(transactionManager.persist(any(Seller.class)))
            .willReturn(seller);
        given(sellerAssembler.toSellerResponse(any(Seller.class)))
            .willReturn(expected);

        SellerResponse response = service.changeStatus(command);

        assertThat(response).isEqualTo(expected);
        verify(schedulerQueryPort, times(0)).countActiveSchedulersBySellerId(command.sellerId());
        verify(transactionManager).persist(seller);
    }
}

