package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 상품 가격 정보 VO
 *
 * <p>상품의 다양한 가격 정보를 묶어서 관리합니다.
 *
 * @param price 판매가
 * @param originalPrice 원가
 * @param normalPrice 정상가
 * @param appPrice 앱 가격
 * @param discountRate 할인율 (%)
 * @param appDiscountRate 앱 할인율 (%)
 * @author development-team
 * @since 1.0.0
 */
public record ProductPrice(
        int price,
        int originalPrice,
        int normalPrice,
        int appPrice,
        int discountRate,
        int appDiscountRate) {

    public ProductPrice {
        if (price < 0) {
            throw new IllegalArgumentException("price는 0 이상이어야 합니다.");
        }
        if (originalPrice < 0) {
            throw new IllegalArgumentException("originalPrice는 0 이상이어야 합니다.");
        }
        if (normalPrice < 0) {
            throw new IllegalArgumentException("normalPrice는 0 이상이어야 합니다.");
        }
        if (appPrice < 0) {
            throw new IllegalArgumentException("appPrice는 0 이상이어야 합니다.");
        }
        if (discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("discountRate는 0~100 사이여야 합니다.");
        }
        if (appDiscountRate < 0 || appDiscountRate > 100) {
            throw new IllegalArgumentException("appDiscountRate는 0~100 사이여야 합니다.");
        }
    }

    /** 기본 팩토리 메서드 */
    public static ProductPrice of(
            int price,
            int originalPrice,
            int normalPrice,
            int appPrice,
            int discountRate,
            int appDiscountRate) {
        return new ProductPrice(
                price, originalPrice, normalPrice, appPrice, discountRate, appDiscountRate);
    }

    /** MiniShopItem에서 생성하는 팩토리 메서드 */
    public static ProductPrice fromMiniShopItem(MiniShopItem item) {
        return new ProductPrice(
                item.price(),
                item.originalPrice(),
                item.normalPrice(),
                item.appPrice(),
                item.discountRate(),
                item.appDiscountRate());
    }

    /**
     * 가격 변경 여부 확인
     *
     * @param other 비교 대상
     * @return 가격 관련 필드 중 하나라도 다르면 true
     */
    public boolean hasPriceChange(ProductPrice other) {
        if (other == null) {
            return true;
        }
        return this.price != other.price
                || this.originalPrice != other.originalPrice
                || this.normalPrice != other.normalPrice
                || this.appPrice != other.appPrice
                || this.discountRate != other.discountRate
                || this.appDiscountRate != other.appDiscountRate;
    }

    /** 할인 중인지 확인 */
    public boolean hasDiscount() {
        return discountRate > 0;
    }

    /** 앱 전용 추가 할인이 있는지 확인 */
    public boolean hasAppExclusiveDiscount() {
        return appDiscountRate > discountRate;
    }

    /** 판매가 반환 (price 별칭) */
    public int sellingPrice() {
        return price;
    }

    /** 할인가 반환 (앱 가격 또는 판매가) */
    public int discountPrice() {
        return appPrice > 0 ? appPrice : price;
    }
}
