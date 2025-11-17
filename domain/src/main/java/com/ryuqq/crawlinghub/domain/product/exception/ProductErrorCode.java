package com.ryuqq.crawlinghub.domain.product.exception;

import com.ryuqq.crawlinghub.domain.common.ErrorCode;

/**
 * ProductErrorCode - Product Bounded Context 에러 코드
 *
 * <p>Product 도메인에서 발생하는 모든 비즈니스 예외의 에러 코드를 정의합니다.</p>
 *
 * <p><strong>에러 코드 규칙:</strong></p>
 * <ul>
 *   <li>✅ 형식: PRODUCT-{3자리 숫자}</li>
 *   <li>✅ HTTP 상태 코드 매핑</li>
 *   <li>✅ 명확한 에러 메시지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public enum ProductErrorCode implements ErrorCode {

    /**
     * Product를 찾을 수 없음
     */
    PRODUCT_NOT_FOUND("PRODUCT-001", 404, "Product not found"),

    /**
     * ProductOutbox를 찾을 수 없음
     */
    OUTBOX_NOT_FOUND("PRODUCT-002", 404, "Product outbox not found"),

    /**
     * 유효하지 않은 Product 파라미터
     */
    INVALID_PRODUCT_ARGUMENT("PRODUCT-003", 400, "Invalid product argument"),

    /**
     * ProductOutbox 상태 전환 불가
     */
    INVALID_OUTBOX_STATE("PRODUCT-004", 400, "Invalid outbox state transition"),

    /**
     * ProductOutbox 최대 재시도 초과
     */
    OUTBOX_MAX_RETRY_EXCEEDED("PRODUCT-005", 500, "Product outbox max retry count exceeded");

    private final String code;
    private final int httpStatus;
    private final String message;

    /**
     * Constructor - ErrorCode 생성
     *
     * @param code 에러 코드 (PRODUCT-XXX)
     * @param httpStatus HTTP 상태 코드
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-11-17
     */
    ProductErrorCode(String code, int httpStatus, String message) {
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
