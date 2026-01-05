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
 *   <li>검색 목록 (SEARCH): /mustit-api/facade-api/v1/search/items (스케줄러 초기 트리거)
 *   <li>미니샵 목록 (MINI_SHOP): /mustit-api/facade-api/v1/searchmini-shop-search
 *   <li>상품 상세 (DETAIL): /mustit-api/facade-api/v1/item/{item_no}/detail/top
 *   <li>상품 옵션 (OPTION): /mustit-api/legacy-api/v1/auction_products/{item_no}/options
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
     * @param mustItSellerName 머스트잇 셀러명 (API 조회 시 필요)
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forMeta(String mustItSellerName) {
        return forMiniShopList(mustItSellerName, 1, 1);
    }

    /**
     * 미니샵 상품 목록 엔드포인트 생성 (MINI_SHOP 타입 전용)
     *
     * <p>미니샵 페이지 크롤링용. 스케줄러 초기 트리거에는 {@link #forSearchItems}를 사용하세요.
     *
     * @param mustItSellerName 머스트잇 셀러명 (API 조회 시 필요)
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return CrawlEndpoint
     * @see #forSearchItems(String, int)
     */
    public static CrawlEndpoint forMiniShopList(String mustItSellerName, int page, int pageSize) {
        if (mustItSellerName == null || mustItSellerName.isBlank()) {
            throw new IllegalArgumentException("mustItSellerName은 null이거나 빈 값일 수 없습니다.");
        }
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/mustit-api/facade-api/v1/searchmini-shop-search",
                Map.of(
                        "sellerId",
                        mustItSellerName,
                        "pageNo",
                        String.valueOf(page),
                        "pageSize",
                        String.valueOf(pageSize),
                        "order",
                        "LATEST"));
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
     * 검색 API 초기 엔드포인트 생성 (SEARCH 타입, 스케줄러 초기 트리거용)
     *
     * <p>셀러명을 keyword로 검색하는 Search API 엔드포인트를 생성합니다.
     *
     * <p><strong>파라미터</strong>:
     *
     * <ul>
     *   <li>keyword: 셀러명 (검색어)
     *   <li>sort: POPULAR2 (인기순)
     *   <li>f: us:NEW,lwp:Y (필터 조건)
     *   <li>pageNo: 페이지 번호
     * </ul>
     *
     * <p><strong>nid, uid, adId, beforeItemType</strong>은 CrawlContext.buildSearchEndpoint()에서
     * UserAgent 쿠키 정보로 추가됩니다.
     *
     * @param keyword 검색어 (셀러명)
     * @param pageNo 페이지 번호
     * @return CrawlEndpoint
     * @see #forSearchApi(String)
     */
    public static CrawlEndpoint forSearchItems(String keyword, int pageNo) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("keyword는 null이거나 빈 값일 수 없습니다.");
        }
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/mustit-api/facade-api/v1/search/items",
                Map.of(
                        "keyword",
                        keyword,
                        "sort",
                        "POPULAR2",
                        "f",
                        "us:NEW,lwp:Y",
                        "pageNo",
                        String.valueOf(pageNo)));
    }

    /**
     * Search API 엔드포인트 생성 (전체 URL 직접 지정)
     *
     * <p>무한스크롤 방식의 Search API는 nextApiUrl을 그대로 사용합니다.
     *
     * @param fullUrl 전체 URL (nextApiUrl)
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forSearchApi(String fullUrl) {
        if (fullUrl == null || fullUrl.isBlank()) {
            throw new IllegalArgumentException("Search API URL은 null이거나 빈 값일 수 없습니다.");
        }
        // URL 파싱하여 baseUrl과 path 분리
        int pathStart = fullUrl.indexOf("/", fullUrl.indexOf("://") + 3);
        if (pathStart == -1) {
            return new CrawlEndpoint(fullUrl, "/", Map.of());
        }
        String baseUrl = fullUrl.substring(0, pathStart);
        String pathWithQuery = fullUrl.substring(pathStart);
        int queryStart = pathWithQuery.indexOf("?");
        if (queryStart == -1) {
            return new CrawlEndpoint(baseUrl, pathWithQuery, Map.of());
        }
        String path = pathWithQuery.substring(0, queryStart);
        String queryString = pathWithQuery.substring(queryStart + 1);
        Map<String, String> queryParams = parseQueryString(queryString);
        return new CrawlEndpoint(baseUrl, path, queryParams);
    }

    /**
     * 쿼리 문자열 파싱
     *
     * @param queryString 쿼리 문자열 (key1=value1&key2=value2)
     * @return 파싱된 쿼리 파라미터 맵
     */
    private static Map<String, String> parseQueryString(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return Map.of();
        }
        return java.util.Arrays.stream(queryString.split("&"))
                .filter(param -> param.contains("="))
                .collect(
                        Collectors.toMap(
                                param -> param.substring(0, param.indexOf("=")),
                                param -> param.substring(param.indexOf("=") + 1),
                                (v1, v2) -> v2));
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

    /**
     * 머스트잇 셀러명 조회
     *
     * <p>queryParams에서 셀러명을 조회합니다.
     *
     * <p><strong>조회 순서</strong>:
     *
     * <ol>
     *   <li>sellerId: META, MINI_SHOP 타입에서 사용
     *   <li>keyword: SEARCH 타입에서 사용 (검색어가 셀러명)
     * </ol>
     *
     * @return 머스트잇 셀러명 (없으면 null)
     */
    public String getMustItSellerName() {
        // META, MINI_SHOP 타입: sellerId 파라미터 사용
        String sellerId = queryParams.get("sellerId");
        if (sellerId != null && !sellerId.isBlank()) {
            return sellerId;
        }
        // SEARCH 타입: keyword 파라미터가 셀러명
        return queryParams.get("keyword");
    }
}
