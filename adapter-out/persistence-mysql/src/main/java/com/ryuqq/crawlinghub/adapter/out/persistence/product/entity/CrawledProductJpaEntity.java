package com.ryuqq.crawlinghub.adapter.out.persistence.product.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * CrawledProductJpaEntity - CrawledProduct JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 crawled_product 테이블과 매핑됩니다.
 *
 * <p><strong>JSON 컬럼 전략:</strong>
 *
 * <ul>
 *   <li>복합 VO (ProductImages, ProductOptions, ProductCategory, ShippingInfo)는 JSON으로 저장
 *   <li>Mapper에서 JSON 직렬화/역직렬화 담당
 * </ul>
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지 (@ManyToOne, @OneToMany 등)
 *   <li>모든 외래키는 Long 타입으로 직접 관리
 * </ul>
 *
 * <p><strong>Lombok 금지:</strong>
 *
 * <ul>
 *   <li>Plain Java getter 사용
 *   <li>Setter 제공 금지
 *   <li>명시적 생성자 제공
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "crawled_product")
public class CrawledProductJpaEntity extends BaseAuditEntity {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 셀러 ID (FK) */
    @Column(name = "seller_id", nullable = false)
    private long sellerId;

    /** 상품 번호 */
    @Column(name = "item_no", nullable = false)
    private long itemNo;

    // === MINI_SHOP 데이터 ===

    /** 상품명 */
    @Column(name = "item_name", length = 500)
    private String itemName;

    /** 브랜드명 */
    @Column(name = "brand_name", length = 200)
    private String brandName;

    /** 원가 */
    @Column(name = "original_price")
    private Long originalPrice;

    /** 할인가 */
    @Column(name = "discount_price")
    private Long discountPrice;

    /** 할인율 */
    @Column(name = "discount_rate")
    private Integer discountRate;

    /** 이미지 정보 JSON (ProductImages) */
    @Lob
    @Column(name = "images_json", columnDefinition = "LONGTEXT")
    private String imagesJson;

    /** 무료 배송 여부 */
    @Column(name = "free_shipping", nullable = false)
    private boolean freeShipping;

    // === DETAIL 데이터 ===

    /** 카테고리 정보 JSON (ProductCategory) */
    @Column(name = "category_json", length = 1000)
    private String categoryJson;

    /** 배송 정보 JSON (ShippingInfo) */
    @Column(name = "shipping_info_json", length = 1000)
    private String shippingInfoJson;

    /** 상세 설명 HTML */
    @Lob
    @Column(name = "description_mark_up", columnDefinition = "LONGTEXT")
    private String descriptionMarkUp;

    /** 상품 상태 */
    @Column(name = "item_status", length = 50)
    private String itemStatus;

    /** 원산지 */
    @Column(name = "origin_country", length = 100)
    private String originCountry;

    /** 배송 출발지 */
    @Column(name = "shipping_location", length = 200)
    private String shippingLocation;

    // === OPTION 데이터 ===

    /** 옵션 정보 JSON (ProductOptions) */
    @Lob
    @Column(name = "options_json", columnDefinition = "LONGTEXT")
    private String optionsJson;

    // === 크롤링 완료 상태 ===

    /** MINI_SHOP 크롤링 완료 시각 */
    @Column(name = "mini_shop_crawled_at")
    private LocalDateTime miniShopCrawledAt;

    /** DETAIL 크롤링 완료 시각 */
    @Column(name = "detail_crawled_at")
    private LocalDateTime detailCrawledAt;

    /** OPTION 크롤링 완료 시각 */
    @Column(name = "option_crawled_at")
    private LocalDateTime optionCrawledAt;

    // === 외부 서버 동기화 상태 ===

    /** 외부 서버 상품 ID */
    @Column(name = "external_product_id")
    private Long externalProductId;

    /** 마지막 동기화 시각 */
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    /** 동기화 필요 여부 */
    @Column(name = "needs_sync", nullable = false)
    private boolean needsSync;

    /** JPA 기본 생성자 (protected) */
    protected CrawledProductJpaEntity() {}

    /** 전체 필드 생성자 (private) */
    private CrawledProductJpaEntity(
            Long id,
            long sellerId,
            long itemNo,
            String itemName,
            String brandName,
            Long originalPrice,
            Long discountPrice,
            Integer discountRate,
            String imagesJson,
            boolean freeShipping,
            String categoryJson,
            String shippingInfoJson,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            String optionsJson,
            LocalDateTime miniShopCrawledAt,
            LocalDateTime detailCrawledAt,
            LocalDateTime optionCrawledAt,
            Long externalProductId,
            LocalDateTime lastSyncedAt,
            boolean needsSync,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sellerId = sellerId;
        this.itemNo = itemNo;
        this.itemName = itemName;
        this.brandName = brandName;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
        this.imagesJson = imagesJson;
        this.freeShipping = freeShipping;
        this.categoryJson = categoryJson;
        this.shippingInfoJson = shippingInfoJson;
        this.descriptionMarkUp = descriptionMarkUp;
        this.itemStatus = itemStatus;
        this.originCountry = originCountry;
        this.shippingLocation = shippingLocation;
        this.optionsJson = optionsJson;
        this.miniShopCrawledAt = miniShopCrawledAt;
        this.detailCrawledAt = detailCrawledAt;
        this.optionCrawledAt = optionCrawledAt;
        this.externalProductId = externalProductId;
        this.lastSyncedAt = lastSyncedAt;
        this.needsSync = needsSync;
    }

    /** of() 스태틱 팩토리 메서드 (Mapper 전용) */
    public static CrawledProductJpaEntity of(
            Long id,
            long sellerId,
            long itemNo,
            String itemName,
            String brandName,
            Long originalPrice,
            Long discountPrice,
            Integer discountRate,
            String imagesJson,
            boolean freeShipping,
            String categoryJson,
            String shippingInfoJson,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            String optionsJson,
            LocalDateTime miniShopCrawledAt,
            LocalDateTime detailCrawledAt,
            LocalDateTime optionCrawledAt,
            Long externalProductId,
            LocalDateTime lastSyncedAt,
            boolean needsSync,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new CrawledProductJpaEntity(
                id,
                sellerId,
                itemNo,
                itemName,
                brandName,
                originalPrice,
                discountPrice,
                discountRate,
                imagesJson,
                freeShipping,
                categoryJson,
                shippingInfoJson,
                descriptionMarkUp,
                itemStatus,
                originCountry,
                shippingLocation,
                optionsJson,
                miniShopCrawledAt,
                detailCrawledAt,
                optionCrawledAt,
                externalProductId,
                lastSyncedAt,
                needsSync,
                createdAt,
                updatedAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public long getSellerId() {
        return sellerId;
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

    public Long getOriginalPrice() {
        return originalPrice;
    }

    public Long getDiscountPrice() {
        return discountPrice;
    }

    public Integer getDiscountRate() {
        return discountRate;
    }

    public String getImagesJson() {
        return imagesJson;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public String getCategoryJson() {
        return categoryJson;
    }

    public String getShippingInfoJson() {
        return shippingInfoJson;
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

    public String getOptionsJson() {
        return optionsJson;
    }

    public LocalDateTime getMiniShopCrawledAt() {
        return miniShopCrawledAt;
    }

    public LocalDateTime getDetailCrawledAt() {
        return detailCrawledAt;
    }

    public LocalDateTime getOptionCrawledAt() {
        return optionCrawledAt;
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
}
