package com.ryuqq.cralwinghub.domain.fixture.seller;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;

/**
 * SellerName Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class SellerNameFixture {

    private static final String DEFAULT_NAME = "test-seller";

    /**
     * 기본 셀러명 생성
     *
     * @return SellerName
     */
    public static SellerName aDefaultName() {
        return SellerName.of(DEFAULT_NAME);
    }

    /**
     * 특정 값으로 셀러명 생성
     *
     * @param value 셀러명
     * @return SellerName
     */
    public static SellerName aName(String value) {
        return SellerName.of(value);
    }

    private SellerNameFixture() {
        // Utility class
    }
}
