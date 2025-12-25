package com.ryuqq.crawlinghub.domain.task.vo;

/**
 * CrawlTask 유형 Enum
 *
 * <p><strong>크롤링 유형</strong>:
 *
 * <ul>
 *   <li>{@code META} - 미니샵 메타 정보 크롤링
 *   <li>{@code MINI_SHOP} - 미니샵 상품 목록 크롤링
 *   <li>{@code DETAIL} - 상품 상세 정보 크롤링
 *   <li>{@code OPTION} - 상품 옵션 정보 크롤링
 *   <li>{@code SEARCH} - 검색 결과 크롤링
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CrawlTaskType {

    /** 미니샵 메타 정보 크롤링 */
    META("미니샵 메타 정보"),

    /** 미니샵 상품 목록 크롤링 */
    MINI_SHOP("미니샵 상품 목록"),

    /** 상품 상세 정보 크롤링 */
    DETAIL("상품 상세 정보"),

    /** 상품 옵션 정보 크롤링 */
    OPTION("상품 옵션 정보"),

    /** 검색 결과 크롤링 */
    SEARCH("검색 결과");

    private final String description;

    CrawlTaskType(String description) {
        this.description = description;
    }

    /**
     * 유형 설명 반환
     *
     * @return 유형 설명
     */
    public String getDescription() {
        return description;
    }
}
