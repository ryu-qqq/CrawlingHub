package com.ryuqq.crawlinghub.domain.seller.exception;

import java.util.Map;

/**
 * 셀러를 찾을 수 없는 예외
 *
 * <p>존재하지 않는 셀러 ID로 조회를 시도할 때 발생합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public final class SellerNotFoundException extends SellerException {

    private final Long sellerId;

    /**
     * 셀러 미발견 예외 생성
     *
     * @param sellerId 찾을 수 없는 셀러 ID
     */
    public SellerNotFoundException(Long sellerId) {
        super(String.format("셀러를 찾을 수 없습니다: %d", sellerId));
        this.sellerId = sellerId;
    }

    /**
     * 찾을 수 없었던 셀러 ID 반환
     *
     * @return 셀러 ID
     */
    public Long getSellerId() {
        return sellerId;
    }

    @Override
    public String code() {
        return SellerErrorCode.SELLER_NOT_FOUND.getCode();
    }

    @Override
    public String message() {
        return getMessage();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of("sellerId", sellerId);
    }
}
