package com.ryuqq.crawlinghub.application.seller.assembler;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerAssembler 테스트")
class SellerAssemblerTest {

    private SellerAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new SellerAssembler();
    }

    @Test
    @DisplayName("RegisterSellerCommand → Seller Domain 변환 성공")
    void shouldConvertCommandToDomainSuccessfully() {
        // Given
        RegisterSellerCommand command = new RegisterSellerCommand("Test Seller");

        // When
        Seller seller = assembler.toDomain(command);

        // Then
        assertThat(seller).isNotNull();
        assertThat(seller.getName()).isEqualTo("Test Seller");
        assertThat(seller.getSellerId().isNew()).isTrue();
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
    }

    @Test
    @DisplayName("Seller Domain → SellerResponse 변환 성공")
    void shouldConvertDomainToResponseSuccessfully() {
        // Given
        Seller seller = Seller.of(SellerId.of(1L), "Test Seller");

        // When
        SellerResponse response = assembler.toResponse(seller);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Test Seller");
        assertThat(response.status()).isEqualTo(SellerStatus.INACTIVE);
    }
}
