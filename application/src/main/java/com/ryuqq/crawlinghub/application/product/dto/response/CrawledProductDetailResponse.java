package com.ryuqq.crawlinghub.application.product.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * CrawledProduct 상세 Response
 *
 * <p>상세 조회용 전체 정보
 *
 * @param id 상품 ID
 * @param sellerId 판매자 ID
 * @param itemNo 상품 번호
 * @param itemName 상품명
 * @param brandName 브랜드명
 * @param itemStatus 상품 상태
 * @param originCountry 원산지
 * @param shippingLocation 배송 출발지
 * @param freeShipping 무료 배송 여부
 * @param price 가격 정보
 * @param images 이미지 정보
 * @param category 카테고리 정보 (nullable)
 * @param shipping 배송 정보 (nullable)
 * @param options 옵션 정보
 * @param crawlStatus 크롤링 상태
 * @param syncStatus 동기화 상태
 * @param descriptionMarkUp 상세 설명 HTML
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author development-team
 * @since 1.0.0
 */
public record CrawledProductDetailResponse(
        Long id,
        Long sellerId,
        long itemNo,
        String itemName,
        String brandName,
        String itemStatus,
        String originCountry,
        String shippingLocation,
        boolean freeShipping,
        PriceInfo price,
        ImagesInfo images,
        CategoryInfo category,
        ShippingInfoDto shipping,
        OptionsInfo options,
        CrawlStatusInfo crawlStatus,
        SyncStatusInfo syncStatus,
        String descriptionMarkUp,
        Instant createdAt,
        Instant updatedAt) {

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
     * @param thumbnails 썸네일 목록
     * @param descriptionImages 상세 이미지 목록
     * @param totalCount 총 이미지 개수
     * @param uploadedCount 업로드 완료 개수
     */
    public record ImagesInfo(
            List<ImageInfo> thumbnails,
            List<ImageInfo> descriptionImages,
            int totalCount,
            int uploadedCount) {
        public ImagesInfo {
            thumbnails = thumbnails == null ? List.of() : List.copyOf(thumbnails);
            descriptionImages =
                    descriptionImages == null ? List.of() : List.copyOf(descriptionImages);
        }
    }

    /**
     * 개별 이미지 정보
     *
     * @param originalUrl 원본 URL
     * @param s3Url S3 URL (nullable)
     * @param status 상태 (PENDING, UPLOADED)
     * @param displayOrder 표시 순서
     */
    public record ImageInfo(String originalUrl, String s3Url, String status, int displayOrder) {}

    /**
     * 카테고리 정보
     *
     * @param fullPath 전체 카테고리 경로
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
     * @param shippingType 배송 타입
     * @param shippingFee 배송비
     * @param shippingFeeType 배송비 타입
     * @param averageDeliveryDays 평균 배송일
     * @param freeShipping 무료 배송 여부
     */
    public record ShippingInfoDto(
            String shippingType,
            int shippingFee,
            String shippingFeeType,
            int averageDeliveryDays,
            boolean freeShipping) {}

    /**
     * 옵션 정보
     *
     * @param options 옵션 목록
     * @param totalStock 총 재고
     * @param inStockCount 재고 있는 옵션 개수
     * @param soldOutCount 품절 옵션 개수
     * @param distinctColors 색상 목록
     * @param distinctSizes 사이즈 목록
     */
    public record OptionsInfo(
            List<OptionInfo> options,
            int totalStock,
            int inStockCount,
            int soldOutCount,
            List<String> distinctColors,
            List<String> distinctSizes) {
        public OptionsInfo {
            options = options == null ? List.of() : List.copyOf(options);
            distinctColors = distinctColors == null ? List.of() : List.copyOf(distinctColors);
            distinctSizes = distinctSizes == null ? List.of() : List.copyOf(distinctSizes);
        }
    }

    /**
     * 개별 옵션 정보
     *
     * @param optionNo 옵션 번호
     * @param color 색상
     * @param size 사이즈
     * @param stock 재고
     */
    public record OptionInfo(long optionNo, String color, String size, int stock) {}

    /**
     * 크롤링 상태
     *
     * @param miniShopCrawledAt MINI_SHOP 크롤링 시각
     * @param detailCrawledAt DETAIL 크롤링 시각
     * @param optionCrawledAt OPTION 크롤링 시각
     * @param completedCount 완료 개수
     * @param pendingTypes 미완료 타입
     */
    public record CrawlStatusInfo(
            Instant miniShopCrawledAt,
            Instant detailCrawledAt,
            Instant optionCrawledAt,
            int completedCount,
            List<String> pendingTypes) {
        public CrawlStatusInfo {
            pendingTypes = pendingTypes == null ? List.of() : List.copyOf(pendingTypes);
        }
    }

    /**
     * 동기화 상태
     *
     * @param externalProductId 외부 상품 ID (nullable)
     * @param needsSync 동기화 필요 여부
     * @param lastSyncedAt 마지막 동기화 시각 (nullable)
     * @param canSync 동기화 가능 여부
     */
    public record SyncStatusInfo(
            Long externalProductId, boolean needsSync, Instant lastSyncedAt, boolean canSync) {}
}
