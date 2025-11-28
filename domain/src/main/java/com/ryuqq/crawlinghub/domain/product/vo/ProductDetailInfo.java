package com.ryuqq.crawlinghub.domain.product.vo;

import java.util.List;

/**
 * 상품 상세 정보 VO
 *
 * <p>DETAIL 크롤링 응답에서 파싱된 상품 상세 정보. ProductInfoModule, ShippingModule, ProductDetailInfoModule 등에서
 * 추출.
 *
 * @param sellerNo 판매자 번호
 * @param sellerId 판매자 ID
 * @param itemNo 상품 번호
 * @param itemName 상품명
 * @param brandName 브랜드명 (영문)
 * @param brandNameKr 브랜드명 (한글)
 * @param brandCode 브랜드 코드
 * @param category 카테고리 정보
 * @param normalPrice 정상가
 * @param sellingPrice 판매가
 * @param discountPrice 할인가
 * @param discountRate 할인율 (%)
 * @param stock 재고
 * @param isSoldOut 품절 여부
 * @param shipping 배송 정보
 * @param bannerImages 배너 이미지 URL 목록
 * @param detailImages 상세 이미지 URL 목록 (descriptionMarkUp에서 추출)
 * @param originCountry 원산지
 * @param itemStatus 상품 상태 (새상품, 중고 등)
 * @author development-team
 * @since 1.0.0
 */
public record ProductDetailInfo(
        long sellerNo,
        String sellerId,
        long itemNo,
        String itemName,
        String brandName,
        String brandNameKr,
        long brandCode,
        ProductCategory category,
        int normalPrice,
        int sellingPrice,
        int discountPrice,
        int discountRate,
        int stock,
        boolean isSoldOut,
        ShippingInfo shipping,
        List<String> bannerImages,
        List<String> detailImages,
        String originCountry,
        String itemStatus) {

    public ProductDetailInfo {
        if (sellerNo <= 0) {
            throw new IllegalArgumentException("sellerNo는 양수여야 합니다.");
        }
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("sellerId는 필수입니다.");
        }
        if (itemNo <= 0) {
            throw new IllegalArgumentException("itemNo는 양수여야 합니다.");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("itemName은 필수입니다.");
        }
        if (normalPrice < 0) {
            throw new IllegalArgumentException("normalPrice는 0 이상이어야 합니다.");
        }
        if (sellingPrice < 0) {
            throw new IllegalArgumentException("sellingPrice는 0 이상이어야 합니다.");
        }
        if (discountPrice < 0) {
            throw new IllegalArgumentException("discountPrice는 0 이상이어야 합니다.");
        }
        if (discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("discountRate는 0~100 사이여야 합니다.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock은 0 이상이어야 합니다.");
        }
        // 방어적 복사 - SpotBugs EI2 경고 수정
        if (bannerImages == null) {
            bannerImages = List.of();
        } else {
            bannerImages = List.copyOf(bannerImages);
        }
        if (detailImages == null) {
            detailImages = List.of();
        } else {
            detailImages = List.copyOf(detailImages);
        }
        if (originCountry == null) {
            originCountry = "";
        }
        if (itemStatus == null) {
            itemStatus = "";
        }
    }

    /** 빌더 시작 */
    public static Builder builder() {
        return new Builder();
    }

    /** 모든 이미지 URL 반환 (배너 + 상세) */
    public List<String> getAllImageUrls() {
        if (bannerImages.isEmpty() && detailImages.isEmpty()) {
            return List.of();
        }
        return java.util.stream.Stream.concat(bannerImages.stream(), detailImages.stream())
                .toList();
    }

    /** 대표 이미지 URL 반환 */
    public String getMainImageUrl() {
        if (!bannerImages.isEmpty()) {
            return bannerImages.getFirst();
        }
        if (!detailImages.isEmpty()) {
            return detailImages.getFirst();
        }
        return null;
    }

    /** 할인 상품인지 확인 */
    public boolean hasDiscount() {
        return discountRate > 0;
    }

    /** 가격 변경 여부 확인 */
    public boolean hasPriceChange(ProductDetailInfo other) {
        if (other == null) {
            return true;
        }
        return this.normalPrice != other.normalPrice
                || this.sellingPrice != other.sellingPrice
                || this.discountPrice != other.discountPrice
                || this.discountRate != other.discountRate;
    }

    /** 재고 변경 여부 확인 */
    public boolean hasStockChange(ProductDetailInfo other) {
        if (other == null) {
            return true;
        }
        return this.stock != other.stock || this.isSoldOut != other.isSoldOut;
    }

    /** ProductPrice VO로 변환 */
    public ProductPrice toProductPrice() {
        return ProductPrice.of(
                sellingPrice, normalPrice, normalPrice, discountPrice, discountRate, discountRate);
    }

    /** Builder 클래스 */
    public static class Builder {
        private long sellerNo;
        private String sellerId;
        private long itemNo;
        private String itemName;
        private String brandName = "";
        private String brandNameKr = "";
        private long brandCode;
        private ProductCategory category;
        private int normalPrice;
        private int sellingPrice;
        private int discountPrice;
        private int discountRate;
        private int stock;
        private boolean isSoldOut;
        private ShippingInfo shipping;
        private List<String> bannerImages = List.of();
        private List<String> detailImages = List.of();
        private String originCountry = "";
        private String itemStatus = "";

        public Builder sellerNo(long sellerNo) {
            this.sellerNo = sellerNo;
            return this;
        }

        public Builder sellerId(String sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder itemNo(long itemNo) {
            this.itemNo = itemNo;
            return this;
        }

        public Builder itemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public Builder brandName(String brandName) {
            this.brandName = brandName;
            return this;
        }

        public Builder brandNameKr(String brandNameKr) {
            this.brandNameKr = brandNameKr;
            return this;
        }

        public Builder brandCode(long brandCode) {
            this.brandCode = brandCode;
            return this;
        }

        public Builder category(ProductCategory category) {
            this.category = category;
            return this;
        }

        public Builder normalPrice(int normalPrice) {
            this.normalPrice = normalPrice;
            return this;
        }

        public Builder sellingPrice(int sellingPrice) {
            this.sellingPrice = sellingPrice;
            return this;
        }

        public Builder discountPrice(int discountPrice) {
            this.discountPrice = discountPrice;
            return this;
        }

        public Builder discountRate(int discountRate) {
            this.discountRate = discountRate;
            return this;
        }

        public Builder stock(int stock) {
            this.stock = stock;
            return this;
        }

        public Builder isSoldOut(boolean isSoldOut) {
            this.isSoldOut = isSoldOut;
            return this;
        }

        public Builder shipping(ShippingInfo shipping) {
            this.shipping = shipping;
            return this;
        }

        public Builder bannerImages(List<String> bannerImages) {
            // 방어적 복사 - SpotBugs EI2 경고 수정
            this.bannerImages = bannerImages != null ? List.copyOf(bannerImages) : List.of();
            return this;
        }

        public Builder detailImages(List<String> detailImages) {
            // 방어적 복사 - SpotBugs EI2 경고 수정
            this.detailImages = detailImages != null ? List.copyOf(detailImages) : List.of();
            return this;
        }

        public Builder originCountry(String originCountry) {
            this.originCountry = originCountry;
            return this;
        }

        public Builder itemStatus(String itemStatus) {
            this.itemStatus = itemStatus;
            return this;
        }

        public ProductDetailInfo build() {
            return new ProductDetailInfo(
                    sellerNo,
                    sellerId,
                    itemNo,
                    itemName,
                    brandName,
                    brandNameKr,
                    brandCode,
                    category,
                    normalPrice,
                    sellingPrice,
                    discountPrice,
                    discountRate,
                    stock,
                    isSoldOut,
                    shipping,
                    bannerImages,
                    detailImages,
                    originCountry,
                    itemStatus);
        }
    }
}
