package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

/**
 * 인바운드 상품 가격 수정 요청 DTO
 *
 * <p>MarketPlace PATCH
 * /api/v1/market/inbound/products/{inboundSourceId}/{externalProductCode}/price
 */
public record UpdatePriceRequest(int regularPrice, int currentPrice) {}
