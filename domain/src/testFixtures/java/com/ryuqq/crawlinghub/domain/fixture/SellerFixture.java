package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.vo.SellerId;

/**
 * Seller Aggregate 테스트 데이터 생성 Fixture
 *
 * <p>Seller와 관련된 테스트 데이터의 기본값을 제공합니다.</p>
 *
 * <p>제공 메서드:</p>
 * <ul>
 *   <li>{@link #defaultSeller()} - 기본 설정의 Seller (ACTIVE 상태, 1일 주기)</li>
 *   <li>{@link #defaultSellerId()} - 기본 SellerId ("seller_test_001")</li>
 * </ul>
 */
public class SellerFixture {

    private static final String DEFAULT_SELLER_ID = "seller_test_001";

    /**
     * 기본 Seller 생성
     *
     * <p>기본값:</p>
     * <ul>
     *   <li>SellerId: "seller_test_001"</li>
     *   <li>Name: "테스트 셀러"</li>
     *   <li>CrawlingInterval: 1일</li>
     *   <li>Status: ACTIVE</li>
     *   <li>TotalProductCount: 0</li>
     * </ul>
     *
     * @return 기본 설정의 Seller Aggregate
     */
    public static Seller defaultSeller() {
        SellerId sellerId = new SellerId(DEFAULT_SELLER_ID);
        String name = "테스트 셀러";
        Integer intervalDays = 1;

        return Seller.register(sellerId, name, intervalDays);
    }

    /**
     * 기본 SellerId 반환
     *
     * @return SellerId("seller_test_001")
     */
    public static SellerId defaultSellerId() {
        return new SellerId(DEFAULT_SELLER_ID);
    }
}
