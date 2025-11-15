package com.ryuqq.crawlinghub.application.product.dto.query;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ProductSnapshot Query DTO
 *
 * <p><strong>CQRS Query DTO - QueryDSL Projection 전용 ⭐</strong></p>
 * <ul>
 *   <li>✅ Query 작업 전용 (읽기 전용)</li>
 *   <li>✅ QueryDSL Projections.constructor()로 직접 매핑</li>
 *   <li>✅ Entity → Domain 변환 없이 직접 반환</li>
 *   <li>✅ N+1 문제 방지</li>
 * </ul>
 *
 * <p><strong>용도:</strong></p>
 * <ul>
 *   <li>ProductSnapshot 조회 결과 반환</li>
 *   <li>ProductSnapshotQueryDslRepository에서 생성</li>
 *   <li>ProductSnapshotQueryAdapter에서 반환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-11
 */
public class ProductSnapshotQueryDto {

    private final Long id;
    private final Long mustItItemNo;
    private final Long sellerId;
    private final String productName;
    private final Long price;
    private final String mainImageUrl;
    private final String options;
    private final Integer totalStock;
    private final String productInfo;
    private final String shipping;
    private final String detailInfo;
    private final LocalDateTime lastSyncedAt;
    private final Integer version;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * QueryDSL Projections.constructor() 전용 생성자
     *
     * @param id 상품 스냅샷 ID
     * @param mustItItemNo 머스트잇 상품 번호
     * @param sellerId 셀러 ID (Long FK)
     * @param productName 상품명
     * @param price 가격
     * @param mainImageUrl 메인 이미지 URL
     * @param options 옵션 JSON
     * @param totalStock 총 재고
     * @param productInfo 상품 정보 JSON
     * @param shipping 배송 정보 JSON
     * @param detailInfo 상세 정보 JSON
     * @param lastSyncedAt 마지막 동기화 시간
     * @param version 버전
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     */
    public ProductSnapshotQueryDto(
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
        this.id = Objects.requireNonNull(id, "id must not be null");
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
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductSnapshotQueryDto that = (ProductSnapshotQueryDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductSnapshotQueryDto{" +
            "id=" + id +
            ", mustItItemNo=" + mustItItemNo +
            ", sellerId=" + sellerId +
            ", productName='" + productName + '\'' +
            ", version=" + version +
            '}';
    }
}
