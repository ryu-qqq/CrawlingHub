package com.ryuqq.crawlinghub.domain.mustit.seller.exception;

/**
 * 셀러를 찾을 수 없을 때 발생하는 예외
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public class SellerNotFoundException extends RuntimeException {

    private final String sellerId;

    /**
     * 셀러 미발견 예외를 생성합니다.
     *
     * @param sellerId 찾을 수 없는 셀러 ID
     */
    public SellerNotFoundException(String sellerId) {
        super(String.format("Seller with ID '%s' not found", sellerId));
        this.sellerId = sellerId;
    }

    /**
     * 찾을 수 없는 셀러 ID를 반환합니다.
     *
     * @return 셀러 ID
     */
    public String getSellerId() {
        return sellerId;
    }
}
