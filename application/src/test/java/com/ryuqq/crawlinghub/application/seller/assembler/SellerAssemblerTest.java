package com.ryuqq.crawlinghub.application.seller.assembler;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SellerAssembler 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Domain → Response DTO 변환</li>
 *   <li>✅ 모든 필드 매핑 검증</li>
 *   <li>✅ Mock 없이 실제 Domain 사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class SellerAssemblerTest {

    private final SellerAssembler sellerAssembler = new SellerAssembler();

    @Test
    void shouldConvertDomainToResponse() {
        // Given
        Seller seller = SellerFixture.forNew();

        // When
        SellerResponse response = sellerAssembler.toResponse(seller);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.sellerId()).isEqualTo(String.valueOf(seller.getSellerId().value()));
        assertThat(response.name()).isEqualTo(seller.getName());
        assertThat(response.status()).isEqualTo(seller.getStatus());
        assertThat(response.crawlingIntervalDays()).isEqualTo(seller.getCrawlingIntervalDays());
        assertThat(response.totalProductCount()).isEqualTo(seller.getTotalProductCount());
        assertThat(response.createdAt()).isEqualTo(seller.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(seller.getUpdatedAt());
    }

    @Test
    void shouldPreserveSellerStatus() {
        // Given
        Seller seller = SellerFixture.forNew();

        // When
        SellerResponse response = sellerAssembler.toResponse(seller);

        // Then
        assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
    }

    @Test
    void shouldPreserveAllTimestamps() {
        // Given
        Seller seller = SellerFixture.forNew();

        // When
        SellerResponse response = sellerAssembler.toResponse(seller);

        // Then
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
        assertThat(response.createdAt()).isEqualTo(seller.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(seller.getUpdatedAt());
    }
}
