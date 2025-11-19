package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.application.fixture.seller.response.SellerDetailResponseFixture;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerDetailResponse")
class SellerDetailResponseTest {

    @Test
    @DisplayName("스케줄 통계와 타임스탬프를 포함한다")
    void shouldExposeSchedulerStatistics() {
        SellerDetailResponse response = SellerDetailResponseFixture.sample();

        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.mustItSellerId()).isEqualTo("mustit-seller-001");
        assertThat(response.sellerName()).isEqualTo("머스트잇 셀러");
        assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
        assertThat(response.activeSchedulerCount()).isEqualTo(2);
        assertThat(response.totalSchedulerCount()).isEqualTo(5);
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.parse("2025-01-01T00:00:00"));
        assertThat(response.updatedAt()).isEqualTo(LocalDateTime.parse("2025-01-02T00:00:00"));
    }
}

