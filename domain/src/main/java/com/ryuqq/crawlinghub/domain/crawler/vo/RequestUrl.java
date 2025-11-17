package com.ryuqq.crawlinghub.domain.crawler.vo;

import com.ryuqq.crawlinghub.domain.crawler.exception.InvalidRequestUrlException;

/**
 * RequestUrl - CrawlerTask 요청 URL Value Object
 *
 * <p>CrawlerTaskType에 따라 URL 형식을 검증하는 Value Object입니다.</p>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>✅ MINISHOP: /searchmini-shop-search 패턴 필수</li>
 *   <li>✅ PRODUCT_DETAIL: /item/{숫자}/detail/top 형식</li>
 *   <li>✅ PRODUCT_OPTION: /auction_products/{숫자}/options 형식</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 (Record 패턴 사용)</li>
 *   <li>✅ 불변성 (Immutable)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public record RequestUrl(String value, CrawlerTaskType taskType) {

    /**
     * Compact Constructor - RequestUrl 생성 시 검증
     *
     * <p>CrawlerTaskType에 따라 URL 형식을 검증합니다.</p>
     *
     * @throws IllegalArgumentException URL이 null이거나 비어있을 때
     * @throws InvalidRequestUrlException URL 형식이 taskType과 맞지 않을 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public RequestUrl {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("RequestUrl은 비어있을 수 없습니다");
        }
        validateByTaskType(value, taskType);
    }

    /**
     * CrawlerTaskType에 따른 URL 형식 검증
     *
     * <p><strong>검증 규칙:</strong></p>
     * <ul>
     *   <li>MINISHOP: /searchmini-shop-search 포함 여부</li>
     *   <li>PRODUCT_DETAIL: /item/{숫자}/detail/top 정규식 매칭</li>
     *   <li>PRODUCT_OPTION: /auction_products/{숫자}/options 정규식 매칭</li>
     * </ul>
     *
     * @param url 검증할 URL
     * @param type CrawlerTaskType
     * @throws InvalidRequestUrlException 형식이 맞지 않을 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    private void validateByTaskType(String url, CrawlerTaskType type) {
        switch (type) {
            case MINISHOP -> {
                if (!url.contains("/searchmini-shop-search")) {
                    throw new InvalidRequestUrlException(
                        "MINISHOP URL은 /searchmini-shop-search 패턴을 포함해야 합니다"
                    );
                }
            }
            case PRODUCT_DETAIL -> {
                if (!url.matches(".*/item/\\d+/detail/top.*")) {
                    throw new InvalidRequestUrlException(
                        "PRODUCT_DETAIL URL은 /item/{숫자}/detail/top 형식이어야 합니다"
                    );
                }
            }
            case PRODUCT_OPTION -> {
                if (!url.matches(".*/auction_products/\\d+/options.*")) {
                    throw new InvalidRequestUrlException(
                        "PRODUCT_OPTION URL은 /auction_products/{숫자}/options 형식이어야 합니다"
                    );
                }
            }
        }
    }

    /**
     * 정적 팩토리 메서드 - RequestUrl 생성
     *
     * @param value URL 값
     * @param taskType CrawlerTaskType
     * @return RequestUrl 인스턴스
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static RequestUrl of(String value, CrawlerTaskType taskType) {
        return new RequestUrl(value, taskType);
    }
}
