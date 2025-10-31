package com.ryuqq.crawlinghub.domain.product;

/**
 * ProductData Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class ProductDataFixture {

    private static final String DEFAULT_JSON_DATA = "{\"name\":\"테스트 상품\",\"price\":10000}";

    /**
     * 기본 ProductData 생성
     *
     * @return ProductData
     */
    public static ProductData create() {
        return ProductData.of(DEFAULT_JSON_DATA);
    }

    /**
     * 지정된 JSON 데이터로 ProductData 생성
     *
     * @param jsonData JSON 데이터
     * @return ProductData
     */
    public static ProductData createWithJson(String jsonData) {
        return ProductData.of(jsonData);
    }

    /**
     * 미니샵 데이터 생성
     *
     * @return ProductData
     */
    public static ProductData createMiniShopData() {
        return ProductData.of("{\"type\":\"miniShop\",\"name\":\"미니샵 상품\",\"price\":50000}");
    }

    /**
     * 상세 데이터 생성
     *
     * @return ProductData
     */
    public static ProductData createDetailData() {
        return ProductData.of("{\"type\":\"detail\",\"description\":\"상세 설명\",\"specs\":\"사양 정보\"}");
    }

    /**
     * 옵션 데이터 생성
     *
     * @return ProductData
     */
    public static ProductData createOptionData() {
        return ProductData.of("{\"type\":\"option\",\"options\":[{\"name\":\"색상\",\"values\":[\"블랙\",\"화이트\"]}]}");
    }
}
