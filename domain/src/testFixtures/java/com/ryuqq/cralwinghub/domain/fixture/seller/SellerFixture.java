package com.ryuqq.cralwinghub.domain.fixture.seller;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.crawlinghub.domain.common.Clock;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Seller Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class SellerFixture {

    private static final Clock DEFAULT_CLOCK = FixedClock.aDefaultClock();

    /**
     * 신규 활성 셀러 생성
     *
     * @return 신규 Seller (ID = null, ACTIVE)
     */
    public static Seller aNewActiveSeller() {
        return Seller.forNew(
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                DEFAULT_CLOCK);
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
                MustItSellerName.of(mustItSellerName), SellerName.of(sellerName), DEFAULT_CLOCK);
    }

    /**
     * 신규 활성 셀러 생성 (특정 Clock)
     *
     * @param clock 시간 제어
     * @return 신규 Seller (ID = null, ACTIVE)
     */
    public static Seller aNewActiveSeller(Clock clock) {
        return Seller.forNew(
                MustItSellerNameFixture.aDefaultName(), SellerNameFixture.aDefaultName(), clock);
    }

    /**
     * 할당된 ID를 가진 활성 셀러 생성
     *
     * @return Seller (ID = 1L, ACTIVE)
     */
    public static Seller anActiveSeller() {
        LocalDateTime now = LocalDateTime.ofInstant(DEFAULT_CLOCK.now(), ZoneId.systemDefault());
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                SellerStatus.ACTIVE,
                0,
                now,
                now,
                DEFAULT_CLOCK);
    }

    /**
     * 할당된 ID를 가진 활성 셀러 생성 (특정 ID)
     *
     * @param id 셀러 ID
     * @return Seller (ACTIVE)
     */
    public static Seller anActiveSeller(Long id) {
        LocalDateTime now = LocalDateTime.ofInstant(DEFAULT_CLOCK.now(), ZoneId.systemDefault());
        return Seller.of(
                SellerId.of(id),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                SellerStatus.ACTIVE,
                0,
                now,
                now,
                DEFAULT_CLOCK);
    }

    /**
     * 할당된 ID를 가진 활성 셀러 생성 (특정 Clock)
     *
     * @param clock 시간 제어
     * @return Seller (ID = 1L, ACTIVE)
     */
    public static Seller anActiveSeller(Clock clock) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                SellerStatus.ACTIVE,
                0,
                now,
                now,
                clock);
    }

    /**
     * 할당된 ID를 가진 비활성 셀러 생성
     *
     * @return Seller (ID = 1L, INACTIVE)
     */
    public static Seller anInactiveSeller() {
        LocalDateTime now = LocalDateTime.ofInstant(DEFAULT_CLOCK.now(), ZoneId.systemDefault());
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                SellerStatus.INACTIVE,
                0,
                now,
                now,
                DEFAULT_CLOCK);
    }

    /**
     * 할당된 ID를 가진 비활성 셀러 생성 (특정 Clock)
     *
     * @param clock 시간 제어
     * @return Seller (ID = 1L, INACTIVE)
     */
    public static Seller anInactiveSeller(Clock clock) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                SellerStatus.INACTIVE,
                0,
                now,
                now,
                clock);
    }

    /**
     * 상품 수가 있는 활성 셀러 생성
     *
     * @param productCount 상품 수
     * @return Seller (ID = 1L, ACTIVE, productCount)
     */
    public static Seller anActiveSellerWithProducts(int productCount) {
        LocalDateTime now = LocalDateTime.ofInstant(DEFAULT_CLOCK.now(), ZoneId.systemDefault());
        return Seller.of(
                SellerIdFixture.anAssignedId(),
                MustItSellerNameFixture.aDefaultName(),
                SellerNameFixture.aDefaultName(),
                SellerStatus.ACTIVE,
                productCount,
                now,
                now,
                DEFAULT_CLOCK);
    }

    private SellerFixture() {
        // Utility class
    }
}
