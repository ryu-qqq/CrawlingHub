package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.fixture.seller.SellerQueryFixture;
import com.ryuqq.crawlinghub.application.fixture.seller.response.SellerDetailResponseFixture;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSellerService")
class GetSellerServiceTest {

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SchedulerQueryPort schedulerQueryPort;

    @Mock
    private SellerAssembler sellerAssembler;

    @InjectMocks
    private GetSellerService service;

    @Test
    @DisplayName("셀러 상세 정보를 조회해 응답을 반환한다")
    void shouldGetSellerDetailSuccessfully() {
        GetSellerQuery query = SellerQueryFixture.sellerDetail();
        Seller seller = SellerFixture.anInactiveSeller();
        SellerDetailResponse expected = SellerDetailResponseFixture.sample();

        given(sellerQueryPort.findById(SellerId.of(query.sellerId())))
            .willReturn(Optional.of(seller));
        given(schedulerQueryPort.countActiveSchedulersBySellerId(query.sellerId()))
            .willReturn(1);
        given(schedulerQueryPort.countTotalSchedulersBySellerId(query.sellerId()))
            .willReturn(3);
        given(sellerAssembler.toSellerDetailResponse(seller, 1, 3))
            .willReturn(expected);

        SellerDetailResponse response = service.getSeller(query);

        assertThat(response).isEqualTo(expected);
        verify(sellerAssembler).toSellerDetailResponse(seller, 1, 3);
    }

    @Test
    @DisplayName("셀러가 존재하지 않으면 예외를 던진다")
    void shouldThrowExceptionWhenSellerNotFound() {
        GetSellerQuery query = SellerQueryFixture.sellerDetail();

        given(sellerQueryPort.findById(SellerId.of(query.sellerId())))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSeller(query))
            .isInstanceOf(SellerNotFoundException.class);
    }
}

