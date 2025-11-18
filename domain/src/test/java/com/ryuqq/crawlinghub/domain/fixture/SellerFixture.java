package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * Seller 관련 테스트 데이터 생성 Fixture
 *
 * <p>TDD Tidy Phase - 테스트 가독성 향상을 위한 Fixture 패턴 적용</p>
 *
 * <p>핵심 원칙:</p>
 * <ul>
 *   <li>재사용 가능한 테스트 데이터 생성</li>
 *   <li>명확한 네이밍으로 의도 표현</li>
 *   <li>테스트 코드 중복 제거</li>
 * </ul>
 */
public class SellerFixture {

    /**
     * 기본 SellerId 생성
     *
     * @return 기본 SellerId (1L)
     */
    public static SellerId defaultSellerId() {
        return new SellerId(1L);
    }

    private SellerFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
