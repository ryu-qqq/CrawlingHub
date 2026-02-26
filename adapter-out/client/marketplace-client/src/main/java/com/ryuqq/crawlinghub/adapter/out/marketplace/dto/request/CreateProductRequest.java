package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

/**
 * 외부몰 상품 등록 요청 DTO
 *
 * @param inboundSourceId 인바운드 소스 ID
 * @param externalProductCode 외부몰 상품 코드
 * @param productName 상품명
 * @param externalBrandCode 외부몰 브랜드 코드
 * @param externalCategoryCode 외부몰 카테고리 코드
 * @param sellerId 셀러 ID
 * @param regularPrice 정상가
 * @param currentPrice 판매가
 * @param optionType 옵션 타입
 * @param descriptionHtml 상품 상세 설명 HTML
 * @param rawPayloadJson 원본 페이로드 JSON
 * @author development-team
 * @since 1.0.0
 */
public record CreateProductRequest(
        long inboundSourceId,
        String externalProductCode,
        String productName,
        String externalBrandCode,
        String externalCategoryCode,
        long sellerId,
        int regularPrice,
        int currentPrice,
        String optionType,
        String descriptionHtml,
        String rawPayloadJson) {}
