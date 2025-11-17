package com.ryuqq.crawlinghub.domain.seller.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SellerNotFoundException 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("SellerNotFoundException 테스트")
class SellerNotFoundExceptionTest {

    @Test
    @DisplayName("메시지로 예외 생성")
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "Seller not found with id: 456";

        // when
        SellerNotFoundException exception = new SellerNotFoundException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.code()).isEqualTo("SELLER-001");
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("메시지와 원인으로 예외 생성")
    void shouldCreateExceptionWithMessageAndCause() {
        // given
        String message = "Seller not found with id: 456";
        Throwable cause = new IllegalArgumentException("Invalid seller ID");

        // when
        SellerNotFoundException exception = new SellerNotFoundException(message, cause);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.code()).isEqualTo("SELLER-001");
        assertThat(exception.args()).isEmpty();
    }
}
