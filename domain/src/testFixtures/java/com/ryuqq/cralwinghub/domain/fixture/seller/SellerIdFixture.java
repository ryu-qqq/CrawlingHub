package com.ryuqq.cralwinghub.domain.fixture.seller;

import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;

/**
 * SellerId Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class SellerIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 할당된 ID 생성
     *
     * @return SellerId (value = 1L)
     */
    public static SellerId anAssignedId() {
        return SellerId.of(DEFAULT_ID);
    }

    /**
     * 특정 값으로 할당된 ID 생성
     *
     * @param value ID 값
     * @return SellerId
     */
    public static SellerId anAssignedId(Long value) {
        return SellerId.of(value);
    }

    /**
     * 신규 생성용 ID (null)
     *
     * @return SellerId (value = null)
     */
    public static SellerId aNewId() {
        return SellerId.forNew();
    }

    private SellerIdFixture() {
        // Utility class
    }
}
