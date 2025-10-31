package com.ryuqq.crawlinghub.domain.product;

/**
 * ProductId Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class ProductIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 ProductId 생성
     *
     * @return ProductId
     */
    public static ProductId create() {
        return ProductId.of(DEFAULT_ID);
    }

    /**
     * 지정된 ID로 ProductId 생성
     *
     * @param id ID 값
     * @return ProductId
     */
    public static ProductId createWithId(Long id) {
        return ProductId.of(id);
    }

    /**
     * null ID로 ProductId 생성 (신규 엔티티용)
     *
     * @return null
     */
    public static ProductId createNull() {
        return null;
    }
}
