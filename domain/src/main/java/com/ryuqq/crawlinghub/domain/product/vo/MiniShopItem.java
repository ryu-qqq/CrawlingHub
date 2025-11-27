package com.ryuqq.crawlinghub.domain.product.vo;

import java.util.List;

/**
 * 미니샵 상품 아이템 VO
 *
 * <p>MINI_SHOP 크롤링 응답에서 파싱된 상품 정보
 *
 * <p><strong>가격 필드</strong>: 문자열("1,075,000")에서 정수(1075000)로 변환된 값
 *
 * @param itemNo 상품 번호 (외부 사이트 상품 ID)
 * @param imageUrlList 이미지 URL 목록
 * @param brandName 브랜드명
 * @param name 상품명
 * @param price 판매가 (정수)
 * @param originalPrice 원가 (정수)
 * @param normalPrice 정상가 (정수)
 * @param discountRate 할인율 (%)
 * @param appDiscountRate 앱 할인율 (%)
 * @param appPrice 앱 가격 (정수)
 * @param tagList 태그 목록
 * @author development-team
 * @since 1.0.0
 */
public record MiniShopItem(
        Long itemNo,
        List<String> imageUrlList,
        String brandName,
        String name,
        int price,
        int originalPrice,
        int normalPrice,
        int discountRate,
        int appDiscountRate,
        int appPrice,
        List<ItemTag> tagList) {

    public MiniShopItem {
        if (itemNo == null) {
            throw new IllegalArgumentException("itemNo는 필수입니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 필수입니다.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("price는 0 이상이어야 합니다.");
        }
        if (originalPrice < 0) {
            throw new IllegalArgumentException("originalPrice는 0 이상이어야 합니다.");
        }
        if (normalPrice < 0) {
            throw new IllegalArgumentException("normalPrice는 0 이상이어야 합니다.");
        }
        if (discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("discountRate는 0~100 사이여야 합니다.");
        }
        if (appDiscountRate < 0 || appDiscountRate > 100) {
            throw new IllegalArgumentException("appDiscountRate는 0~100 사이여야 합니다.");
        }
        if (appPrice < 0) {
            throw new IllegalArgumentException("appPrice는 0 이상이어야 합니다.");
        }
        if (imageUrlList == null) {
            imageUrlList = List.of();
        }
        if (tagList == null) {
            tagList = List.of();
        }
    }

    /**
     * 가격 문자열을 정수로 변환
     *
     * <p>"1,075,000" → 1075000
     *
     * @param priceString 가격 문자열 (쉼표 포함)
     * @return 정수 가격 (변환 실패 시 0)
     */
    public static int parsePrice(String priceString) {
        if (priceString == null || priceString.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(priceString.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 할인율 문자열을 정수로 변환
     *
     * @param discountRateString 할인율 문자열 (예: "49")
     * @return 정수 할인율 (변환 실패 시 0)
     */
    public static int parseDiscountRate(String discountRateString) {
        if (discountRateString == null || discountRateString.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(discountRateString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 문자열 가격들을 정수로 변환하여 생성하는 팩토리 메서드
     *
     * @param itemNo 상품 번호
     * @param imageUrlList 이미지 URL 목록
     * @param brandName 브랜드명
     * @param name 상품명
     * @param priceString 판매가 문자열 (예: "545,600")
     * @param originalPriceString 원가 문자열 (예: "1,075,000")
     * @param normalPriceString 정상가 문자열 (예: "1,075,000")
     * @param discountRateString 할인율 문자열 (예: "49")
     * @param appDiscountRateString 앱 할인율 문자열 (예: "49")
     * @param appPriceString 앱 가격 문자열 (예: "545,600")
     * @param tagList 태그 목록
     * @return MiniShopItem
     */
    public static MiniShopItem fromStrings(
            Long itemNo,
            List<String> imageUrlList,
            String brandName,
            String name,
            String priceString,
            String originalPriceString,
            String normalPriceString,
            String discountRateString,
            String appDiscountRateString,
            String appPriceString,
            List<ItemTag> tagList) {
        return new MiniShopItem(
                itemNo,
                imageUrlList,
                brandName,
                name,
                parsePrice(priceString),
                parsePrice(originalPriceString),
                parsePrice(normalPriceString),
                parseDiscountRate(discountRateString),
                parseDiscountRate(appDiscountRateString),
                parsePrice(appPriceString),
                tagList);
    }

    /**
     * 대표 이미지 URL 반환
     *
     * @return 첫 번째 이미지 URL (없으면 null)
     */
    public String mainImageUrl() {
        return imageUrlList != null && !imageUrlList.isEmpty() ? imageUrlList.get(0) : null;
    }

    /**
     * 할인 상품 여부 확인
     *
     * @return 할인율이 0보다 크면 true
     */
    public boolean hasDiscount() {
        return discountRate > 0;
    }

    /**
     * 앱 전용 할인 여부 확인
     *
     * @return 앱 할인율이 일반 할인율보다 크면 true
     */
    public boolean hasAppExclusiveDiscount() {
        return appDiscountRate > discountRate;
    }

    /**
     * 이미지 URL 목록 반환
     *
     * @return 이미지 URL 목록
     */
    public List<String> imageUrls() {
        return imageUrlList;
    }

    /**
     * ProductPrice VO로 변환
     *
     * @return ProductPrice
     */
    public ProductPrice toProductPrice() {
        return ProductPrice.of(
                price,
                originalPrice,
                normalPrice,
                appPrice,
                discountRate,
                appDiscountRate);
    }

    /**
     * 무료배송 태그 여부 확인
     *
     * @return 무료배송 태그가 있으면 true
     */
    public boolean hasFreeShippingTag() {
        if (tagList == null || tagList.isEmpty()) {
            return false;
        }
        return tagList.stream()
                .anyMatch(tag -> tag.title() != null
                        && (tag.title().contains("무료배송") || tag.title().contains("FREE")));
    }
}
