package com.ryuqq.crawlinghub.application.product.sync.port.out;

/**
 * 내부 상품 API Port (외부 API)
 *
 * <p>⚠️ 트랜잭션 경계 주의:
 * <ul>
 *   <li>이 Port는 내부 상품 시스템 API 호출이므로 트랜잭션 밖에서 실행해야 합니다</li>
 *   <li>UseCase 레벨에서 트랜잭션 분리 필요</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface InternalProductApiPort {

    /**
     * 내부 시스템으로 상품 동기화
     *
     * @param syncPayload JSON 형식의 상품 데이터
     * @return 동기화 결과 (성공 여부)
     */
    SyncResult syncProduct(String syncPayload);

    /**
     * 동기화 결과
     */
    record SyncResult(
        boolean success,
        String message,
        Integer statusCode
    ) {
        public boolean isSuccess() {
            return success;
        }
    }
}
