package com.ryuqq.crawlinghub.domain.product.aggregate;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CrawledProduct Aggregate Root
 *
 * <p>크롤링된 상품 정보를 관리하는 Aggregate Root.
 * <p>MINI_SHOP, DETAIL, OPTION 크롤링 결과를 통합하여 관리하고,
 * 변경 감지, 이미지 업로드, 외부 서버 동기화 등의 비즈니스 규칙을 담당합니다.
 *
 * <p><strong>핵심 비즈니스 규칙</strong>:
 * <ul>
 *   <li>외부 서버 동기화는 MINI_SHOP, DETAIL, OPTION이 모두 한 번 이상 크롤링된 후에만 가능
 *   <li>이름, 이미지, 브랜드, 가격 변경 시 외부 서버로 갱신 필요
 *   <li>이미지는 S3에 업로드 후 URL 교체
 *   <li>"무료배송" 태그만 저장
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawledProduct {

    private final CrawledProductId id;
    private final SellerId sellerId;
    private final long itemNo;

    // MINI_SHOP 데이터
    private String itemName;
    private String brandName;
    private ProductPrice price;
    private ProductImages images;
    private boolean freeShipping;

    // DETAIL 데이터
    private ProductCategory category;
    private ShippingInfo shippingInfo;
    private String descriptionMarkUp;
    private String itemStatus;
    private String originCountry;
    private String shippingLocation;

    // OPTION 데이터
    private ProductOptions options;

    // 크롤링 완료 상태
    private CrawlCompletionStatus crawlCompletionStatus;

    // 외부 서버 동기화 상태
    private Long externalProductId;
    private LocalDateTime lastSyncedAt;
    private boolean needsSync;

    // 감사 정보
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 도메인 이벤트
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private CrawledProduct(
            CrawledProductId id,
            SellerId sellerId,
            long itemNo,
            String itemName,
            String brandName,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping,
            ProductCategory category,
            ShippingInfo shippingInfo,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            ProductOptions options,
            CrawlCompletionStatus crawlCompletionStatus,
            Long externalProductId,
            LocalDateTime lastSyncedAt,
            boolean needsSync,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.itemNo = itemNo;
        this.itemName = itemName;
        this.brandName = brandName;
        this.price = price;
        this.images = images;
        this.freeShipping = freeShipping;
        this.category = category;
        this.shippingInfo = shippingInfo;
        this.descriptionMarkUp = descriptionMarkUp;
        this.itemStatus = itemStatus;
        this.originCountry = originCountry;
        this.shippingLocation = shippingLocation;
        this.options = options;
        this.crawlCompletionStatus = crawlCompletionStatus;
        this.externalProductId = externalProductId;
        this.lastSyncedAt = lastSyncedAt;
        this.needsSync = needsSync;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // === 팩토리 메서드 ===

    /**
     * MINI_SHOP 크롤링 결과로 신규 상품 생성
     *
     * @param sellerId 셀러 ID
     * @param itemNo 상품 번호
     * @param itemName 상품명
     * @param brandName 브랜드명
     * @param price 가격 정보
     * @param images 이미지 목록
     * @param freeShipping 무료 배송 여부
     * @return 새로운 CrawledProduct
     */
    public static CrawledProduct fromMiniShop(
            SellerId sellerId,
            long itemNo,
            String itemName,
            String brandName,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping) {
        LocalDateTime now = LocalDateTime.now();
        return new CrawledProduct(
                CrawledProductId.unassigned(),
                sellerId,
                itemNo,
                itemName,
                brandName,
                price,
                images,
                freeShipping,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                CrawlCompletionStatus.initial().withMiniShopCrawled(now),
                null,
                null,
                false,
                now,
                now);
    }

    /**
     * 기존 데이터로 CrawledProduct 복원 (영속성 계층 전용)
     */
    public static CrawledProduct reconstitute(
            CrawledProductId id,
            SellerId sellerId,
            long itemNo,
            String itemName,
            String brandName,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping,
            ProductCategory category,
            ShippingInfo shippingInfo,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            ProductOptions options,
            CrawlCompletionStatus crawlCompletionStatus,
            Long externalProductId,
            LocalDateTime lastSyncedAt,
            boolean needsSync,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new CrawledProduct(
                id, sellerId, itemNo, itemName, brandName, price, images, freeShipping,
                category, shippingInfo, descriptionMarkUp, itemStatus, originCountry,
                shippingLocation, options, crawlCompletionStatus, externalProductId,
                lastSyncedAt, needsSync, createdAt, updatedAt);
    }

    // === MINI_SHOP 업데이트 ===

    /**
     * MINI_SHOP 크롤링 결과로 업데이트
     *
     * <p>이름, 브랜드, 가격, 이미지 변경을 감지하고 needsSync 플래그 설정
     *
     * @param itemName 상품명
     * @param brandName 브랜드명
     * @param price 가격 정보
     * @param images 이미지 목록
     * @param freeShipping 무료 배송 여부
     */
    public void updateFromMiniShop(
            String itemName,
            String brandName,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping) {
        boolean hasChanges = detectMiniShopChanges(itemName, brandName, price, images);

        this.itemName = itemName;
        this.brandName = brandName;
        this.price = price;
        this.images = images;
        this.freeShipping = freeShipping;
        this.crawlCompletionStatus = this.crawlCompletionStatus.withMiniShopCrawled(LocalDateTime.now());
        this.updatedAt = LocalDateTime.now();

        if (hasChanges && canSyncToExternalServer()) {
            this.needsSync = true;
        }
    }

    /**
     * MINI_SHOP 데이터 변경 감지
     *
     * @return 이름, 브랜드, 가격, 이미지 중 하나라도 변경되었으면 true
     */
    private boolean detectMiniShopChanges(
            String newItemName,
            String newBrandName,
            ProductPrice newPrice,
            ProductImages newImages) {
        boolean nameChanged = !equalsNullSafe(this.itemName, newItemName);
        boolean brandChanged = !equalsNullSafe(this.brandName, newBrandName);
        boolean priceChanged = this.price == null || this.price.hasPriceChange(newPrice);
        boolean imagesChanged = this.images == null || this.images.hasChanges(newImages);

        return nameChanged || brandChanged || priceChanged || imagesChanged;
    }

    // === DETAIL 업데이트 ===

    /**
     * DETAIL 크롤링 결과로 업데이트
     *
     * @param category 카테고리 정보
     * @param shippingInfo 배송 정보
     * @param descriptionMarkUp 상세 설명 HTML
     * @param itemStatus 상품 상태
     * @param originCountry 원산지
     * @param shippingLocation 배송 출발지
     * @param descriptionImages 상세 설명 내 이미지 URL 목록
     */
    public void updateFromDetail(
            ProductCategory category,
            ShippingInfo shippingInfo,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            List<String> descriptionImages) {
        this.category = category;
        this.shippingInfo = shippingInfo;
        this.descriptionMarkUp = descriptionMarkUp;
        this.itemStatus = itemStatus;
        this.originCountry = originCountry;
        this.shippingLocation = shippingLocation;
        this.crawlCompletionStatus = this.crawlCompletionStatus.withDetailCrawled(LocalDateTime.now());
        this.updatedAt = LocalDateTime.now();

        // 상세 설명 이미지 추가
        if (descriptionImages != null && !descriptionImages.isEmpty()) {
            this.images = this.images.addDescriptionImages(descriptionImages);
        }

        if (canSyncToExternalServer()) {
            this.needsSync = true;
        }
    }

    // === OPTION 업데이트 ===

    /**
     * OPTION 크롤링 결과로 업데이트
     *
     * @param options 옵션 목록
     */
    public void updateFromOption(ProductOptions options) {
        boolean hasChanges = this.options != null && this.options.hasChanges(options);

        this.options = options;
        this.crawlCompletionStatus = this.crawlCompletionStatus.withOptionCrawled(LocalDateTime.now());
        this.updatedAt = LocalDateTime.now();

        if (hasChanges && canSyncToExternalServer()) {
            this.needsSync = true;
        }
    }

    // === 이미지 업로드 관련 ===

    /**
     * 업로드가 필요한 이미지 URL 목록 반환
     *
     * @return 업로드 대기 중인 이미지 URL 목록
     */
    public List<String> getPendingUploadImageUrls() {
        if (this.images == null) {
            return Collections.emptyList();
        }
        return this.images.getPendingUploadUrls();
    }

    /**
     * 이미지 S3 업로드 완료 처리
     *
     * @param originalUrl 원본 URL
     * @param s3Url S3 URL
     */
    public void markImageAsUploaded(String originalUrl, String s3Url) {
        if (this.images != null) {
            this.images = this.images.updateS3Url(originalUrl, s3Url);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 모든 이미지가 업로드 완료되었는지 확인
     */
    public boolean allImagesUploaded() {
        return this.images != null && this.images.allUploaded();
    }

    // === 외부 서버 동기화 관련 ===

    /**
     * 외부 서버 동기화 가능 여부 확인
     *
     * <p>MINI_SHOP, DETAIL, OPTION이 모두 한 번 이상 크롤링되어야 함
     *
     * @return 동기화 가능하면 true
     */
    public boolean canSyncToExternalServer() {
        return this.crawlCompletionStatus.canSyncToExternalServer();
    }

    /**
     * 외부 서버 동기화 필요 여부 확인
     *
     * @return 동기화가 필요하면 true
     */
    public boolean needsExternalSync() {
        return this.needsSync && canSyncToExternalServer();
    }

    /**
     * 외부 서버 동기화 완료 처리
     *
     * @param externalProductId 외부 서버에서 할당받은 상품 ID
     */
    public void markAsSynced(Long externalProductId) {
        this.externalProductId = externalProductId;
        this.lastSyncedAt = LocalDateTime.now();
        this.needsSync = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 동기화 실패 처리
     */
    public void markSyncFailed() {
        // needsSync는 true로 유지하여 재시도 가능
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 외부 서버에 등록된 상품인지 확인
     *
     * @return 외부 상품 ID가 있으면 true
     */
    public boolean isRegisteredToExternalServer() {
        return this.externalProductId != null;
    }

    // === 상태 조회 ===

    /**
     * 품절 여부 확인
     */
    public boolean isSoldOut() {
        return this.options != null && this.options.isAllSoldOut();
    }

    /**
     * 총 재고 수량
     */
    public int getTotalStock() {
        return this.options != null ? this.options.getTotalStock() : 0;
    }

    // === 유틸리티 ===

    private boolean equalsNullSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    // === Getters ===

    public CrawledProductId getId() {
        return id;
    }

    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public Long getSellerIdValue() {
        return sellerId.value();
    }

    public long getItemNo() {
        return itemNo;
    }

    public String getItemName() {
        return itemName;
    }

    public String getBrandName() {
        return brandName;
    }

    public ProductPrice getPrice() {
        return price;
    }

    public ProductImages getImages() {
        return images;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public ShippingInfo getShippingInfo() {
        return shippingInfo;
    }

    public String getDescriptionMarkUp() {
        return descriptionMarkUp;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public String getShippingLocation() {
        return shippingLocation;
    }

    public ProductOptions getOptions() {
        return options;
    }

    public CrawlCompletionStatus getCrawlCompletionStatus() {
        return crawlCompletionStatus;
    }

    public Long getExternalProductId() {
        return externalProductId;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public boolean isNeedsSync() {
        return needsSync;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // === 도메인 이벤트 ===

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
}
