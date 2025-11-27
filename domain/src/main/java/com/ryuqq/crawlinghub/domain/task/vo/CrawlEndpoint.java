package com.ryuqq.crawlinghub.domain.task.vo;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * CrawlEndpoint Value Object
 *
 * <p>크롤링 대상 API 엔드포인트 정보를 담는 불변 객체
 *
 * <p><strong>MUSTIT API 엔드포인트</strong>:
 *
 * <ul>
 *   <li>미니샵 목록: /mustit-api/facade-api/v1/searchmini-shop-search
 *   <li>상품 상세: /mustit-api/facade-api/v1/item/{item_no}/detail/top
 *   <li>상품 옵션: /mustit-api/legacy-api/v1/auction_products/{item_no}/options
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record CrawlEndpoint(String baseUrl, String path, Map<String, String> queryParams) {

    private static final String MUSTIT_BASE_URL = "https://m.web.mustit.co.kr";

    public CrawlEndpoint {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("CrawlEndpoint baseUrl은 null이거나 빈 값일 수 없습니다.");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("CrawlEndpoint path는 null이거나 빈 값일 수 없습니다.");
        }
        queryParams = queryParams == null ? Map.of() : Map.copyOf(queryParams);
    }

    /**
     * 미니샵 상품 목록(메타데이터) 엔드포인트 생성
     *
     * @param sellerId 셀러 ID
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forMeta(Long sellerId) {
        return forMiniShopList(sellerId, 1, 1);
    }

    /**
     * 미니샵 상품 목록 엔드포인트 생성
     *
     * @param sellerId 셀러 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forMiniShopList(Long sellerId, int page, int pageSize) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/mustit-api/facade-api/v1/searchmini-shop-search",
                Map.of(
                        "sellerId", String.valueOf(sellerId),
                        "pageNo", String.valueOf(page),
                        "pageSize", String.valueOf(pageSize),
                        "order", "LATEST"));
    }

    /**
     * 상품 상세 엔드포인트 생성
     *
     * @param itemNo 상품 번호
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forProductDetail(Long itemNo) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/mustit-api/facade-api/v1/item/" + itemNo + "/detail/top",
                Map.of());
    }

    /**
     * 상품 옵션 엔드포인트 생성
     *
     * @param itemNo 상품 번호
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forProductOption(Long itemNo) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/mustit-api/legacy-api/v1/auction_products/" + itemNo + "/options",
                Map.of());
    }

    /**
     * 전체 URL 생성
     *
     * @return baseUrl + path + queryString
     */
    public String toFullUrl() {
        if (queryParams.isEmpty()) {
            return baseUrl + path;
        }

        String queryString =
                queryParams.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining("&"));

        return baseUrl + path + "?" + queryString;
    }
}
