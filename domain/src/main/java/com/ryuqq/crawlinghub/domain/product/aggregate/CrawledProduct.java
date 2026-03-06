package com.ryuqq.crawlinghub.domain.product.aggregate;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * CrawledProduct Aggregate Root
 *
 * <p>크롤링된 상품 정보를 관리하는 Aggregate Root.
 *
 * <p>MINI_SHOP, DETAIL, OPTION 크롤링 결과를 통합하여 관리하고, 변경 감지, 이미지 업로드, 외부 서버 동기화 등의 비즈니스 규칙을 담당합니다.
 *
 * <p><strong>핵심 비즈니스 규칙</strong>:
 *
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
    private long brandCode;
    private ProductPrice price;
    private ProductImages images;
    private boolean freeShipping;

    // DETAIL 데이터
    private ProductCategory category;
    private ShippingInfo shippingInfo;
    private String originalDescriptionMarkUp; // 비교 기준 (원본 URL 유지)
    private String descriptionMarkUp; // 실제 사용 (S3 URL로 치환됨)
    private String itemStatus;
    private String originCountry;
    private String shippingLocation;

    // OPTION 데이터
    private ProductOptions options;

    // 크롤링 완료 상태
    private CrawlCompletionStatus crawlCompletionStatus;

    // 외부 서버 동기화 상태
    private Long externalProductId;
    private Instant lastSyncedAt;
    private boolean needsSync;
    private final Set<ProductChangeType> pendingChanges;

    // Soft Delete 상태
    private DeletionStatus deletionStatus;

    // 낙관적 잠금 버전
    private Long version;

    // 감사 정보
    private final Instant createdAt;
    private Instant updatedAt;

    // 도메인 이벤트
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private CrawledProduct(
            CrawledProductId id,
            SellerId sellerId,
            long itemNo,
            String itemName,
            String brandName,
            long brandCode,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping,
            ProductCategory category,
            ShippingInfo shippingInfo,
            String originalDescriptionMarkUp,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            ProductOptions options,
            CrawlCompletionStatus crawlCompletionStatus,
            Long externalProductId,
            Instant lastSyncedAt,
            boolean needsSync,
            Set<ProductChangeType> pendingChanges,
            DeletionStatus deletionStatus,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        this.id = id;
        this.sellerId = sellerId;
        this.itemNo = itemNo;
        this.itemName = itemName;
        this.brandName = brandName;
        this.brandCode = brandCode;
        this.price = price;
        this.images = images;
        this.freeShipping = freeShipping;
        this.category = category;
        this.shippingInfo = shippingInfo;
        this.originalDescriptionMarkUp = originalDescriptionMarkUp;
        this.descriptionMarkUp = descriptionMarkUp;
        this.itemStatus = itemStatus;
        this.originCountry = originCountry;
        this.shippingLocation = shippingLocation;
        this.options = options;
        this.crawlCompletionStatus = crawlCompletionStatus;
        this.externalProductId = externalProductId;
        this.lastSyncedAt = lastSyncedAt;
        this.needsSync = needsSync;
        this.pendingChanges =
                pendingChanges != null
                        ? EnumSet.copyOf(pendingChanges)
                        : EnumSet.noneOf(ProductChangeType.class);
        this.deletionStatus = deletionStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
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
     * @param now 현재 시각
     * @return 새로운 CrawledProduct
     */
    public static CrawledProduct fromMiniShop(
            SellerId sellerId,
            long itemNo,
            String itemName,
            String brandName,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping,
            Instant now) {
        return new CrawledProduct(
                CrawledProductId.forNew(),
                sellerId,
                itemNo,
                itemName,
                brandName,
                0L,
                price,
                images,
                freeShipping,
                null,
                null,
                null, // originalDescriptionMarkUp
                null, // descriptionMarkUp
                null,
                null,
                null,
                ProductOptions.empty(),
                CrawlCompletionStatus.initial().withMiniShopCrawled(now),
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    /**
     * MINI_SHOP 크롤링 데이터 VO로 신규 상품 생성
     *
     * @param crawlData MINI_SHOP 크롤링 데이터 VO
     * @return 새로운 CrawledProduct
     */
    public static CrawledProduct fromMiniShopCrawlData(MiniShopCrawlData crawlData) {
        Instant now = crawlData.createdAt();
        return new CrawledProduct(
                CrawledProductId.forNew(),
                crawlData.sellerId(),
                crawlData.itemNo(),
                crawlData.itemName(),
                crawlData.brandName(),
                0L,
                crawlData.price(),
                crawlData.images(),
                crawlData.freeShipping(),
                null,
                null,
                null, // originalDescriptionMarkUp
                null, // descriptionMarkUp
                null,
                null,
                null,
                ProductOptions.empty(),
                CrawlCompletionStatus.initial().withMiniShopCrawled(now),
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    /** 기존 데이터로 CrawledProduct 복원 (영속성 계층 전용) */
    public static CrawledProduct reconstitute(
            CrawledProductId id,
            SellerId sellerId,
            long itemNo,
            String itemName,
            String brandName,
            long brandCode,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping,
            ProductCategory category,
            ShippingInfo shippingInfo,
            String originalDescriptionMarkUp,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            ProductOptions options,
            CrawlCompletionStatus crawlCompletionStatus,
            Long externalProductId,
            Instant lastSyncedAt,
            boolean needsSync,
            Set<ProductChangeType> pendingChanges,
            DeletionStatus deletionStatus,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        return new CrawledProduct(
                id,
                sellerId,
                itemNo,
                itemName,
                brandName,
                brandCode,
                price,
                images,
                freeShipping,
                category,
                shippingInfo,
                originalDescriptionMarkUp,
                descriptionMarkUp,
                itemStatus,
                originCountry,
                shippingLocation,
                options,
                crawlCompletionStatus,
                externalProductId,
                lastSyncedAt,
                needsSync,
                pendingChanges,
                deletionStatus,
                createdAt,
                updatedAt,
                version);
    }

    // === MINI_SHOP 업데이트 ===

    /**
     * MINI_SHOP 크롤링 데이터 VO로 업데이트
     *
     * <p>이름, 브랜드, 가격, 이미지 변경을 감지하고 needsSync 플래그 설정
     *
     * @param crawlData MINI_SHOP 크롤링 데이터 VO
     */
    public void updateFromMiniShopCrawlData(MiniShopCrawlData crawlData) {
        Instant now = crawlData.createdAt();

        boolean nameChanged = !equalsNullSafe(this.itemName, crawlData.itemName());
        boolean brandChanged = !equalsNullSafe(this.brandName, crawlData.brandName());
        boolean priceChanged = this.price == null || this.price.hasPriceChange(crawlData.price());
        boolean imagesChanged = this.images == null || this.images.hasChanges(crawlData.images());

        this.itemName = crawlData.itemName();
        this.brandName = crawlData.brandName();
        this.price = crawlData.price();
        this.images = crawlData.images();
        this.freeShipping = crawlData.freeShipping();
        this.crawlCompletionStatus = this.crawlCompletionStatus.withMiniShopCrawled(now);
        this.updatedAt = now;

        if (canSyncToExternalServer()) {
            if (nameChanged || brandChanged) {
                addPendingChange(ProductChangeType.PRODUCT_INFO);
            }
            if (priceChanged) {
                addPendingChange(ProductChangeType.PRICE);
            }
            if (imagesChanged) {
                addPendingChange(ProductChangeType.IMAGE);
            }
        }
    }

    // === DETAIL 업데이트 ===

    /**
     * DETAIL 크롤링 결과로 업데이트
     *
     * <p>상세 설명 HTML이 변경되면 기존 상세 이미지를 교체합니다. 변경되지 않았으면 기존 상태를 유지합니다.
     *
     * @param category 카테고리 정보
     * @param shippingInfo 배송 정보
     * @param descriptionMarkUp 상세 설명 HTML
     * @param itemStatus 상품 상태
     * @param originCountry 원산지
     * @param shippingLocation 배송 출발지
     * @param descriptionImages 상세 설명 내 이미지 URL 목록
     * @param now 현재 시각
     * @return 새로 업로드가 필요한 이미지 URL 목록
     */
    public List<String> updateFromDetail(
            ProductCategory category,
            ShippingInfo shippingInfo,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            List<String> descriptionImages,
            Instant now) {
        boolean productInfoChanged =
                detectDetailProductInfoChanges(
                        category, shippingInfo, itemStatus, originCountry, shippingLocation);

        this.category = category;
        this.shippingInfo = shippingInfo;
        this.itemStatus = itemStatus;
        this.originCountry = originCountry;
        this.shippingLocation = shippingLocation;
        this.crawlCompletionStatus = this.crawlCompletionStatus.withDetailCrawled(now);
        this.updatedAt = now;

        List<String> newImageUrls = Collections.emptyList();

        boolean descriptionChanged = hasDescriptionChanged(descriptionMarkUp);
        if (descriptionChanged) {
            this.originalDescriptionMarkUp = descriptionMarkUp;
            this.descriptionMarkUp = descriptionMarkUp;

            if (descriptionImages != null && !descriptionImages.isEmpty()) {
                newImageUrls = this.images.getNewDescriptionImageUrls(descriptionImages);
                this.images = this.images.replaceDescriptionImages(descriptionImages);
            }
        }

        if (canSyncToExternalServer()) {
            if (descriptionChanged) {
                addPendingChange(ProductChangeType.DESCRIPTION);
            }
            if (productInfoChanged) {
                addPendingChange(ProductChangeType.PRODUCT_INFO);
            }
        }

        return newImageUrls;
    }

    /**
     * DETAIL 크롤링 데이터 VO로 업데이트
     *
     * @param crawlData DETAIL 크롤링 데이터 VO
     * @return 새로 업로드가 필요한 이미지 URL 목록
     */
    public List<String> updateFromDetailCrawlData(DetailCrawlData crawlData) {
        Instant now = crawlData.updatedAt();
        boolean productInfoChanged =
                detectDetailProductInfoChanges(
                        crawlData.category(),
                        crawlData.shippingInfo(),
                        crawlData.itemStatus(),
                        crawlData.originCountry(),
                        crawlData.shippingLocation());

        this.brandCode = crawlData.brandCode();
        this.category = crawlData.category();
        this.shippingInfo = crawlData.shippingInfo();
        this.itemStatus = crawlData.itemStatus();
        this.originCountry = crawlData.originCountry();
        this.shippingLocation = crawlData.shippingLocation();
        this.crawlCompletionStatus = this.crawlCompletionStatus.withDetailCrawled(now);
        this.updatedAt = now;

        List<String> newImageUrls = Collections.emptyList();

        boolean descriptionChanged = hasDescriptionChanged(crawlData.descriptionMarkUp());
        if (descriptionChanged) {
            this.originalDescriptionMarkUp = crawlData.descriptionMarkUp();
            this.descriptionMarkUp = crawlData.descriptionMarkUp();

            List<String> descriptionImages = crawlData.descriptionImages();
            if (descriptionImages != null && !descriptionImages.isEmpty()) {
                newImageUrls = this.images.getNewDescriptionImageUrls(descriptionImages);
                this.images = this.images.replaceDescriptionImages(descriptionImages);
            }
        }

        if (canSyncToExternalServer()) {
            if (descriptionChanged) {
                addPendingChange(ProductChangeType.DESCRIPTION);
            }
            if (productInfoChanged) {
                addPendingChange(ProductChangeType.PRODUCT_INFO);
            }
        }

        return newImageUrls;
    }

    /**
     * 상세 설명 변경 여부 확인
     *
     * <p>원본 디스크립션(originalDescriptionMarkUp)과 비교합니다. 저장된 descriptionMarkUp은 S3 URL로 치환되어 있으므로 비교
     * 기준으로 사용하면 안 됩니다.
     *
     * @param newDescriptionMarkUp 새 상세 설명 HTML (크롤링된 원본)
     * @return 변경되었으면 true
     */
    private boolean hasDescriptionChanged(String newDescriptionMarkUp) {
        if (this.originalDescriptionMarkUp == null && newDescriptionMarkUp == null) {
            return false;
        }
        if (this.originalDescriptionMarkUp == null || newDescriptionMarkUp == null) {
            return true;
        }
        return !this.originalDescriptionMarkUp.equals(newDescriptionMarkUp);
    }

    // === OPTION 업데이트 ===

    /**
     * OPTION 크롤링 결과로 업데이트
     *
     * @param options 옵션 목록
     * @param now 현재 시각
     */
    public void updateFromOption(ProductOptions options, Instant now) {
        boolean hasChanges = this.options != null && this.options.hasChanges(options);

        this.options = options;
        this.crawlCompletionStatus = this.crawlCompletionStatus.withOptionCrawled(now);
        this.updatedAt = now;

        if (hasChanges && canSyncToExternalServer()) {
            addPendingChange(ProductChangeType.OPTION_STOCK);
        }
    }

    /**
     * OPTION 크롤링 데이터 VO로 업데이트
     *
     * @param crawlData OPTION 크롤링 데이터 VO
     */
    public void updateFromOptionCrawlData(OptionCrawlData crawlData) {
        boolean hasChanges = this.options != null && this.options.hasChanges(crawlData.options());

        Instant now = crawlData.updatedAt();
        this.options = crawlData.options();
        this.crawlCompletionStatus = this.crawlCompletionStatus.withOptionCrawled(now);
        this.updatedAt = now;

        if (hasChanges && canSyncToExternalServer()) {
            addPendingChange(ProductChangeType.OPTION_STOCK);
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
     * <p>이미지 컬렉션과 상세 설명 HTML 내의 URL을 동시에 교체합니다.
     *
     * @param originalUrl 원본 URL
     * @param s3Url S3 URL
     * @param now 현재 시각
     */
    public void markImageAsUploaded(String originalUrl, String s3Url, Instant now) {
        if (this.images != null) {
            this.images = this.images.updateS3Url(originalUrl, s3Url);
        }

        // 상세 설명 HTML 내의 URL도 교체
        if (this.descriptionMarkUp != null && !this.descriptionMarkUp.isEmpty()) {
            this.descriptionMarkUp = this.descriptionMarkUp.replace(originalUrl, s3Url);
        }

        this.updatedAt = now;
    }

    /** 모든 이미지가 업로드 완료되었는지 확인 */
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
        return (!this.pendingChanges.isEmpty() || this.needsSync) && canSyncToExternalServer();
    }

    /**
     * 외부 서버 동기화 완료 처리 (CREATE 전용 - 전체 초기화)
     *
     * @param externalProductId 외부 서버에서 할당받은 상품 ID
     * @param now 현재 시각
     */
    public void markAsSynced(Long externalProductId, Instant now) {
        this.externalProductId = externalProductId;
        this.lastSyncedAt = now;
        this.needsSync = false;
        this.pendingChanges.clear();
        this.updatedAt = now;
    }

    /**
     * 특정 변경 유형 동기화 완료 처리 (부분 동기화 완료용)
     *
     * @param changeTypes 동기화 완료된 변경 유형들
     * @param now 현재 시각
     */
    public void markChangesSynced(Set<ProductChangeType> changeTypes, Instant now) {
        this.pendingChanges.removeAll(changeTypes);
        this.lastSyncedAt = now;
        if (this.pendingChanges.isEmpty()) {
            this.needsSync = false;
        }
        this.updatedAt = now;
    }

    /**
     * 동기화 실패 처리
     *
     * @param now 현재 시각
     */
    public void markSyncFailed(Instant now) {
        this.updatedAt = now;
    }

    /**
     * 외부 서버에 등록된 상품인지 확인
     *
     * @return 외부 상품 ID가 있으면 true
     */
    public boolean isRegisteredToExternalServer() {
        return this.externalProductId != null;
    }

    // === Soft Delete ===

    /**
     * 상품 소프트 삭제
     *
     * @param occurredAt 삭제 발생 시각
     */
    public void delete(Instant occurredAt) {
        this.deletionStatus = DeletionStatus.deletedAt(occurredAt);
        this.updatedAt = occurredAt;
    }

    /**
     * 소프트 삭제된 상품 복원
     *
     * @param occurredAt 복원 발생 시각
     */
    public void restore(Instant occurredAt) {
        this.deletionStatus = DeletionStatus.active();
        this.updatedAt = occurredAt;
    }

    /** 삭제 여부 확인 */
    public boolean isDeleted() {
        return this.deletionStatus != null && this.deletionStatus.isDeleted();
    }

    // === 상태 조회 ===

    /** 품절 여부 확인 */
    public boolean isSoldOut() {
        return this.options != null && this.options.isAllSoldOut();
    }

    /** 총 재고 수량 */
    public int getTotalStock() {
        return this.options != null ? this.options.getTotalStock() : 0;
    }

    /** 보류 중인 변경 유형 목록 (불변) */
    public Set<ProductChangeType> getPendingChanges() {
        return Collections.unmodifiableSet(pendingChanges);
    }

    // === 내부 헬퍼 ===

    private void addPendingChange(ProductChangeType changeType) {
        this.needsSync = true;
        if (isRegisteredToExternalServer()) {
            this.pendingChanges.add(changeType);
        }
    }

    private boolean detectDetailProductInfoChanges(
            ProductCategory newCategory,
            ShippingInfo newShippingInfo,
            String newItemStatus,
            String newOriginCountry,
            String newShippingLocation) {
        boolean categoryChanged =
                (this.category == null && newCategory != null)
                        || (this.category != null && !this.category.equals(newCategory));
        boolean shippingInfoChanged =
                (this.shippingInfo == null && newShippingInfo != null)
                        || (this.shippingInfo != null
                                && !this.shippingInfo.equals(newShippingInfo));
        boolean itemStatusChanged = !equalsNullSafe(this.itemStatus, newItemStatus);
        boolean originCountryChanged = !equalsNullSafe(this.originCountry, newOriginCountry);
        boolean shippingLocationChanged =
                !equalsNullSafe(this.shippingLocation, newShippingLocation);

        return categoryChanged
                || shippingInfoChanged
                || itemStatusChanged
                || originCountryChanged
                || shippingLocationChanged;
    }

    // === 유틸리티 ===

    private boolean equalsNullSafe(String a, String b) {
        String normA = (a == null || a.isEmpty()) ? "" : a;
        String normB = (b == null || b.isEmpty()) ? "" : b;
        return normA.equals(normB);
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

    public long getBrandCode() {
        return brandCode;
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

    public String getOriginalDescriptionMarkUp() {
        return originalDescriptionMarkUp;
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

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public boolean isNeedsSync() {
        return needsSync;
    }

    public DeletionStatus getDeletionStatus() {
        return deletionStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    // === 도메인 이벤트 ===

    /**
     * 도메인 이벤트 등록
     *
     * @param event 등록할 도메인 이벤트
     */
    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * 도메인 이벤트 폴링 (읽기 + 초기화)
     *
     * <p>이벤트 목록을 불변 복사본으로 반환하고 내부 목록을 비웁니다.
     *
     * @return 불변 이벤트 목록
     */
    public List<DomainEvent> pollEvents() {
        List<DomainEvent> events = Collections.unmodifiableList(new ArrayList<>(domainEvents));
        domainEvents.clear();
        return events;
    }
}
