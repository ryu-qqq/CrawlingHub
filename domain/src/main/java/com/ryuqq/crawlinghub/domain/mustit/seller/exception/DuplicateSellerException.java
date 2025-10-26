package com.ryuqq.crawlinghub.domain.mustit.seller.exception;

/**
 * 중복된 셀러 ID로 등록을 시도할 때 발생하는 예외
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public class DuplicateSellerException extends RuntimeException {

    private final String sellerId;

    /**
     * 중복 셀러 예외를 생성합니다.
     *
     * @param sellerId 중복된 셀러 ID
     */
    public DuplicateSellerException(String sellerId) {
        super(String.format("Seller with ID '%s' already exists", sellerId));
        this.sellerId = sellerId;
    }

    /**
     * 중복된 셀러 ID를 반환합니다.
     *
     * @return 셀러 ID
     */
    public String getSellerId() {
        return sellerId;
    }
}
