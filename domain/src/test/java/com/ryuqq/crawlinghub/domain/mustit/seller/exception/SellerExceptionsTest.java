package com.ryuqq.crawlinghub.domain.mustit.seller.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Seller 관련 예외 클래스 단위 테스트
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@DisplayName("Seller 예외 클래스 테스트")
class SellerExceptionsTest {

    @Test
    @DisplayName("DuplicateSellerException은 sellerId를 포함한 메시지를 생성한다")
    void duplicateSellerExceptionContainsSellerId() {
        // given
        String sellerId = "SELLER001";

        // when
        DuplicateSellerException exception = new DuplicateSellerException(sellerId);

        // then
        assertThat(exception.getMessage()).contains(sellerId);
        assertThat(exception.getMessage()).contains("already exists");
        assertThat(exception.getSellerId()).isEqualTo(sellerId);
    }

    @Test
    @DisplayName("DuplicateSellerException은 RuntimeException을 상속한다")
    void duplicateSellerExceptionExtendsRuntimeException() {
        // given
        String sellerId = "SELLER001";

        // when
        DuplicateSellerException exception = new DuplicateSellerException(sellerId);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InvalidSellerException은 메시지를 포함하여 생성할 수 있다")
    void invalidSellerExceptionWithMessage() {
        // given
        String message = "Invalid seller data";

        // when
        InvalidSellerException exception = new InvalidSellerException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("InvalidSellerException은 메시지와 원인 예외를 포함하여 생성할 수 있다")
    void invalidSellerExceptionWithMessageAndCause() {
        // given
        String message = "Invalid seller data";
        Throwable cause = new IllegalArgumentException("Invalid field");

        // when
        InvalidSellerException exception = new InvalidSellerException(message, cause);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("InvalidSellerException은 RuntimeException을 상속한다")
    void invalidSellerExceptionExtendsRuntimeException() {
        // given
        String message = "Invalid seller data";

        // when
        InvalidSellerException exception = new InvalidSellerException(message);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("SellerNotFoundException은 sellerId를 포함한 메시지를 생성한다")
    void sellerNotFoundExceptionContainsSellerId() {
        // given
        String sellerId = "SELLER001";

        // when
        SellerNotFoundException exception = new SellerNotFoundException(sellerId);

        // then
        assertThat(exception.getMessage()).contains(sellerId);
        assertThat(exception.getMessage()).contains("not found");
        assertThat(exception.getSellerId()).isEqualTo(sellerId);
    }

    @Test
    @DisplayName("SellerNotFoundException은 RuntimeException을 상속한다")
    void sellerNotFoundExceptionExtendsRuntimeException() {
        // given
        String sellerId = "SELLER001";

        // when
        SellerNotFoundException exception = new SellerNotFoundException(sellerId);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
