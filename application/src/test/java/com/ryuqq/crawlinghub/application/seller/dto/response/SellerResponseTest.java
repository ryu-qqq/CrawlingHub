package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SellerResponse DTO 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Domain 객체로부터 Response 생성</li>
 *   <li>✅ 모든 필드 매핑 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class SellerResponseTest {

    @Test
    void shouldCreateResponseFromDomain() {
        // Given
        Seller seller = SellerFixture.forNew();

        // When
        SellerResponse response = new SellerResponse(
            String.valueOf(seller.getSellerId().value()),
            seller.getName(),
            seller.getStatus(),
            seller.getCrawlingIntervalDays(),
            seller.getTotalProductCount(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );

        // Then
        assertThat(response.sellerId()).isEqualTo(String.valueOf(seller.getSellerId().value()));
        assertThat(response.name()).isEqualTo(seller.getName());
        assertThat(response.status()).isEqualTo(seller.getStatus());
        assertThat(response.crawlingIntervalDays()).isEqualTo(seller.getCrawlingIntervalDays());
        assertThat(response.totalProductCount()).isEqualTo(seller.getTotalProductCount());
        assertThat(response.createdAt()).isEqualTo(seller.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(seller.getUpdatedAt());
    }

    @Test
    void shouldHaveCorrectSellerStatus() {
        // Given
        Seller seller = SellerFixture.forNew();

        // When
        SellerResponse response = new SellerResponse(
            String.valueOf(seller.getSellerId().value()),
            seller.getName(),
            seller.getStatus(),
            seller.getCrawlingIntervalDays(),
            seller.getTotalProductCount(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );

        // Then
        assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
    }

    @Test
    void shouldHaveZeroProductCountForNewSeller() {
        // Given
        Seller seller = SellerFixture.forNew();

        // When
        SellerResponse response = new SellerResponse(
            String.valueOf(seller.getSellerId().value()),
            seller.getName(),
            seller.getStatus(),
            seller.getCrawlingIntervalDays(),
            seller.getTotalProductCount(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );

        // Then
        assertThat(response.totalProductCount()).isEqualTo(0);
    }

    @Test
    void shouldHaveValidTimestamps() {
        // Given
        Seller seller = SellerFixture.forNew();
        LocalDateTime now = LocalDateTime.now();

        // When
        SellerResponse response = new SellerResponse(
            String.valueOf(seller.getSellerId().value()),
            seller.getName(),
            seller.getStatus(),
            seller.getCrawlingIntervalDays(),
            seller.getTotalProductCount(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );

        // Then
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
        assertThat(response.createdAt()).isBeforeOrEqualTo(now.plusSeconds(1));
        assertThat(response.updatedAt()).isBeforeOrEqualTo(now.plusSeconds(1));
    }
}
