package com.ryuqq.crawlinghub.application.seller.dto.query;

import com.ryuqq.crawlinghub.application.fixture.seller.SellerQueryFixture;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ListSellersQuery")
class ListSellersQueryTest {

    @Test
    @DisplayName("상태 필터와 페이징 파라미터를 보존한다")
    void shouldExposeFilterAndPagination() {
        ListSellersQuery query = SellerQueryFixture.activeSellerPage();

        assertThat(query.status()).isEqualTo(SellerStatus.ACTIVE);
        assertThat(query.page()).isEqualTo(0);
        assertThat(query.size()).isEqualTo(20);
    }
}

