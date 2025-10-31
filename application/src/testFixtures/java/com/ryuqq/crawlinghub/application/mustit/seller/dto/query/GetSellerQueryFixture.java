package com.ryuqq.crawlinghub.application.mustit.seller.dto.query;

/**
 * GetSellerQuery Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class GetSellerQueryFixture {

    private static final Long DEFAULT_SELLER_ID = 1L;

    /**
     * 기본 GetSellerQuery 생성
     *
     * @return GetSellerQuery
     */
    public static GetSellerQuery create() {
        return new GetSellerQuery(DEFAULT_SELLER_ID);
    }

    /**
     * 특정 셀러 ID로 GetSellerQuery 생성
     *
     * @param sellerId 셀러 ID
     * @return GetSellerQuery
     */
    public static GetSellerQuery createWithId(Long sellerId) {
        return new GetSellerQuery(sellerId);
    }
}
