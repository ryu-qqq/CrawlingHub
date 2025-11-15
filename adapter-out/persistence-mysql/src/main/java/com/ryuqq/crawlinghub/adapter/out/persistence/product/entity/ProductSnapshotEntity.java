package com.ryuqq.crawlinghub.adapter.out.persistence.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.ryuqq.crawlinghub.adapter.out.persistence.common.entity.BaseAuditEntity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ProductSnapshot JPA Entity
 *
 * <p>테이블: product_snapshot</p>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java</li>
 *   <li>✅ 3-생성자 패턴: no-args, create, reconstitute</li>
 *   <li>✅ Long FK 전략 - sellerId는 Long 타입</li>
 *   <li>✅ 불변성 - final 필드 사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Entity
@Table(
    name = "product_snapshot",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"must_it_item_no", "seller_id"})
    },
    indexes = {
        @Index(name = "idx_last_synced", columnList = "last_synced_at")
    }
)
public class ProductSnapshotEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column(name = "must_it_item_no", nullable = false)
    private final Long mustItItemNo;

    @Column(name = "seller_id", nullable = false)
    private final Long sellerId;

    // 미니샵 데이터
    @Column(name = "product_name", length = 500)
    private final String productName;

    @Column(name = "price")
    private final Long price;

    @Column(name = "main_image_url", length = 1000)
    private final String mainImageUrl;

    // 옵션 데이터
    @Column(name = "options", columnDefinition = "JSON")
    private final String options;

    @Column(name = "total_stock")
    private final Integer totalStock;

    // 상세 데이터
    @Column(name = "product_info", columnDefinition = "JSON")
    private final String productInfo;

    @Column(name = "shipping", columnDefinition = "JSON")
    private final String shipping;

    @Column(name = "detail_info", columnDefinition = "JSON")
    private final String detailInfo;

    // 메타 정보
    @Column(name = "last_synced_at")
    private final LocalDateTime lastSyncedAt;

    @Column(name = "version", nullable = false)
    private final Integer version;

    /**
     * No-args 생성자 (JPA 필수)
     */
    protected ProductSnapshotEntity() {
        super();
        this.id = null;
        this.mustItItemNo = null;
        this.sellerId = null;
        this.productName = null;
        this.price = null;
        this.mainImageUrl = null;
        this.options = null;
        this.totalStock = null;
        this.productInfo = null;
        this.shipping = null;
        this.detailInfo = null;
        this.lastSyncedAt = null;
        this.version = null;
    }

    /**
     * 신규 생성용 생성자 (ID 없음)
     */
    protected ProductSnapshotEntity(
        Long mustItItemNo,
        Long sellerId
    ) {
        super();
        this.id = null;
        this.mustItItemNo = Objects.requireNonNull(mustItItemNo, "mustItItemNo must not be null");
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.productName = null;
        this.price = null;
        this.mainImageUrl = null;
        this.options = null;
        this.totalStock = null;
        this.productInfo = null;
        this.shipping = null;
        this.detailInfo = null;
        this.lastSyncedAt = null;
        this.version = 1;
        initializeAuditFields();
    }

    /**
     * Static Factory Method - 신규 생성
     */
    public static ProductSnapshotEntity create(
        Long mustItItemNo,
        Long sellerId
    ) {
        return new ProductSnapshotEntity(mustItItemNo, sellerId);
    }

    /**
     * DB reconstitute용 전체 생성자
     */
    private ProductSnapshotEntity(
        Long id,
        Long mustItItemNo,
        Long sellerId,
        String productName,
        Long price,
        String mainImageUrl,
        String options,
        Integer totalStock,
        String productInfo,
        String shipping,
        String detailInfo,
        LocalDateTime lastSyncedAt,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.mustItItemNo = Objects.requireNonNull(mustItItemNo, "mustItItemNo must not be null");
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.productName = productName;
        this.price = price;
        this.mainImageUrl = mainImageUrl;
        this.options = options;
        this.totalStock = totalStock;
        this.productInfo = productInfo;
        this.shipping = shipping;
        this.detailInfo = detailInfo;
        this.lastSyncedAt = lastSyncedAt;
        this.version = Objects.requireNonNull(version, "version must not be null");
    }

    /**
     * Static Factory Method - DB reconstitute
     */
    public static ProductSnapshotEntity reconstitute(
        Long id,
        Long mustItItemNo,
        Long sellerId,
        String productName,
        Long price,
        String mainImageUrl,
        String options,
        Integer totalStock,
        String productInfo,
        String shipping,
        String detailInfo,
        LocalDateTime lastSyncedAt,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductSnapshotEntity(
            id, mustItItemNo, sellerId,
            productName, price, mainImageUrl,
            options, totalStock,
            productInfo, shipping, detailInfo,
            lastSyncedAt, version,
            createdAt, updatedAt
        );
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getMustItItemNo() {
        return mustItItemNo;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getPrice() {
        return price;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public String getOptions() {
        return options;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public String getProductInfo() {
        return productInfo;
    }

    public String getShipping() {
        return shipping;
    }

    public String getDetailInfo() {
        return detailInfo;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductSnapshotEntity that = (ProductSnapshotEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductSnapshotEntity{" +
            "id=" + id +
            ", mustItItemNo=" + mustItItemNo +
            ", sellerId=" + sellerId +
            ", version=" + version +
            '}';
    }
}

