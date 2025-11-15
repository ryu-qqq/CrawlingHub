package com.ryuqq.crawlinghub.domain.seller;

/**
 * MustitSellerId Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class MustitSellerIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 MustitSellerId 생성
     *
     * @return MustitSellerId
     */
    public static MustItSellerId create() {
        return MustItSellerId.of(DEFAULT_ID);
    }

    /**
     * 지정된 ID로 MustitSellerId 생성
     *
     * @param id ID 값
     * @return MustitSellerId
     */
    public static MustItSellerId createWithId(Long id) {
        return MustItSellerId.of(id);
    }

    /**
     * null ID로 MustitSellerId 생성 (신규 엔티티용)
     *
     * @return null
     */
    public static MustItSellerId createNull() {
        return null;
    }
}
