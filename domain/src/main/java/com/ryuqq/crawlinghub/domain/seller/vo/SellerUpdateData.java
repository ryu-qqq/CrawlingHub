package com.ryuqq.crawlinghub.domain.seller.vo;

/**
 * 셀러 수정 데이터
 *
 * <p>셀러 수정에 필요한 모든 필드를 non-null로 포함합니다.
 *
 * @param mustItSellerName 머스트잇 셀러명
 * @param sellerName 셀러명
 * @param status 셀러 상태
 */
public record SellerUpdateData(
        MustItSellerName mustItSellerName, SellerName sellerName, SellerStatus status) {

    public static SellerUpdateData of(
            MustItSellerName mustItSellerName, SellerName sellerName, SellerStatus status) {
        return new SellerUpdateData(mustItSellerName, sellerName, status);
    }
}
