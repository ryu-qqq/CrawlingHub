package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

/**
 * 외부몰 상품 등록 전체 페이로드
 *
 * <p>CreateProductRequest.rawPayloadJson으로 직렬화되는 루트 객체. CrawledProduct의 전체 데이터를 외부몰 형식으로 변환한 구조입니다.
 *
 * @param itemName 상품명
 * @param brandName 브랜드명
 * @param categoryCode 카테고리 코드 (소분류)
 * @param categoryName 카테고리 전체 경로
 * @param regularPrice 정상가
 * @param currentPrice 판매가
 * @param discountRate 할인율
 * @param itemStatus 상품 상태
 * @param originCountry 원산지
 * @param descriptionHtml 상세 설명 HTML (S3 URL 치환된)
 * @param freeShipping 무료 배송 여부
 * @param images 이미지 정보
 * @param options 옵션 정보
 * @param shipping 배송 정보
 * @author development-team
 * @since 1.0.0
 */
public record CreateProductPayload(
        String itemName,
        String brandName,
        String categoryCode,
        String categoryName,
        int regularPrice,
        int currentPrice,
        int discountRate,
        String itemStatus,
        String originCountry,
        String descriptionHtml,
        boolean freeShipping,
        ProductImageListPayload images,
        ProductOptionListPayload options,
        ProductShippingPayload shipping) {}
