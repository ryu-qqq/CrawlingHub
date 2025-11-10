package com.ryuqq.crawlinghub.application.task.dto.output;

import java.util.List;

/**
 * PRODUCT_DETAIL API 응답 DTO
 *
 * <p>API 응답 예시: {@code docs/output/product_info.json}
 *
 * <p>포함 정보:
 * - 상품 기본 정보 (itemNo, itemName, brandName 등)
 * - 이미지 목록 (ProductBannersModule)
 * - 상품 상세 정보 (ProductInfoModule)
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public record ProductDetailOutput(
    List<Module> moduleList
) {
    /**
     * 모듈 (다양한 타입의 정보 블록)
     */
    public record Module(
        String type,
        ModuleData data
    ) {}

    /**
     * 모듈 데이터
     */
    public record ModuleData(
        // ProductBannersModule
        List<String> images,
        List<Object> timesales,

        // ProductInfoModule
        Long sellerNo,
        String sellerId,
        Long itemNo,
        String itemName,
        String brandName,
        String brandNameKr,
        Integer brandCode,
        String headerCategoryCode,
        String headerCategory,
        String largeCategoryCode,
        String largeCategory,
        String mediumCategoryCode,
        String mediumCategory,
        String smallCategoryCode,
        String smallCategory,
        Integer salePrice,
        Integer originalPrice,
        Integer discountRate
    ) {}

    /**
     * 상품 기본 정보 추출
     */
    public ModuleData getProductInfo() {
        return moduleList.stream()
            .filter(module -> "ProductInfoModule".equals(module.type()))
            .map(Module::data)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("ProductInfoModule을 찾을 수 없습니다"));
    }

    /**
     * 이미지 목록 추출
     */
    public List<String> getImages() {
        return moduleList.stream()
            .filter(module -> "ProductBannersModule".equals(module.type()))
            .map(Module::data)
            .map(ModuleData::images)
            .findFirst()
            .orElse(List.of());
    }
}
