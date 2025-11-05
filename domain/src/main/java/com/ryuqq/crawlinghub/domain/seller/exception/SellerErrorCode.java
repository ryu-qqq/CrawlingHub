package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.ErrorCode;

/**
 * Seller Bounded Context 전용 ErrorCode
 *
 * <p>Seller 도메인에서 발생하는 모든 비즈니스 예외의 에러 코드를 정의합니다.</p>
 *
 * <p><strong>코드 체계:</strong></p>
 * <ul>
 *   <li>SELLER-001 ~ SELLER-009: Not Found (404)</li>
 *   <li>SELLER-010 ~ SELLER-099: Conflict (409)</li>
 *   <li>SELLER-101 ~ SELLER-199: Bad Request (400)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public enum SellerErrorCode implements ErrorCode {

    /**
     * 셀러를 찾을 수 없음
     */
    SELLER_NOT_FOUND("SELLER-001", 404, "셀러를 찾을 수 없습니다"),

    /**
     * 셀러가 비활성 상태
     */
    SELLER_INACTIVE("SELLER-010", 409, "셀러가 비활성 상태입니다"),

    /**
     * 중복된 셀러 코드
     */
    DUPLICATE_SELLER_CODE("SELLER-011", 409, "이미 존재하는 셀러 코드입니다");

    private final String code;
    private final int httpStatus;
    private final String message;

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

