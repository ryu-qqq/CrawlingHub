package com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response;

import java.util.List;

/**
 * CrawledProduct Detail API Response
 *
 * <p>크롤링 상품 상세 정보 API 응답 DTO
 *
 * <p><strong>응답 구조:</strong>
 *
 * <ul>
 *   <li>기본 정보: 상품 식별 정보 및 메타데이터
 *   <li>가격 정보: 정가, 판매가, 할인율 등
 *   <li>이미지 정보: 썸네일, 상세 이미지, 업로드 상태
 *   <li>카테고리 정보: 대/중/소 카테고리
 *   <li>배송 정보: 배송비, 배송 유형 등
 *   <li>옵션 정보: 색상, 사이즈, 재고 등
 *   <li>크롤링 상태: 각 크롤링 유형별 완료 시간
 *   <li>동기화 상태: 외부 연동 정보
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record CrawledProductDetailApiResponse(
        Long id,
        Long sellerId,
        Long itemNo,
        String itemName,
        String brandName,
        String itemStatus,
        String originCountry,
        String shippingLocation,
        boolean freeShipping,
        PriceInfo price,
        ImagesInfo images,
        CategoryInfo category,
        ShippingInfo shipping,
        OptionsInfo options,
        CrawlStatusInfo crawlStatus,
        SyncStatusInfo syncStatus,
        String descriptionMarkUp,
        String createdAt,
        String updatedAt) {

    /**
     * 가격 정보
     *
     * @param price 판매가
     * @param originalPrice 원가
     * @param normalPrice 정상가
     * @param appPrice 앱 가격
     * @param discountRate 할인율
     * @param appDiscountRate 앱 할인율
     */
    public record PriceInfo(
            int price,
            int originalPrice,
            int normalPrice,
            int appPrice,
            int discountRate,
            int appDiscountRate) {}

    /**
     * 이미지 정보
     *
     * @param thumbnails 썸네일 이미지 목록
     * @param descriptionImages 상세 이미지 목록
     * @param totalCount 전체 이미지 수
     * @param uploadedCount 업로드 완료 수
     */
    public record ImagesInfo(
            List<ImageInfo> thumbnails,
            List<ImageInfo> descriptionImages,
            int totalCount,
            int uploadedCount) {}

    /**
     * 개별 이미지 정보
     *
     * @param originalUrl 원본 URL
     * @param s3Url S3 업로드 URL
     * @param status 업로드 상태
     * @param displayOrder 표시 순서
     */
    public record ImageInfo(String originalUrl, String s3Url, String status, int displayOrder) {}

    /**
     * 카테고리 정보
     *
     * @param fullPath 전체 경로
     * @param headerCategoryCode 대분류 코드
     * @param headerCategoryName 대분류명
     * @param largeCategoryCode 중분류 코드
     * @param largeCategoryName 중분류명
     * @param mediumCategoryCode 소분류 코드
     * @param mediumCategoryName 소분류명
     */
    public record CategoryInfo(
            String fullPath,
            String headerCategoryCode,
            String headerCategoryName,
            String largeCategoryCode,
            String largeCategoryName,
            String mediumCategoryCode,
            String mediumCategoryName) {}

    /**
     * 배송 정보
     *
     * @param shippingType 배송 유형
     * @param shippingFee 배송비
     * @param shippingFeeType 배송비 유형
     * @param averageDeliveryDays 평균 배송일
     * @param freeShipping 무료배송 여부
     */
    public record ShippingInfo(
            String shippingType,
            int shippingFee,
            String shippingFeeType,
            int averageDeliveryDays,
            boolean freeShipping) {}

    /**
     * 옵션 정보
     *
     * @param options 옵션 목록
     * @param totalStock 전체 재고
     * @param inStockCount 재고 있는 옵션 수
     * @param soldOutCount 품절 옵션 수
     * @param distinctColors 색상 종류
     * @param distinctSizes 사이즈 종류
     */
    public record OptionsInfo(
            List<OptionInfo> options,
            int totalStock,
            int inStockCount,
            int soldOutCount,
            List<String> distinctColors,
            List<String> distinctSizes) {}

    /**
     * 개별 옵션 정보
     *
     * @param optionNo 옵션 번호
     * @param color 색상
     * @param size 사이즈
     * @param stock 재고
     */
    public record OptionInfo(Long optionNo, String color, String size, int stock) {}

    /**
     * 크롤링 상태 정보
     *
     * @param miniShopCrawledAt 미니샵 크롤링 완료 시간
     * @param detailCrawledAt 상세 크롤링 완료 시간
     * @param optionCrawledAt 옵션 크롤링 완료 시간
     * @param completedCount 완료된 크롤링 수
     * @param pendingTypes 대기 중인 크롤링 유형
     */
    public record CrawlStatusInfo(
            String miniShopCrawledAt,
            String detailCrawledAt,
            String optionCrawledAt,
            int completedCount,
            List<String> pendingTypes) {}

    /**
     * 동기화 상태 정보
     *
     * @param externalProductId 외부 상품 ID
     * @param needsSync 동기화 필요 여부
     * @param lastSyncedAt 마지막 동기화 시간
     * @param canSync 동기화 가능 여부
     */
    public record SyncStatusInfo(
            Long externalProductId, boolean needsSync, String lastSyncedAt, boolean canSync) {}
}
