package com.ryuqq.crawlinghub.domain.seller.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SellerInvalidStateException 테스트
 *
 * <p>TDD Phase: Green</p>
 * <ul>
 *   <li>예외 메시지 검증</li>
 *   <li>에러 코드 검증</li>
 *   <li>HTTP 상태 코드 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@DisplayName("SellerInvalidStateException 테스트")
class SellerInvalidStateExceptionTest {

    @Test
    @DisplayName("기본 생성자로 예외 생성 시 메시지 검증")
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "이미 활성화된 상태입니다";

        // when
        SellerInvalidStateException exception = new SellerInvalidStateException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("원인 예외와 함께 예외 생성 시 메시지 및 원인 검증")
    void shouldCreateExceptionWithMessageAndCause() {
        // given
        String message = "이미 비활성화된 상태입니다";
        Throwable cause = new IllegalStateException("원인 예외");

        // when
        SellerInvalidStateException exception = new SellerInvalidStateException(message, cause);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("에러 코드 검증 - SELLER-003 반환")
    void shouldReturnCorrectErrorCode() {
        // given
        SellerInvalidStateException exception = new SellerInvalidStateException("테스트");

        // when
        String errorCode = exception.code();

        // then
        assertThat(errorCode).isEqualTo("SELLER-003");
    }

    @Test
    @DisplayName("args() 메서드는 빈 Map 반환")
    void shouldReturnEmptyArgsMap() {
        // given
        SellerInvalidStateException exception = new SellerInvalidStateException("테스트");

        // when
        var args = exception.args();

        // then
        assertThat(args).isEmpty();
    }

    @Test
    @DisplayName("DomainException을 상속하므로 예외 계층 검증")
    void shouldExtendDomainException() {
        // given
        SellerInvalidStateException exception = new SellerInvalidStateException("테스트");

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
