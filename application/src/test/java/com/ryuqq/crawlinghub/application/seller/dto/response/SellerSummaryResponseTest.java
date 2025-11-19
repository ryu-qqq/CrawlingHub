package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.application.fixture.seller.response.SellerSummaryResponseFixture;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerSummaryResponse")
class SellerSummaryResponseTest {

    @Test
    @DisplayName("셀러 요약 정보를 제공한다")
    void shouldExposeSummaryInfo() {
        SellerSummaryResponse response = SellerSummaryResponseFixture.sample();

        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.mustItSellerId()).isEqualTo("mustit-seller-001");
        assertThat(response.sellerName()).isEqualTo("머스트잇 셀러");
        assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
        assertThat(response.totalSchedulerCount()).isEqualTo(5);
    }
}

