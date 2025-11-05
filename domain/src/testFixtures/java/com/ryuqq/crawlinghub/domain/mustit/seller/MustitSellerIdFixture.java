package com.ryuqq.crawlinghub.domain.mustit.seller;

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
    public static MustitSellerId create() {
        return MustitSellerId.of(DEFAULT_ID);
    }

    /**
     * 지정된 ID로 MustitSellerId 생성
     *
     * @param id ID 값
     * @return MustitSellerId
     */
    public static MustitSellerId createWithId(Long id) {
        return MustitSellerId.of(id);
    }

    /**
     * null ID로 MustitSellerId 생성 (신규 엔티티용)
     *
     * @return null
     */
    public static MustitSellerId createNull() {
        return null;
    }
}
