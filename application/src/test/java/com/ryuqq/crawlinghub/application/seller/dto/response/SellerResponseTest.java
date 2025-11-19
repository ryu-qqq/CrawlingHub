package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.application.fixture.seller.response.SellerResponseFixture;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerResponse")
class SellerResponseTest {

    @Test
    @DisplayName("셀러 식별자와 상태를 포함한 불변 응답이다")
    void shouldExposeSellerSnapshot() {
        SellerResponse response = SellerResponseFixture.sample();

        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.mustItSellerId()).isEqualTo("mustit-seller-001");
        assertThat(response.sellerName()).isEqualTo("머스트잇 셀러");
        assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.parse("2025-01-01T00:00:00"));
    }
}

