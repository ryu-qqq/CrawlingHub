package com.ryuqq.cralwinghub.domain.fixture.seller;

import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;

/**
 * MustItSellerName Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class MustItSellerNameFixture {

    private static final String DEFAULT_NAME = "mustit-test-seller";

    /**
     * 기본 머스트잇 셀러명 생성
     *
     * @return MustItSellerName
     */
    public static MustItSellerName aDefaultName() {
        return MustItSellerName.of(DEFAULT_NAME);
    }

    /**
     * 특정 값으로 머스트잇 셀러명 생성
     *
     * @param value 셀러명
     * @return MustItSellerName
     */
    public static MustItSellerName aName(String value) {
        return MustItSellerName.of(value);
    }

    private MustItSellerNameFixture() {
        // Utility class
    }
}
