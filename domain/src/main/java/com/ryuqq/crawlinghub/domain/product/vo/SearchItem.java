package com.ryuqq.crawlinghub.domain.product.vo;

import java.util.List;

/**
 * Search API 상품 아이템 VO
 *
 * <p>SEARCH 크롤링 응답에서 파싱된 상품 정보 (무한스크롤 방식)
 *
 * <p><strong>MiniShopItem과의 차이점</strong>:
 *
 * <ul>
 *   <li>normalPrice, appPrice, appDiscountRate 필드가 없음 (nullable)
 *   <li>shippingType 필드로 무료배송 여부 판단
 *   <li>가격 필드가 문자열로 전달됨
 * </ul>
 *
 * <p><strong>무료배송 판단</strong>:
 *
 * <ul>
 *   <li>shippingType == "DOMESTIC" → 무료배송
 *   <li>tagList에 "무료배송" 포함 → 무료배송
 * </ul>
 *
 * @param itemNo 상품 번호 (외부 사이트 상품 ID)
 * @param imageUrlList 이미지 URL 목록
 * @param brandName 브랜드명
 * @param name 상품명
 * @param price 판매가 문자열 (예: "286,100")
 * @param originalPrice 원가 문자열 (예: "682,000")
 * @param discountRate 할인율 문자열 (예: "58")
 * @param tagList 태그 목록
 * @param shippingType 배송 타입 (DOMESTIC: 무료배송)
 * @author development-team
 * @since 1.0.0
 */
public record SearchItem(
        Long itemNo,
        List<String> imageUrlList,
        String brandName,
        String name,
        String price,
        String originalPrice,
        String discountRate,
        List<ItemTag> tagList,
        String shippingType) {

    private static final String FREE_SHIPPING_TYPE = "DOMESTIC";
    private static final String FREE_SHIPPING_TAG = "무료배송";

    public SearchItem {
        if (itemNo == null) {
            throw new IllegalArgumentException("itemNo는 필수입니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 필수입니다.");
        }
        // 방어적 복사
        if (imageUrlList == null) {
            imageUrlList = List.of();
        } else {
            imageUrlList = List.copyOf(imageUrlList);
        }
        if (tagList == null) {
            tagList = List.of();
        } else {
            tagList = List.copyOf(tagList);
        }
    }

    /**
     * MiniShopItem으로 변환
     *
     * <p>Search API 응답을 기존 MiniShopItem 형식으로 변환하여 기존 로직 재사용
     *
     * <p><strong>변환 규칙</strong>:
     *
     * <ul>
     *   <li>normalPrice = originalPrice (Search에는 normalPrice 없음)
     *   <li>appPrice = price (Search에는 appPrice 없음)
     *   <li>appDiscountRate = discountRate (Search에는 앱 전용 할인 없음)
     *   <li>무료배송: shippingType == DOMESTIC 또는 태그에 포함
     * </ul>
     *
     * @return MiniShopItem
     */
    public MiniShopItem toMiniShopItem() {
        int parsedPrice = MiniShopItem.parsePrice(price);
        int parsedOriginalPrice = MiniShopItem.parsePrice(originalPrice);
        int parsedDiscountRate = MiniShopItem.parseDiscountRate(discountRate);

        // Search에는 normalPrice, appPrice, appDiscountRate가 없으므로 기본값 사용
        int normalPrice = parsedOriginalPrice;
        int appPrice = parsedPrice;
        int appDiscountRate = parsedDiscountRate;

        // 무료배송 태그 확인 및 추가
        List<ItemTag> finalTagList = ensureFreeShippingTag(tagList);

        return new MiniShopItem(
                itemNo,
                imageUrlList,
                brandName,
                name,
                parsedPrice,
                parsedOriginalPrice,
                normalPrice,
                parsedDiscountRate,
                appDiscountRate,
                appPrice,
                finalTagList);
    }

    /**
     * 무료배송 여부 확인
     *
     * @return shippingType이 DOMESTIC이거나 태그에 무료배송이 있으면 true
     */
    public boolean isFreeShipping() {
        if (FREE_SHIPPING_TYPE.equalsIgnoreCase(shippingType)) {
            return true;
        }
        return hasTagWithTitle(FREE_SHIPPING_TAG);
    }

    /**
     * 특정 제목의 태그 존재 여부 확인
     *
     * @param title 태그 제목
     * @return 해당 태그가 있으면 true
     */
    private boolean hasTagWithTitle(String title) {
        if (tagList == null || tagList.isEmpty()) {
            return false;
        }
        return tagList.stream().anyMatch(tag -> tag.title() != null && tag.title().contains(title));
    }

    /**
     * 무료배송 태그가 없으면 추가
     *
     * <p>shippingType이 DOMESTIC인데 태그에 무료배송이 없으면 추가
     *
     * @param originalTagList 원본 태그 목록
     * @return 무료배송 태그가 포함된 목록
     */
    private List<ItemTag> ensureFreeShippingTag(List<ItemTag> originalTagList) {
        if (!isFreeShipping()) {
            return originalTagList;
        }

        // 이미 무료배송 태그가 있으면 그대로 반환
        if (hasTagWithTitle(FREE_SHIPPING_TAG)) {
            return originalTagList;
        }

        // 무료배송 태그 추가
        java.util.List<ItemTag> mutableList = new java.util.ArrayList<>(originalTagList);
        mutableList.add(ItemTag.ofTitle(FREE_SHIPPING_TAG));
        return List.copyOf(mutableList);
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
     * 팩토리 메서드 - JSON 파싱 결과로부터 생성
     *
     * @param itemNo 상품 번호
     * @param imageUrlList 이미지 URL 목록
     * @param brandName 브랜드명
     * @param name 상품명
     * @param price 판매가 문자열
     * @param originalPrice 원가 문자열
     * @param discountRate 할인율 문자열
     * @param tagList 태그 목록
     * @param shippingType 배송 타입
     * @return SearchItem
     */
    public static SearchItem of(
            Long itemNo,
            List<String> imageUrlList,
            String brandName,
            String name,
            String price,
            String originalPrice,
            String discountRate,
            List<ItemTag> tagList,
            String shippingType) {
        return new SearchItem(
                itemNo,
                imageUrlList,
                brandName,
                name,
                price,
                originalPrice,
                discountRate,
                tagList,
                shippingType);
    }
}
