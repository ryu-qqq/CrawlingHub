package com.ryuqq.crawlinghub.domain.seller;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MustitSeller Test Fixture
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
public class MustitSellerFixture {

    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_SELLER_CODE = "SEL001";
    private static final String DEFAULT_SELLER_NAME = "테스트셀러";
    private static final Clock DEFAULT_CLOCK = Clock.fixed(
        Instant.parse("2025-01-01T00:00:00Z"),
        ZoneId.systemDefault()
    );

    /**
     * 기본 MustitSeller 생성 (신규)
     *
     * @return MustitSeller
     */
    public static MustItSeller create() {
        return MustItSeller.forNew(
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME
        );
    }

    /**
     * ID를 가진 MustitSeller 생성
     *
     * @param id MustitSeller ID
     * @return MustitSeller
     */
    public static MustItSeller createWithId(Long id) {
        return MustItSeller.of(
            MustItSellerId.of(id),
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            SellerStatus.ACTIVE
        );
    }

    /**
     * 특정 셀러 코드로 MustitSeller 생성
     *
     * @param sellerCode 셀러 코드
     * @return MustitSeller
     */
    public static MustItSeller createWithCode(String sellerCode) {
        return MustItSeller.forNew(
            sellerCode,
            DEFAULT_SELLER_NAME
        );
    }

    /**
     * 특정 셀러 이름으로 MustitSeller 생성
     *
     * @param sellerName 셀러 이름
     * @return MustitSeller
     */
    public static MustItSeller createWithName(String sellerName) {
        return MustItSeller.forNew(
            DEFAULT_SELLER_CODE,
            sellerName
        );
    }

    /**
     * ACTIVE 상태의 MustitSeller 생성
     *
     * @return MustitSeller
     */
    public static MustItSeller createActive() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return MustItSeller.reconstitute(
            MustItSellerId.of(DEFAULT_ID),
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            SellerStatus.ACTIVE,
            100,
            null,  // lastCrawledAt - 테스트를 위해 null로 설정
            now,
            now
        );
    }

    /**
     * PAUSED 상태의 MustitSeller 생성
     *
     * @return MustitSeller
     */
    public static MustItSeller createPaused() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return MustItSeller.reconstitute(
            MustItSellerId.of(DEFAULT_ID),
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            SellerStatus.PAUSED,
            50,
            now.minusDays(1),
            now,
            now
        );
    }

    /**
     * DISABLED 상태의 MustitSeller 생성
     *
     * @return MustitSeller
     */
    public static MustItSeller createDisabled() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return MustItSeller.reconstitute(
            MustItSellerId.of(DEFAULT_ID),
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            SellerStatus.DISABLED,
            0,
            now.minusDays(7),
            now,
            now
        );
    }

    /**
     * 특정 상품 수를 가진 MustitSeller 생성
     *
     * @param productCount 상품 수
     * @return MustitSeller
     */
    public static MustItSeller createWithProductCount(Integer productCount) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return MustItSeller.reconstitute(
            MustItSellerId.of(DEFAULT_ID),
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            SellerStatus.ACTIVE,
            productCount,
            now,
            now,
            now
        );
    }

    /**
     * DB reconstitute용 MustitSeller 생성
     *
     * @param id MustitSeller ID
     * @param status 셀러 상태
     * @param productCount 상품 수
     * @return MustitSeller
     */
    public static MustItSeller reconstitute(
        Long id,
        SellerStatus status,
        Integer productCount
    ) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return MustItSeller.reconstitute(
            MustItSellerId.of(id),
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            status,
            productCount,
            now,
            now,
            now
        );
    }

    /**
     * 완전한 커스텀 MustitSeller 생성
     *
     * @param id MustitSeller ID (null 가능)
     * @param sellerCode 셀러 코드
     * @param sellerName 셀러 이름
     * @param status 셀러 상태
     * @return MustitSeller
     */
    public static MustItSeller createCustom(
        Long id,
        String sellerCode,
        String sellerName,
        SellerStatus status
    ) {
        if (id == null) {
            return MustItSeller.forNew(sellerCode, sellerName);
        }
        return MustItSeller.of(
            MustItSellerId.of(id),
            sellerCode,
            sellerName,
            status
        );
    }
}
