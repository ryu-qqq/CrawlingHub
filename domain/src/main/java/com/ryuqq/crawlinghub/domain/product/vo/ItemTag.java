package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 상품 태그 VO
 *
 * <p>미니샵 상품의 태그 정보 (예: 무료배송, 할인 등)
 *
 * @param title 태그 제목 (예: "무료배송")
 * @param textColor 텍스트 색상 (예: "#888888")
 * @param bgColor 배경 색상 (예: "#ffffff")
 * @param borderColor 테두리 색상 (예: "#dddddd")
 * @author development-team
 * @since 1.0.0
 */
public record ItemTag(
        String title,
        String textColor,
        String bgColor,
        String borderColor) {

    public ItemTag {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("태그 title은 필수입니다.");
        }
    }

    /**
     * 팩토리 메서드
     *
     * @param title 태그 제목
     * @param textColor 텍스트 색상
     * @param bgColor 배경 색상
     * @param borderColor 테두리 색상
     * @return ItemTag
     */
    public static ItemTag of(String title, String textColor, String bgColor, String borderColor) {
        return new ItemTag(title, textColor, bgColor, borderColor);
    }

    /**
     * 제목만으로 생성하는 간단 팩토리 메서드
     *
     * @param title 태그 제목
     * @return ItemTag
     */
    public static ItemTag ofTitle(String title) {
        return new ItemTag(title, null, null, null);
    }
}
