package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

/**
 * 인바운드 상품 상세설명 수정 요청 DTO
 *
 * <p>MarketPlace PATCH
 * /api/v1/market/inbound/products/{inboundSourceId}/{externalProductCode}/description
 */
public record UpdateDescriptionRequest(String content) {}
