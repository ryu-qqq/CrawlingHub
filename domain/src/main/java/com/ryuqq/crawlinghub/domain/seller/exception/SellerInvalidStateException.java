package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

import java.util.Map;

/**
 * SellerInvalidStateException - Seller 상태 전환 불가 시 발생하는 예외
 *
 * <p>비즈니스 메서드 실행 조건이 맞지 않을 때 발생합니다.</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>이미 ACTIVE 상태에서 activate() 시도</li>
 *   <li>이미 INACTIVE 상태에서 deactivate() 시도</li>
 * </ul>
 *
 * <p><strong>HTTP 응답:</strong></p>
 * <ul>
 *   <li>Status Code: 400 BAD REQUEST</li>
 *   <li>Error Code: SELLER-003</li>
 *   <li>Message: "Invalid seller state transition"</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public class SellerInvalidStateException extends DomainException {

    /**
     * Constructor - 기본 에러 메시지
     *
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-11-18
     */
    public SellerInvalidStateException(String message) {
        super(message);
    }

    /**
     * Constructor - 에러 메시지 + 원인 예외
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     * @author ryu-qqq
     * @since 2025-11-18
     */
    public SellerInvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String code() {
        return SellerErrorCode.INVALID_SELLER_STATE.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of();
    }
}
