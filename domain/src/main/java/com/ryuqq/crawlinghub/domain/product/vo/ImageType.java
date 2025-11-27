package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 이미지 타입
 *
 * <p>이미지의 출처 및 용도를 구분합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum ImageType {

    /**
     * 썸네일 이미지
     *
     * <p>MiniShopItem.imageUrlList에서 추출된 상품 대표 이미지
     */
    THUMBNAIL,

    /**
     * 상세 설명 이미지
     *
     * <p>ProductDetailInfoModule.descriptionMarkUp HTML 내 img 태그에서 추출된 이미지
     */
    DESCRIPTION
}
