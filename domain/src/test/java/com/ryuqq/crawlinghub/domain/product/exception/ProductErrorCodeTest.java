package com.ryuqq.crawlinghub.domain.product.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductErrorCode 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("ProductErrorCode 테스트")
class ProductErrorCodeTest {

    @Test
    @DisplayName("PRODUCT_NOT_FOUND 에러 코드 검증")
    void shouldReturnCorrectErrorCodeForProductNotFound() {
        // when
        ProductErrorCode errorCode = ProductErrorCode.PRODUCT_NOT_FOUND;

        // then
        assertThat(errorCode.getCode()).isEqualTo("PRODUCT-001");
        assertThat(errorCode.getHttpStatus()).isEqualTo(404);
        assertThat(errorCode.getMessage()).isEqualTo("Product not found");
    }

    @Test
    @DisplayName("모든 ErrorCode Enum 상수 검증")
    void shouldHaveAllErrorCodeConstants() {
        // when
        ProductErrorCode[] errorCodes = ProductErrorCode.values();

        // then
        assertThat(errorCodes).hasSize(5);
        assertThat(errorCodes).contains(
                ProductErrorCode.PRODUCT_NOT_FOUND,
                ProductErrorCode.OUTBOX_NOT_FOUND,
                ProductErrorCode.INVALID_PRODUCT_ARGUMENT,
                ProductErrorCode.INVALID_OUTBOX_STATE,
                ProductErrorCode.OUTBOX_MAX_RETRY_EXCEEDED
        );
    }
}
