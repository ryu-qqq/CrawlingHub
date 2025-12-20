package com.ryuqq.cralwinghub.domain.fixture.crawl.task;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;

/**
 * CrawlEndpoint Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlEndpointFixture {

    private static final String DEFAULT_MUST_IT_SELLER_NAME = "test-seller";
    private static final Long DEFAULT_PRODUCT_ID = 99999L;

    /**
     * 미니샵 목록 엔드포인트 생성
     *
     * @return CrawlEndpoint (page=1, size=20)
     */
    public static CrawlEndpoint aMiniShopListEndpoint() {
        return CrawlEndpoint.forMiniShopList(DEFAULT_MUST_IT_SELLER_NAME, 1, 20);
    }

    /**
     * 미니샵 목록 엔드포인트 생성 (특정 셀러)
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint aMiniShopListEndpoint(
            String mustItSellerName, int page, int pageSize) {
        return CrawlEndpoint.forMiniShopList(mustItSellerName, page, pageSize);
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
