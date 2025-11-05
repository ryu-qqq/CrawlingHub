package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

/**
 * Seller Exception - Abstract Class
 *
 * <p>Seller Bounded Context의 모든 예외를 묶는 추상 클래스입니다.</p>
 *
 * <p><strong>예외 계층:</strong></p>
 * <ul>
 *   <li>SellerNotFoundException - 셀러를 찾을 수 없음 (404)</li>
 *   <li>InactiveSellerException - 셀러가 비활성 상태 (409)</li>
 *   <li>DuplicateSellerCodeException - 중복된 셀러 코드 (409)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public abstract class SellerException extends DomainException {

    /**
     * SellerException 생성자
     *
     * @param message 에러 메시지
     */
    protected SellerException(String message) {
        super(message);
    }

    /**
     * SellerException 생성자 (원인 포함)
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    protected SellerException(String message, Throwable cause) {
        super(message, cause);
    }
}

