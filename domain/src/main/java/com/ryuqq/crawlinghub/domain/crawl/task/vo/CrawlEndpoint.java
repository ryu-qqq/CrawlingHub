package com.ryuqq.crawlinghub.domain.crawl.task.vo;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * CrawlEndpoint Value Object
 *
 * <p>크롤링 대상 API 엔드포인트 정보를 담는 불변 객체
 *
 * <p><strong>생성 패턴</strong>:
 *
 * <ul>
 *   <li>{@code forMiniShopMeta()} - 미니샵 메타 정보 엔드포인트
 *   <li>{@code forMiniShopList()} - 미니샵 상품 목록 엔드포인트
 *   <li>{@code forProductDetail()} - 상품 상세 엔드포인트
 *   <li>{@code forProductOption()} - 상품 옵션 엔드포인트
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record CrawlEndpoint(
        String baseUrl,
        String path,
        Map<String, String> queryParams
) {

    private static final String MUSTIT_BASE_URL = "https://api.mustit.co.kr";

    /**
     * Compact Constructor (검증 + 불변 보장)
     */
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
     * 미니샵 메타 정보 엔드포인트 생성
     *
     * @param sellerId 셀러 ID
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forMiniShopMeta(Long sellerId) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/api/v1/minishop/" + sellerId + "/meta",
                Map.of()
        );
    }

    /**
     * 미니샵 상품 목록 엔드포인트 생성
     *
     * @param sellerId 셀러 ID
     * @param page     페이지 번호
     * @param size     페이지 크기
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forMiniShopList(Long sellerId, int page, int size) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/api/v1/minishop/" + sellerId + "/products",
                Map.of("page", String.valueOf(page), "size", String.valueOf(size))
        );
    }

    /**
     * 상품 상세 엔드포인트 생성
     *
     * @param productId 상품 ID
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forProductDetail(Long productId) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/api/v1/product/" + productId,
                Map.of()
        );
    }

    /**
     * 상품 옵션 엔드포인트 생성
     *
     * @param productId 상품 ID
     * @return CrawlEndpoint
     */
    public static CrawlEndpoint forProductOption(Long productId) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/api/v1/product/" + productId + "/option",
                Map.of()
        );
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

        String queryString = queryParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return baseUrl + path + "?" + queryString;
    }
}
