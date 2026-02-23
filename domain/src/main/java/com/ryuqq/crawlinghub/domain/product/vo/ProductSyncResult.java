package com.ryuqq.crawlinghub.domain.product.vo;

/** 상품 동기화 결과 */
public record ProductSyncResult(
        boolean success, Long externalProductId, String errorCode, String errorMessage) {

    public static ProductSyncResult success(Long externalProductId) {
        return new ProductSyncResult(true, externalProductId, null, null);
    }

    public static ProductSyncResult failure(String errorCode, String errorMessage) {
        return new ProductSyncResult(false, null, errorCode, errorMessage);
    }

    public String toErrorMessage() {
        return String.format("API 호출 실패: errorCode=%s, errorMessage=%s", errorCode, errorMessage);
    }
}
