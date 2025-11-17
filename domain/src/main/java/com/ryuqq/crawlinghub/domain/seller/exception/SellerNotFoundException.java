package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

import java.util.Map;

/**
 * SellerNotFoundException - Seller를 찾을 수 없음 예외
 *
 * <p>요청한 Seller ID에 해당하는 Seller가 존재하지 않을 때 발생합니다.</p>
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>✅ 존재하지 않는 Seller ID로 조회</li>
 *   <li>✅ 삭제된 Seller 접근</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class SellerNotFoundException extends DomainException {

    /**
     * Constructor - 기본 에러 메시지
     *
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public SellerNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor - 에러 메시지 + 원인 예외
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public SellerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String code() {
        return SellerErrorCode.SELLER_NOT_FOUND.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of();
    }
}
