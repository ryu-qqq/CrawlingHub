package com.ryuqq.crawlinghub.domain.seller.exception;

/**
 * 비활성 셀러 예외
 *
 * <p>셀러가 DISABLED 또는 PAUSED 상태일 때 크롤링 시도 시 발생합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class InactiveSellerException extends RuntimeException {

    private final Long sellerId;
    private final String sellerName;

    /**
     * InactiveSellerException 생성자
     *
     * @param sellerId 셀러 ID
     * @param sellerName 셀러 이름
     */
    public InactiveSellerException(Long sellerId, String sellerName) {
        super(String.format("셀러가 비활성 상태입니다: ID=%d, Name=%s", sellerId, sellerName));
        this.sellerId = sellerId;
        this.sellerName = sellerName;
    }

    /**
     * 셀러 ID 반환
     *
     * @return 셀러 ID
     */
    public Long getSellerId() {
        return sellerId;
    }

    /**
     * 셀러 이름 반환
     *
     * @return 셀러 이름
     */
    public String getSellerName() {
        return sellerName;
    }
}
