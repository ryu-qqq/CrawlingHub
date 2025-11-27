package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 상품 수 Value Object
 *
 * <p>미니샵 메타 정보에서 파싱된 총 상품 수.
 *
 * <p><strong>페이지 계산</strong>: totalCount / 500 = 총 페이지 수
 *
 * @param totalCount 총 상품 수
 * @author development-team
 * @since 1.0.0
 */
public record ProductCount(int totalCount) {

    private static final int PAGE_SIZE = 500;

    public ProductCount {
        if (totalCount < 0) {
            throw new IllegalArgumentException("totalCount는 0 이상이어야 합니다.");
        }
    }

    /**
     * 팩토리 메서드
     *
     * @param totalCount 총 상품 수
     * @return ProductCount
     */
    public static ProductCount of(int totalCount) {
        return new ProductCount(totalCount);
    }

    /**
     * 총 페이지 수 계산
     *
     * <p>상품 수 / 500, 올림 처리
     *
     * @return 총 페이지 수
     */
    public int calculateTotalPages() {
        if (totalCount == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / PAGE_SIZE);
    }

    /**
     * 상품이 존재하는지 확인
     *
     * @return 상품이 있으면 true
     */
    public boolean hasProducts() {
        return totalCount > 0;
    }
}
