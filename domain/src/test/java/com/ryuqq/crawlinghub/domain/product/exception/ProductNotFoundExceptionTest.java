package com.ryuqq.crawlinghub.domain.product.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductNotFoundException 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("ProductNotFoundException 테스트")
class ProductNotFoundExceptionTest {

    @Test
    @DisplayName("메시지로 예외 생성")
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "Product not found with id: 123";

        // when
        ProductNotFoundException exception = new ProductNotFoundException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.code()).isEqualTo("PRODUCT-001");
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("메시지와 원인으로 예외 생성")
    void shouldCreateExceptionWithMessageAndCause() {
        // given
        String message = "Product not found with id: 123";
        Throwable cause = new IllegalArgumentException("Invalid product ID");

        // when
        ProductNotFoundException exception = new ProductNotFoundException(message, cause);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.code()).isEqualTo("PRODUCT-001");
        assertThat(exception.args()).isEmpty();
    }
}
