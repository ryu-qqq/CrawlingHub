package com.ryuqq.cralwinghub.domain.fixture.crawl.task;

import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlEndpoint;

/**
 * CrawlEndpoint Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlEndpointFixture {

    private static final Long DEFAULT_SELLER_ID = 12345L;
    private static final Long DEFAULT_PRODUCT_ID = 99999L;

    /**
     * 미니샵 목록 엔드포인트 생성
     *
     * @return CrawlEndpoint (page=1, size=20)
     */
    public static CrawlEndpoint aMiniShopListEndpoint() {
        return CrawlEndpoint.forMiniShopList(DEFAULT_SELLER_ID, 1, 20);
    }

    /**
     * 미니샵 목록 엔드포인트 생성 (특정 셀러)
     *
     * @param sellerId 셀러 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint aMiniShopListEndpoint(Long sellerId, int page, int pageSize) {
        return CrawlEndpoint.forMiniShopList(sellerId, page, pageSize);
    }

    /**
     * 상품 상세 엔드포인트 생성
     *
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint aProductDetailEndpoint() {
        return CrawlEndpoint.forProductDetail(DEFAULT_PRODUCT_ID);
    }

    /**
     * 상품 옵션 엔드포인트 생성
     *
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint aProductOptionEndpoint() {
        return CrawlEndpoint.forProductOption(DEFAULT_PRODUCT_ID);
    }

    private CrawlEndpointFixture() {
        // Utility class
    }
}
