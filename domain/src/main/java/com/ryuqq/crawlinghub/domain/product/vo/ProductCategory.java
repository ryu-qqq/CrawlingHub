package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 상품 카테고리 정보 VO
 *
 * <p>DETAIL 크롤링에서 추출한 카테고리 정보
 *
 * @param headerCategoryCode 대분류 코드 (W/M/K/L)
 * @param headerCategoryName 대분류명 (여성/남성/키즈/라이프)
 * @param largeCategoryCode 중분류 코드
 * @param largeCategoryName 중분류명
 * @param mediumCategoryCode 소분류 코드
 * @param mediumCategoryName 소분류명
 * @author development-team
 * @since 1.0.0
 */
public record ProductCategory(
        String headerCategoryCode,
        String headerCategoryName,
        String largeCategoryCode,
        String largeCategoryName,
        String mediumCategoryCode,
        String mediumCategoryName) {

    /**
     * 팩토리 메서드
     */
    public static ProductCategory of(
            String headerCategoryCode,
            String headerCategoryName,
            String largeCategoryCode,
            String largeCategoryName,
            String mediumCategoryCode,
            String mediumCategoryName) {
        return new ProductCategory(
                headerCategoryCode,
                headerCategoryName,
                largeCategoryCode,
                largeCategoryName,
                mediumCategoryCode,
                mediumCategoryName);
    }

    /**
     * 전체 카테고리 경로 반환
     *
     * @return "여성 > 가방 > 백팩" 형식
     */
    public String getFullPath() {
        StringBuilder sb = new StringBuilder();
        if (headerCategoryName != null) {
            sb.append(headerCategoryName);
        }
        if (largeCategoryName != null) {
            if (sb.length() > 0) sb.append(" > ");
            sb.append(largeCategoryName);
        }
        if (mediumCategoryName != null) {
            if (sb.length() > 0) sb.append(" > ");
            sb.append(mediumCategoryName);
        }
        return sb.toString();
    }

    /**
     * 여성 카테고리인지 확인
     */
    public boolean isWomen() {
        return "W".equals(headerCategoryCode);
    }

    /**
     * 남성 카테고리인지 확인
     */
    public boolean isMen() {
        return "M".equals(headerCategoryCode);
    }

    /**
     * 키즈 카테고리인지 확인
     */
    public boolean isKids() {
        return "K".equals(headerCategoryCode);
    }

    /**
     * 중분류명 반환 (mediumCategoryName 별칭)
     */
    public String mediumCategory() {
        return mediumCategoryName;
    }
}
