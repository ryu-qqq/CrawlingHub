package com.ryuqq.crawlinghub.application.task.dto.output;

import java.util.List;

/**
 * MINI_SHOP API 응답 DTO
 *
 * <p>API 응답 예시: {@code docs/output/mini_shop_item.json}
 *
 * <p>포함 정보:
 * - count: 전체 상품 수
 * - items: 상품 목록 (itemNo, itemName 등)
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public record MiniShopOutput(
    int count,
    String indexName,
    GroupResult groupResult,
    List<Item> items
) {
    /**
     * 그룹 결과 (카테고리별 집계)
     */
    public record GroupResult(
        List<CategoryGroup> headerCategoryGroupResult,
        List<CategoryGroup> largeCategoryGroupResult
    ) {}

    /**
     * 카테고리 그룹
     */
    public record CategoryGroup(
        String headerCategoryCode,
        String headerCategoryName,
        String largeCategoryCode,
        String largeCategoryName,
        int categoryCount
    ) {}

    /**
     * 상품 정보
     */
    public record Item(
        Long itemNo,
        List<String> imageUrlList,
        String brandName,
        String landingUrl,
        String name,
        String price,
        List<Tag> tagList,
        String originalPrice,
        String normalPrice,
        String discountRate,
        boolean likeStatus,
        String appDiscountRate,
        String appPrice,
        String likeImageUrl,
        String webSpacingInfo
    ) {}

    /**
     * 상품 태그 정보
     */
    public record Tag(
        String title,
        String textColor,
        String bgColor,
        String borderColor
    ) {}

    /**
     * 전체 상품 수 반환
     */
    public int getTotalCount() {
        return count;
    }

    /**
     * 페이지 수 계산 (pageSize=500 기준)
     */
    public int calculateTotalPages(int pageSize) {
        return (int) Math.ceil((double) count / pageSize);
    }
}
