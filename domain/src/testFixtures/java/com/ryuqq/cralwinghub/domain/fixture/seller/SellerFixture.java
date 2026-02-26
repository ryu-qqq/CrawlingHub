package com.ryuqq.cralwinghub.domain.fixture.seller;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;

/**
 * Seller Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class SellerFixture {

    private static final Instant DEFAULT_INSTANT = FixedClock.aDefaultClock().instant();
    private static final Instant DEFAULT_TIME = DEFAULT_INSTANT;

    /**
     * 신규 활성 셀러 생성
     *
     * @return 신규 Seller (ID = null, ACTIVE)
     */
    public static Seller aNewActiveSeller() {
        return Seller.forNew(
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                DEFAULT_INSTANT);
    }

    /**
     * 신규 활성 셀러 생성 (특정 이름)
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @param sellerName 셀러명
     * @return 신규 Seller (ID = null, ACTIVE)
     */
    public static Seller aNewActiveSeller(String mustItSellerName, String sellerName) {
        return Seller.forNew(
                MustItSellerName.of(mustItSellerName), SellerName.of(sellerName), DEFAULT_INSTANT);
    }

    /**
     * 신규 활성 셀러 생성 (특정 시간)
     *
     * @param now 현재 시간
     * @return 신규 Seller (ID = null, ACTIVE)
     */
    public static Seller aNewActiveSeller(Instant now) {
        return Seller.forNew(
                MustItSellerNameFixture.aDefaultName(), SellerNameFixture.aDefaultName(), now);
    }

    /**
     * 할당된 ID를 가진 활성 셀러 생성
     *
     * @return Seller (ID = 1L, ACTIVE)
     */
    public static Seller anActiveSeller() {
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                null,
                SellerStatus.ACTIVE,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 할당된 ID를 가진 활성 셀러 생성 (특정 ID)
     *
     * @param id 셀러 ID
     * @return Seller (ACTIVE)
     */
    public static Seller anActiveSeller(Long id) {
        return Seller.of(
                SellerId.of(id),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                null,
                SellerStatus.ACTIVE,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 할당된 ID를 가진 활성 셀러 생성 (특정 시간)
     *
     * @param now 현재 시간
     * @return Seller (ID = 1L, ACTIVE)
     */
    public static Seller anActiveSeller(Instant now) {
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                null,
                SellerStatus.ACTIVE,
                0,
                now,
                now);
    }

    /**
     * 할당된 ID를 가진 비활성 셀러 생성
     *
     * @return Seller (ID = 1L, INACTIVE)
     */
    public static Seller anInactiveSeller() {
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                null,
                SellerStatus.INACTIVE,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 할당된 ID를 가진 비활성 셀러 생성 (특정 시간)
     *
     * @param now 현재 시간
     * @return Seller (ID = 1L, INACTIVE)
     */
    public static Seller anInactiveSeller(Instant now) {
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                null,
                SellerStatus.INACTIVE,
                0,
                now,
                now);
    }

    /**
     * 상품 수가 있는 활성 셀러 생성
     *
     * @param productCount 상품 수
     * @return Seller (ID = 1L, ACTIVE, productCount)
     */
    public static Seller anActiveSellerWithProducts(int productCount) {
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                null,
                SellerStatus.ACTIVE,
                productCount,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 상품 수가 있는 활성 셀러 생성 (특정 시간)
     *
     * @param productCount 상품 수
     * @param now 현재 시간
     * @return Seller (ID = 1L, ACTIVE, productCount)
     */
    public static Seller anActiveSellerWithProducts(int productCount, Instant now) {
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                null,
                SellerStatus.ACTIVE,
                productCount,
                now,
                now);
    }

    private SellerFixture() {
        // Utility class
    }
}
