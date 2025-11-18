package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.ErrorCode;

/**
 * SellerErrorCode - Seller Bounded Context 에러 코드
 *
 * <p>Seller 도메인에서 발생하는 모든 비즈니스 예외의 에러 코드를 정의합니다.</p>
 *
 * <p><strong>에러 코드 규칙:</strong></p>
 * <ul>
 *   <li>✅ 형식: SELLER-{3자리 숫자}</li>
 *   <li>✅ HTTP 상태 코드 매핑</li>
 *   <li>✅ 명확한 에러 메시지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public enum SellerErrorCode implements ErrorCode {

    /**
     * Seller를 찾을 수 없음
     */
    SELLER_NOT_FOUND("SELLER-001", 404, "Seller not found"),

    /**
     * 유효하지 않은 Seller 파라미터
     */
    INVALID_SELLER_ARGUMENT("SELLER-002", 400, "Invalid seller argument"),

    /**
     * 유효하지 않은 Seller 상태 전환
     */
    INVALID_SELLER_STATE("SELLER-003", 400, "Invalid seller state transition"),

    /**
     * Seller 중복
     */
    SELLER_DUPLICATED("SELLER-004", 409, "Seller already exists");

    private final String code;
    private final int httpStatus;
    private final String message;

    /**
     * Constructor - ErrorCode 생성
     *
     * @param code 에러 코드 (SELLER-XXX)
     * @param httpStatus HTTP 상태 코드
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-11-17
     */
    SellerErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
