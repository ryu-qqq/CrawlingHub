package com.ryuqq.crawlinghub.domain.task;

/**
 * RequestUrl Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class RequestUrlFixture {

    private static final String DEFAULT_URL = "https://www.mustIt.co.kr/product/list";
    private static final String DETAIL_URL = "https://www.mustIt.co.kr/product/detail/12345";
    private static final String API_URL = "https://api.mustIt.co.kr/v1/products";

    /**
     * 기본 RequestUrl 생성
     *
     * @return RequestUrl
     */
    public static RequestUrl create() {
        return RequestUrl.of(DEFAULT_URL);
    }

    /**
     * 지정된 URL로 RequestUrl 생성
     *
     * @param url URL 문자열
     * @return RequestUrl
     */
    public static RequestUrl createWithUrl(String url) {
        return RequestUrl.of(url);
    }

    /**
     * 상품 목록 URL 생성
     *
     * @return RequestUrl
     */
    public static RequestUrl createListUrl() {
        return RequestUrl.of(DEFAULT_URL);
    }

    /**
     * 상품 상세 URL 생성
     *
     * @return RequestUrl
     */
    public static RequestUrl createDetailUrl() {
        return RequestUrl.of(DETAIL_URL);
    }

    /**
     * API URL 생성
     *
     * @return RequestUrl
     */
    public static RequestUrl createApiUrl() {
        return RequestUrl.of(API_URL);
    }

    /**
     * 페이지 번호가 포함된 URL 생성
     *
     * @param pageNumber 페이지 번호
     * @return RequestUrl
     */
    public static RequestUrl createWithPage(int pageNumber) {
        return RequestUrl.of(DEFAULT_URL + "?page=" + pageNumber);
    }
}
