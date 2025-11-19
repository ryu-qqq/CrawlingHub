package com.ryuqq.crawlinghub.application.assembler;

import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerAssembler")
class SellerAssemblerTest {

    private final SellerAssembler assembler = new SellerAssembler();

    @Test
    @DisplayName("도메인 Seller를 SellerResponse로 변환한다")
    void shouldAssembleSellerResponse() {
        Seller seller = SellerFixture.anActiveSeller();

        SellerResponse response = assembler.toSellerResponse(seller);

        assertThat(response.sellerId()).isEqualTo(seller.getSellerId().value());
        assertThat(response.mustItSellerId()).isEqualTo(String.valueOf(seller.getMustItSellerId().value()));
        assertThat(response.sellerName()).isEqualTo(seller.getSellerName());
        assertThat(response.status()).isEqualTo(seller.getStatus());
        assertThat(response.createdAt()).isEqualTo(seller.getCreatedAt());
    }

    @Test
    @DisplayName("스케줄 통계 정보를 포함한 SellerDetailResponse로 변환한다")
    void shouldAssembleSellerDetailResponse() {
        Seller seller = SellerFixture.anActiveSeller();

        SellerDetailResponse response = assembler.toSellerDetailResponse(seller, 2, 5);

        assertThat(response.sellerId()).isEqualTo(seller.getSellerId().value());
        assertThat(response.mustItSellerId()).isEqualTo(String.valueOf(seller.getMustItSellerId().value()));
        assertThat(response.sellerName()).isEqualTo(seller.getSellerName());
        assertThat(response.status()).isEqualTo(seller.getStatus());
        assertThat(response.activeSchedulerCount()).isEqualTo(2);
        assertThat(response.totalSchedulerCount()).isEqualTo(5);
        assertThat(response.createdAt()).isEqualTo(seller.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(seller.getUpdatedAt());
    }

    @Test
    @DisplayName("요약 정보를 SellerSummaryResponse로 변환한다")
    void shouldAssembleSellerSummaryResponse() {
        Seller seller = SellerFixture.anInactiveSeller();

        SellerSummaryResponse response = assembler.toSellerSummaryResponse(seller, 7);

        assertThat(response.sellerId()).isEqualTo(seller.getSellerId().value());
        assertThat(response.mustItSellerId()).isEqualTo(String.valueOf(seller.getMustItSellerId().value()));
        assertThat(response.sellerName()).isEqualTo(seller.getSellerName());
        assertThat(response.status()).isEqualTo(seller.getStatus());
        assertThat(response.totalSchedulerCount()).isEqualTo(7);
    }
}

