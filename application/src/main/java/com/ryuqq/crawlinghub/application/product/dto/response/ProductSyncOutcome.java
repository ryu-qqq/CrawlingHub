package com.ryuqq.crawlinghub.application.product.dto.response;

/**
 * 상품 동기화 결과 DTO
 *
 * <p>역할: ProductSyncOutbox 처리 결과를 전달하는 불변 객체
 *
 * <p>사용처:
 * <ul>
 *   <li>ProductSnapshotManager.processOne() 반환값</li>
 *   <li>ProductSyncOutboxProcessor에서 처리 결과 판단</li>
 * </ul>
 *
 * <p>상태 정의:
 * <ul>
 *   <li>success = true: 외부 Product API 호출 성공</li>
 *   <li>success = false: 외부 Product API 호출 실패</li>
 * </ul>
 *
 * @param success 성공 여부
 * @param message 처리 메시지
 * @param errorCode 에러 코드 (실패 시)
 * @param cause 실패 원인 (실패 시)
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record ProductSyncOutcome(
    boolean success,
    String message,
    String errorCode,
    String cause
) {

    /**
     * 성공 결과 생성
     *
     * @param message 성공 메시지
     * @return 성공 결과
     */
    public static ProductSyncOutcome success(String message) {
        return new ProductSyncOutcome(true, message, null, null);
    }

    /**
     * 실패 결과 생성
     *
     * @param errorCode 에러 코드
     * @param message 실패 메시지
     * @param cause 실패 원인
     * @return 실패 결과
     */
    public static ProductSyncOutcome failure(String errorCode, String message, String cause) {
        return new ProductSyncOutcome(false, message, errorCode, cause);
    }

    /**
     * 성공 여부 확인
     *
     * @return 성공 시 true, 실패 시 false
     */
    public boolean isSuccess() {
        return success;
    }
}
