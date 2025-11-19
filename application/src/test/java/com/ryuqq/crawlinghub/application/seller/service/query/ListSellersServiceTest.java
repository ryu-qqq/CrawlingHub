package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.fixture.seller.SellerQueryFixture;
import com.ryuqq.crawlinghub.application.fixture.seller.response.SellerSummaryResponseFixture;
import com.ryuqq.crawlinghub.application.seller.dto.query.ListSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryCriteria;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ListSellersService")
class ListSellersServiceTest {

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SchedulerQueryPort schedulerQueryPort;

    @Mock
    private SellerAssembler sellerAssembler;

    @InjectMocks
    private ListSellersService service;

    @Test
    @DisplayName("전체 셀러 목록을 조회해 페이징된 응답을 반환한다")
    void shouldListAllSellers() {
        ListSellersQuery query = new ListSellersQuery(null, 0, 20);
        Seller seller1 = SellerFixture.anActiveSeller();
        Seller seller2 = SellerFixture.anInactiveSeller();
        List<Seller> sellers = List.of(seller1, seller2);

        given(sellerQueryPort.findByCriteria(any(SellerQueryCriteria.class)))
            .willReturn(sellers);
        given(sellerQueryPort.countByCriteria(any(SellerQueryCriteria.class)))
            .willReturn(2L);
        given(schedulerQueryPort.countTotalSchedulersBySellerId(seller1.getSellerId().value()))
            .willReturn(3);
        given(schedulerQueryPort.countTotalSchedulersBySellerId(seller2.getSellerId().value()))
            .willReturn(5);
        given(sellerAssembler.toSellerSummaryResponse(any(Seller.class), eq(3)))
            .willReturn(SellerSummaryResponseFixture.sample());
        given(sellerAssembler.toSellerSummaryResponse(any(Seller.class), eq(5)))
            .willReturn(SellerSummaryResponseFixture.sample());

        PageResponse<SellerSummaryResponse> response = service.listSellers(query);

        assertThat(response.content()).hasSize(2);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(2L);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }

    @Test
    @DisplayName("상태로 필터링된 셀러 목록을 조회한다")
    void shouldFilterByStatus() {
        ListSellersQuery query = SellerQueryFixture.activeSellerPage();
        Seller seller = SellerFixture.anActiveSeller();
        List<Seller> sellers = List.of(seller);

        ArgumentCaptor<SellerQueryCriteria> criteriaCaptor = ArgumentCaptor.forClass(SellerQueryCriteria.class);

        given(sellerQueryPort.findByCriteria(criteriaCaptor.capture()))
            .willReturn(sellers);
        given(sellerQueryPort.countByCriteria(any(SellerQueryCriteria.class)))
            .willReturn(1L);
        given(schedulerQueryPort.countTotalSchedulersBySellerId(seller.getSellerId().value()))
            .willReturn(3);
        given(sellerAssembler.toSellerSummaryResponse(seller, 3))
            .willReturn(SellerSummaryResponseFixture.sample());

        PageResponse<SellerSummaryResponse> response = service.listSellers(query);

        SellerQueryCriteria capturedCriteria = criteriaCaptor.getValue();
        assertThat(capturedCriteria.status()).isEqualTo(SellerStatus.ACTIVE);
        assertThat(response.content()).hasSize(1);
    }

    @Test
    @DisplayName("페이지네이션을 지원한다")
    void shouldSupportPagination() {
        ListSellersQuery query = new ListSellersQuery(null, 1, 10);
        Seller seller = SellerFixture.anActiveSeller();
        List<Seller> sellers = List.of(seller);

        given(sellerQueryPort.findByCriteria(any(SellerQueryCriteria.class)))
            .willReturn(sellers);
        given(sellerQueryPort.countByCriteria(any(SellerQueryCriteria.class)))
            .willReturn(25L);
        given(schedulerQueryPort.countTotalSchedulersBySellerId(seller.getSellerId().value()))
            .willReturn(3);
        given(sellerAssembler.toSellerSummaryResponse(seller, 3))
            .willReturn(SellerSummaryResponseFixture.sample());

        PageResponse<SellerSummaryResponse> response = service.listSellers(query);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(25L);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isFalse();
    }
}

