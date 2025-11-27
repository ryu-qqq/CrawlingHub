package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 상품 옵션 VO
 *
 * <p>OPTION 크롤링에서 추출한 개별 옵션 정보
 *
 * @param optionNo 옵션 번호 (고유 식별자)
 * @param itemNo 상품 번호
 * @param color 색상 (nullable)
 * @param size 사이즈
 * @param stock 재고 수량
 * @param sizeGuide 사이즈 가이드 (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record ProductOption(
        long optionNo,
        long itemNo,
        String color,
        String size,
        int stock,
        String sizeGuide) {

    public ProductOption {
        if (optionNo <= 0) {
            throw new IllegalArgumentException("optionNo는 양수여야 합니다.");
        }
        if (itemNo <= 0) {
            throw new IllegalArgumentException("itemNo는 양수여야 합니다.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock은 0 이상이어야 합니다.");
        }
        // color, size, sizeGuide는 null 허용
        if (color == null) {
            color = "";
        }
        if (size == null) {
            size = "";
        }
        if (sizeGuide == null) {
            sizeGuide = "";
        }
    }

    /**
     * 팩토리 메서드
     */
    public static ProductOption of(
            long optionNo,
            long itemNo,
            String color,
            String size,
            int stock,
            String sizeGuide) {
        return new ProductOption(optionNo, itemNo, color, size, stock, sizeGuide);
    }

    /**
     * 재고가 있는지 확인
     */
    public boolean isInStock() {
        return stock > 0;
    }

    /**
     * 품절인지 확인
     */
    public boolean isSoldOut() {
        return stock <= 0;
    }

    /**
     * 색상이 있는지 확인
     */
    public boolean hasColor() {
        return color != null && !color.isBlank();
    }

    /**
     * 사이즈가 있는지 확인
     */
    public boolean hasSize() {
        return size != null && !size.isBlank();
    }

    /**
     * 옵션 변경 여부 확인 (재고만 비교)
     *
     * @param other 비교 대상
     * @return 재고가 변경되었으면 true
     */
    public boolean hasStockChange(ProductOption other) {
        if (other == null) {
            return true;
        }
        return this.stock != other.stock;
    }


}
