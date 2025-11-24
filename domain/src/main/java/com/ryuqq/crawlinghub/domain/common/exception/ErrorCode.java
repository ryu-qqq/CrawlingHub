package com.ryuqq.crawlinghub.domain.common.exception;

/**
 * ErrorCode - 도메인 에러 코드 인터페이스
 *
 * <p>모든 Bounded Context의 ErrorCode Enum이 구현해야 하는 인터페이스입니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>✅ 형식: {BC}-{3자리 숫자} (예: SELLER-001, CRAWLER-001)
 *   <li>✅ HTTP 상태 코드 매핑 (404, 400, 409, 500 등)
 *   <li>✅ 명확한 에러 메시지
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * public enum SellerErrorCode implements ErrorCode {
 *     SELLER_NOT_FOUND("SELLER-001", 404, "Seller not found"),
 *     SELLER_NAME_DUPLICATED("SELLER-002", 409, "Seller name already exists");
 *
 *     private final String code;
 *     private final int httpStatus;
 *     private final String message;
 *
 *     SellerErrorCode(String code, int httpStatus, String message) {
 *         this.code = code;
 *         this.httpStatus = httpStatus;
 *         this.message = message;
 *     }
 *
 *     public String getCode() { return code; }
 *     public int getHttpStatus() { return httpStatus; }
 *     public String getMessage() { return message; }
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public interface ErrorCode {

    /**
     * 에러 코드 반환
     *
     * <p>형식: {BC}-{3자리 숫자}
     *
     * <p>예: SELLER-001, CRAWLER-001, PRODUCT-001
     *
     * @return 에러 코드 문자열
     * @author ryu-qqq
     * @since 2025-11-17
     */
    String getCode();

    /**
     * HTTP 상태 코드 반환
     *
     * <p>GlobalExceptionHandler에서 HTTP 응답 생성 시 사용됩니다.
     *
     * @return HTTP 상태 코드 (예: 404, 400, 409, 500)
     * @author ryu-qqq
     * @since 2025-11-17
     */
    int getHttpStatus();

    /**
     * 에러 메시지 반환
     *
     * <p>사용자에게 표시될 에러 메시지입니다.
     *
     * @return 에러 메시지 문자열
     * @author ryu-qqq
     * @since 2025-11-17
     */
    String getMessage();
}
