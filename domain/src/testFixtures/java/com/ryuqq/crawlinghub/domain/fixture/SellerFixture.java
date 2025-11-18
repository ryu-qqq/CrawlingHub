package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

import java.time.Clock;

/**
 * Seller Aggregate 테스트 데이터 생성 Fixture
 *
 * <p>Seller와 관련된 테스트 데이터의 기본값을 제공합니다.</p>
 *
 * <p>표준 패턴 준수:</p>
 * <ul>
 *   <li>{@link #forNew()} - 새 Seller 생성 (ID 자동 생성)</li>
 *   <li>{@link #of(SellerId, String)} - 불변 속성으로 재구성</li>
 *   <li>{@link #reconstitute(SellerId, String, SellerStatus, Integer)} - 완전한 재구성</li>
 * </ul>
 *
 * <p>레거시 호환 메서드:</p>
 * <ul>
 *   <li>{@link #defaultSeller()} - 기본 설정의 Seller (ACTIVE 상태)</li>
 *   <li>{@link #defaultSellerId()} - 기본 SellerId (1L)</li>
 * </ul>
 */
public class SellerFixture {

    private static final Long DEFAULT_SELLER_ID = 1L;
    private static final String DEFAULT_NAME = "테스트 셀러";

    /**
     * 새로운 Seller 생성 (표준 패턴)
     *
     * <p>forNew() 패턴: ID 자동 생성, ACTIVE 상태</p>
     *
     * @return 새로 생성된 Seller
     */
    public static Seller forNew() {
        return forNew(Clock.systemDefaultZone());
    }

    /**
     * 새로운 Seller 생성 (표준 패턴 + Clock 주입)
     *
     * <p>forNew(Clock) 패턴: ID 자동 생성, ACTIVE 상태, Clock 주입</p>
     *
     * @param clock 시간 제어 (테스트 가능성)
     * @return 새로 생성된 Seller
     */
    public static Seller forNew(Clock clock) {
        SellerId sellerId = new SellerId(DEFAULT_SELLER_ID);
        return Seller.forNew(sellerId, DEFAULT_NAME, clock);
    }

    /**
     * 불변 속성으로 Seller 재구성 (표준 패턴)
     *
     * <p>of() 패턴: ID 포함, 테스트용 간편 생성</p>
     *
     * @param sellerId Seller ID
     * @param name 셀러 이름
     * @return 재구성된 Seller
     */
    public static Seller of(SellerId sellerId, String name) {
        return of(sellerId, name, Clock.systemDefaultZone());
    }

    /**
     * 불변 속성으로 Seller 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>of(Clock) 패턴: ID 포함, 테스트용 간편 생성, Clock 주입</p>
     *
     * @param sellerId Seller ID
     * @param name 셀러 이름
     * @param clock 시간 제어
     * @return 재구성된 Seller
     */
    public static Seller of(SellerId sellerId, String name, Clock clock) {
        return Seller.of(sellerId, name, clock);
    }

    /**
     * 완전한 Seller 재구성 (표준 패턴)
     *
     * <p>reconstitute() 패턴: 모든 필드 포함, DB 조회 시뮬레이션</p>
     *
     * @param sellerId Seller ID
     * @param name 셀러 이름
     * @param status 상태
     * @param totalProductCount 총 상품 수
     * @return 재구성된 Seller
     */
    public static Seller reconstitute(SellerId sellerId, String name, SellerStatus status, Integer totalProductCount) {
        return reconstitute(sellerId, name, status, totalProductCount, Clock.systemDefaultZone());
    }

    /**
     * 완전한 Seller 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>reconstitute(Clock) 패턴: 모든 필드 포함, DB 조회 시뮬레이션, Clock 주입</p>
     *
     * @param sellerId Seller ID
     * @param name 셀러 이름
     * @param status 상태
     * @param totalProductCount 총 상품 수
     * @param clock 시간 제어
     * @return 재구성된 Seller
     */
    public static Seller reconstitute(SellerId sellerId, String name, SellerStatus status, Integer totalProductCount, Clock clock) {
        return Seller.reconstitute(sellerId, name, status, totalProductCount, clock);
    }

    /**
     * 기본 Seller 생성 (레거시)
     *
     * @deprecated Use {@link #forNew()} instead
     * @return 기본 설정의 Seller Aggregate
     */
    @Deprecated
    public static Seller defaultSeller() {
        return forNew();
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
