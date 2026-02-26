package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

import java.util.List;

/**
 * 상품 이미지 목록 래퍼
 *
 * <p>이미지 등록/수정 API에서 독립적으로 사용 가능한 래퍼 객체. 받는 쪽에서 이미지 목록 단위로 밸리데이션하기 용이합니다.
 *
 * @param thumbnails 썸네일 이미지 목록
 * @param descriptionImages 상세 설명 이미지 목록
 * @author development-team
 * @since 1.0.0
 */
public record ProductImageListPayload(
        List<ProductImagePayload> thumbnails, List<ProductImagePayload> descriptionImages) {

    public ProductImageListPayload(
            List<ProductImagePayload> thumbnails, List<ProductImagePayload> descriptionImages) {
        this.thumbnails = thumbnails == null ? List.of() : List.copyOf(thumbnails);
        this.descriptionImages =
                descriptionImages == null ? List.of() : List.copyOf(descriptionImages);
    }
}
