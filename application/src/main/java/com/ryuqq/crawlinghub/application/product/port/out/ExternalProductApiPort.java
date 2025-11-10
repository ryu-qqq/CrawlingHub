package com.ryuqq.crawlinghub.application.product.port.out;

/**
 * 외부 상품 API Port (Outbound)
 *
 * <p>HTTP Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface ExternalProductApiPort {

    /**
     * 외부 상품 서버에 상품 데이터 업데이트
     *
     * @param productJson 상품 데이터 JSON (null 불가)
     * @throws IllegalArgumentException productJson이 null인 경우
     * @throws RuntimeException 외부 API 호출 실패 시
     */
    void updateProduct(String productJson);
}

