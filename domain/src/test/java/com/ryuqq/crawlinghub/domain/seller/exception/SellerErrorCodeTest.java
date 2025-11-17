package com.ryuqq.crawlinghub.domain.seller.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SellerErrorCode 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("SellerErrorCode 테스트")
class SellerErrorCodeTest {

    @Test
    @DisplayName("SELLER_NOT_FOUND 에러 코드 검증")
    void shouldReturnCorrectErrorCodeForSellerNotFound() {
        // when
        SellerErrorCode errorCode = SellerErrorCode.SELLER_NOT_FOUND;

        // then
        assertThat(errorCode.getCode()).isEqualTo("SELLER-001");
        assertThat(errorCode.getHttpStatus()).isEqualTo(404);
        assertThat(errorCode.getMessage()).isEqualTo("Seller not found");
    }

    @Test
    @DisplayName("모든 ErrorCode Enum 상수 검증")
    void shouldHaveAllErrorCodeConstants() {
        // when
        SellerErrorCode[] errorCodes = SellerErrorCode.values();

        // then
        assertThat(errorCodes).hasSize(2);
        assertThat(errorCodes).contains(
                SellerErrorCode.SELLER_NOT_FOUND,
                SellerErrorCode.INVALID_SELLER_ARGUMENT
        );
    }
}
