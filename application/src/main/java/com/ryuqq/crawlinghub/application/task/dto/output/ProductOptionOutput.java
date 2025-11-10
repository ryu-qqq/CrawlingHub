package com.ryuqq.crawlinghub.application.task.dto.output;

import java.util.List;

/**
 * PRODUCT_OPTION API 응답 DTO
 *
 * <p>API 응답 예시: {@code docs/output/product_option.json}
 *
 * <p>포함 정보:
 * - optionNo: 옵션 번호
 * - itemNo: 상품 번호
 * - color: 색상
 * - size: 사이즈
 * - stock: 재고
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public record ProductOptionOutput(
    List<Option> options
) {
    /**
     * 상품 옵션
     */
    public record Option(
        Long optionNo,
        Long itemNo,
        String color,
        String size,
        String shippingType,
        int stock,
        String sizeGuide
    ) {
        /**
         * 재고 있는지 확인
         */
        public boolean hasStock() {
            return stock > 0;
        }
    }

    /**
     * 전체 옵션 수
     */
    public int getTotalOptionCount() {
        return options.size();
    }

    /**
     * 재고 있는 옵션 수
     */
    public int getAvailableOptionCount() {
        return (int) options.stream()
            .filter(Option::hasStock)
            .count();
    }
}
