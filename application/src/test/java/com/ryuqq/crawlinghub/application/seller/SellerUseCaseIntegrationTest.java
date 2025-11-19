package com.ryuqq.crawlinghub.application.seller;

import com.ryuqq.crawlinghub.application.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.fixture.seller.SellerCommandFixture;
import com.ryuqq.crawlinghub.application.fixture.seller.SellerQueryFixture;
import com.ryuqq.crawlinghub.application.seller.dto.command.ChangeSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.query.ListSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.command.ChangeSellerStatusUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.query.GetSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.query.ListSellersUseCase;
import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;

/**
 * Seller UseCase 통합 테스트.
 *
 * <p>여러 UseCase를 함께 사용하는 시나리오를 테스트합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Seller UseCase 통합 테스트")
class SellerUseCaseIntegrationTest {

    @Mock
    private SellerTransactionManager transactionManager;

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SellerPersistencePort sellerPersistencePort;

    @Mock
    private SchedulerQueryPort schedulerQueryPort;

    @Mock
    private SellerAssembler sellerAssembler;

    private RegisterSellerUseCase registerSellerUseCase;
    private GetSellerUseCase getSellerUseCase;
    private ChangeSellerStatusUseCase changeSellerStatusUseCase;
    private ListSellersUseCase listSellersUseCase;

    @BeforeEach
    void setUp() {
        registerSellerUseCase = new com.ryuqq.crawlinghub.application.seller.service.command.RegisterSellerService(
            transactionManager,
            sellerQueryPort,
            sellerAssembler
        );
        getSellerUseCase = new com.ryuqq.crawlinghub.application.seller.service.query.GetSellerService(
            sellerQueryPort,
            schedulerQueryPort,
            sellerAssembler
        );
        changeSellerStatusUseCase = new com.ryuqq.crawlinghub.application.seller.service.command.ChangeSellerStatusService(
            transactionManager,
            sellerQueryPort,
            schedulerQueryPort,
            sellerAssembler
        );
        listSellersUseCase = new com.ryuqq.crawlinghub.application.seller.service.query.ListSellersService(
            sellerQueryPort,
            schedulerQueryPort,
            sellerAssembler
        );

        reset(transactionManager, sellerQueryPort, sellerPersistencePort, schedulerQueryPort, sellerAssembler);
    }

    @Test
    @DisplayName("RegisterSeller → GetSeller → ChangeStatus → ListSellers 시나리오를 통과한다")
    void shouldCompleteFullUseCaseScenario() {
        // Given: RegisterSeller
        RegisterSellerCommand registerCommand = SellerCommandFixture.registerSeller();
        Seller registeredSeller = SellerFixture.anActiveSeller();
        SellerResponse registerResponse = new SellerResponse(
            registeredSeller.getSellerId().value(),
            registeredSeller.getMustItSellerId().value() != null
                ? String.valueOf(registeredSeller.getMustItSellerId().value())
                : null,
            registeredSeller.getSellerName(),
            registeredSeller.getStatus(),
            registeredSeller.getCreatedAt()
        );

        given(sellerQueryPort.findByCriteria(any()))
            .willReturn(List.of());
        given(transactionManager.persist(any(Seller.class)))
            .willReturn(registeredSeller);
        given(sellerAssembler.toSellerResponse(any(Seller.class)))
            .willReturn(registerResponse);

        // When: RegisterSeller 실행
        SellerResponse registered = registerSellerUseCase.register(registerCommand);

        // Then: 등록 성공
        assertThat(registered).isNotNull();
        assertThat(registered.sellerId()).isNotNull();
        Long sellerId = registered.sellerId();

        // Given: GetSeller
        GetSellerQuery getQuery = new GetSellerQuery(sellerId);
        SellerDetailResponse detailResponse = new SellerDetailResponse(
            sellerId,
            String.valueOf(registeredSeller.getMustItSellerId().value()),
            registeredSeller.getSellerName(),
            registeredSeller.getStatus(),
            0,
            0,
            registeredSeller.getCreatedAt(),
            registeredSeller.getUpdatedAt()
        );

        given(sellerQueryPort.findById(SellerId.of(sellerId)))
            .willReturn(Optional.of(registeredSeller));
        given(schedulerQueryPort.countActiveSchedulersBySellerId(sellerId))
            .willReturn(0);
        given(schedulerQueryPort.countTotalSchedulersBySellerId(sellerId))
            .willReturn(0);
        given(sellerAssembler.toSellerDetailResponse(registeredSeller, 0, 0))
            .willReturn(detailResponse);

        // When: GetSeller 실행
        SellerDetailResponse retrieved = getSellerUseCase.getSeller(getQuery);

        // Then: 조회 성공
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.sellerId()).isEqualTo(sellerId);
        assertThat(retrieved.status()).isEqualTo(SellerStatus.ACTIVE);

        // Given: ChangeStatus (비활성화)
        ChangeSellerStatusCommand changeCommand = new ChangeSellerStatusCommand(sellerId, SellerStatus.INACTIVE);
        Seller deactivatedSeller = SellerFixture.anInactiveSeller();
        SellerResponse changeResponse = new SellerResponse(
            sellerId,
            String.valueOf(deactivatedSeller.getMustItSellerId().value()),
            deactivatedSeller.getSellerName(),
            deactivatedSeller.getStatus(),
            deactivatedSeller.getCreatedAt()
        );

        given(sellerQueryPort.findById(SellerId.of(sellerId)))
            .willReturn(Optional.of(registeredSeller));
        given(schedulerQueryPort.countActiveSchedulersBySellerId(sellerId))
            .willReturn(0);
        given(transactionManager.persist(any(Seller.class)))
            .willReturn(deactivatedSeller);
        given(sellerAssembler.toSellerResponse(any(Seller.class)))
            .willReturn(changeResponse);

        // When: ChangeStatus 실행
        SellerResponse changed = changeSellerStatusUseCase.changeStatus(changeCommand);

        // Then: 상태 변경 성공
        assertThat(changed).isNotNull();
        assertThat(changed.status()).isEqualTo(SellerStatus.INACTIVE);

        // Given: ListSellers
        ListSellersQuery listQuery = SellerQueryFixture.inactiveSellerPage();
        SellerSummaryResponse summaryResponse = new SellerSummaryResponse(
            sellerId,
            String.valueOf(deactivatedSeller.getMustItSellerId().value()),
            deactivatedSeller.getSellerName(),
            deactivatedSeller.getStatus(),
            0
        );

        given(sellerQueryPort.findByCriteria(any()))
            .willReturn(List.of(deactivatedSeller));
        given(sellerQueryPort.countByCriteria(any()))
            .willReturn(1L);
        given(schedulerQueryPort.countTotalSchedulersBySellerId(sellerId))
            .willReturn(0);
        given(sellerAssembler.toSellerSummaryResponse(deactivatedSeller, 0))
            .willReturn(summaryResponse);

        // When: ListSellers 실행
        PageResponse<SellerSummaryResponse> listed = listSellersUseCase.listSellers(listQuery);

        // Then: 목록 조회 성공
        assertThat(listed).isNotNull();
        assertThat(listed.content()).hasSize(1);
        assertThat(listed.content().get(0).sellerId()).isEqualTo(sellerId);
        assertThat(listed.content().get(0).status()).isEqualTo(SellerStatus.INACTIVE);
    }
}

