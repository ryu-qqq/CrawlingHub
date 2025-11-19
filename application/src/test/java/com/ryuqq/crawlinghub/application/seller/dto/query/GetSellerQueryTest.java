package com.ryuqq.crawlinghub.application.seller.dto.query;

import com.ryuqq.crawlinghub.application.fixture.seller.SellerQueryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GetSellerQuery")
class GetSellerQueryTest {

    @Test
    @DisplayName("조회할 셀러 ID를 보존한다")
    void shouldExposeSellerId() {
        GetSellerQuery query = SellerQueryFixture.sellerDetail();

        assertThat(query.sellerId()).isEqualTo(1L);
    }
}

