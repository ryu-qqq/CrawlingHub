package com.ryuqq.crawlinghub.application.product.port.out.client;

/**
 * 외부 상품 서버 연동 Port (Port Out - External)
 *
 * <p>크롤링된 상품 정보를 외부 상품 서버에 등록/갱신합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ExternalProductServerClient {

    /**
     * 상품 신규 등록
     *
     * @param request 등록 요청 정보
     * @return 등록 결과 (외부 상품 ID 포함)
     */
    ProductSyncResult createProduct(ProductCreateRequest request);

    /**
     * 상품 정보 갱신
     *
     * @param request 갱신 요청 정보
     * @return 갱신 결과
     */
    ProductSyncResult updateProduct(ProductUpdateRequest request);

    /**
     * 상품 존재 여부 확인
     *
     * @param externalProductId 외부 상품 ID
     * @return 존재하면 true
     */
    boolean existsProduct(Long externalProductId);

    /**
     * 상품 신규 등록 요청
     */
    record ProductCreateRequest(
            String idempotencyKey,
            long sellerId,
            long itemNo,
            String itemName,
            String brandName,
            int sellingPrice,
            int normalPrice,
            int discountPrice,
            int discountRate,
            String categoryCode,
            String categoryName,
            String mainImageUrl,
            String descriptionMarkUp,
            String originCountry,
            String itemStatus,
            boolean freeShipping,
            int totalStock) {

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String idempotencyKey;
            private long sellerId;
            private long itemNo;
            private String itemName;
            private String brandName;
            private int sellingPrice;
            private int normalPrice;
            private int discountPrice;
            private int discountRate;
            private String categoryCode;
            private String categoryName;
            private String mainImageUrl;
            private String descriptionMarkUp;
            private String originCountry;
            private String itemStatus;
            private boolean freeShipping;
            private int totalStock;

            public Builder idempotencyKey(String idempotencyKey) {
                this.idempotencyKey = idempotencyKey;
                return this;
            }

            public Builder sellerId(long sellerId) {
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

            public Builder sellingPrice(int sellingPrice) {
                this.sellingPrice = sellingPrice;
                return this;
            }

            public Builder normalPrice(int normalPrice) {
                this.normalPrice = normalPrice;
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

            public Builder categoryCode(String categoryCode) {
                this.categoryCode = categoryCode;
                return this;
            }

            public Builder categoryName(String categoryName) {
                this.categoryName = categoryName;
                return this;
            }

            public Builder mainImageUrl(String mainImageUrl) {
                this.mainImageUrl = mainImageUrl;
                return this;
            }

            public Builder descriptionMarkUp(String descriptionMarkUp) {
                this.descriptionMarkUp = descriptionMarkUp;
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

            public Builder freeShipping(boolean freeShipping) {
                this.freeShipping = freeShipping;
                return this;
            }

            public Builder totalStock(int totalStock) {
                this.totalStock = totalStock;
                return this;
            }

            public ProductCreateRequest build() {
                return new ProductCreateRequest(
                        idempotencyKey, sellerId, itemNo, itemName, brandName,
                        sellingPrice, normalPrice, discountPrice, discountRate,
                        categoryCode, categoryName, mainImageUrl, descriptionMarkUp,
                        originCountry, itemStatus, freeShipping, totalStock);
            }
        }
    }

    /**
     * 상품 갱신 요청
     */
    record ProductUpdateRequest(
            String idempotencyKey,
            Long externalProductId,
            String itemName,
            String brandName,
            int sellingPrice,
            int normalPrice,
            int discountPrice,
            int discountRate,
            String mainImageUrl,
            String descriptionMarkUp,
            int totalStock) {

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String idempotencyKey;
            private Long externalProductId;
            private String itemName;
            private String brandName;
            private int sellingPrice;
            private int normalPrice;
            private int discountPrice;
            private int discountRate;
            private String mainImageUrl;
            private String descriptionMarkUp;
            private int totalStock;

            public Builder idempotencyKey(String idempotencyKey) {
                this.idempotencyKey = idempotencyKey;
                return this;
            }

            public Builder externalProductId(Long externalProductId) {
                this.externalProductId = externalProductId;
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

            public Builder sellingPrice(int sellingPrice) {
                this.sellingPrice = sellingPrice;
                return this;
            }

            public Builder normalPrice(int normalPrice) {
                this.normalPrice = normalPrice;
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

            public Builder mainImageUrl(String mainImageUrl) {
                this.mainImageUrl = mainImageUrl;
                return this;
            }

            public Builder descriptionMarkUp(String descriptionMarkUp) {
                this.descriptionMarkUp = descriptionMarkUp;
                return this;
            }

            public Builder totalStock(int totalStock) {
                this.totalStock = totalStock;
                return this;
            }

            public ProductUpdateRequest build() {
                return new ProductUpdateRequest(
                        idempotencyKey, externalProductId, itemName, brandName,
                        sellingPrice, normalPrice, discountPrice, discountRate,
                        mainImageUrl, descriptionMarkUp, totalStock);
            }
        }
    }

    /**
     * 상품 동기화 결과
     */
    record ProductSyncResult(
            boolean success,
            Long externalProductId,
            String errorCode,
            String errorMessage) {

        public static ProductSyncResult success(Long externalProductId) {
            return new ProductSyncResult(true, externalProductId, null, null);
        }

        public static ProductSyncResult failure(String errorCode, String errorMessage) {
            return new ProductSyncResult(false, null, errorCode, errorMessage);
        }
    }
}
