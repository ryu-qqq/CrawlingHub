package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

import java.util.List;

/**
 * 인바운드 상품 이미지 수정 요청 DTO
 *
 * <p>MarketPlace PATCH
 * /api/v1/market/inbound/products/{inboundSourceId}/{externalProductCode}/images
 */
public record UpdateImagesRequest(List<ImageEntry> images) {

    public UpdateImagesRequest {
        images = images != null ? List.copyOf(images) : List.of();
    }

    public record ImageEntry(String imageType, String originUrl, int sortOrder) {}
}
