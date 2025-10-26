package com.ryuqq.crawlinghub.domain.mustit.seller.exception;

/**
 * 유효하지 않은 셀러 정보로 작업을 시도할 때 발생하는 예외
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public class InvalidSellerException extends RuntimeException {

    /**
     * 유효하지 않은 셀러 예외를 생성합니다.
     *
     * @param message 예외 메시지
     */
    public InvalidSellerException(String message) {
        super(message);
    }

    /**
     * 유효하지 않은 셀러 예외를 생성합니다.
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public InvalidSellerException(String message, Throwable cause) {
        super(message, cause);
    }
}
