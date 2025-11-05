package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.common.entity.BaseAuditEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;


import java.time.LocalDateTime;

/**
 * ProductCountHistoryEntity - 상품 수 이력 JPA Entity
 *
 * <p><strong>🆕 변경사항 (v2):</strong></p>
 * <ul>
 *   <li>❌ previousCount 컬럼 제거</li>
 *   <li>✅ executedDate + productCount만 저장</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 금지</li>
 *   <li>✅ Pure Java getter/setter</li>
 *   <li>❌ JPA 관계 어노테이션 금지 (@ManyToOne 등)</li>
 *   <li>✅ Long FK 전략 (sellerId)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Entity
@Table(
    name = "product_count_history",
    indexes = {
        @Index(name = "idx_seller_id_executed_date", columnList = "seller_id, executed_date")
    }
)
public class ProductCountHistoryEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId; // Long FK ⭐

    @Column(name = "product_count", nullable = false)
    private Integer productCount; // 카운트 된 수 ⭐

    @Column(name = "executed_date", nullable = false)
    private LocalDateTime executedDate; // 실행 날짜 ⭐

    /**
     * JPA 기본 생성자 (protected)
     */
    protected ProductCountHistoryEntity() {
        super();
    }

    /**
     * Protected Constructor - 신규 Entity 생성 (ID 없음)
     *
     * @param sellerId 셀러 ID (Long FK)
     * @param productCount 카운트 된 수
     * @param executedDate 실행 날짜
     * @param createdAt 생성 일시
     */
    protected ProductCountHistoryEntity(
        Long sellerId,
        Integer productCount,
        LocalDateTime executedDate,
        LocalDateTime createdAt
    ) {
        super(createdAt, createdAt);
        this.id = null;
        this.sellerId = sellerId;
        this.productCount = productCount;
        this.executedDate = executedDate;
    }

    /**
     * Private Constructor - 기존 Entity 복원 (ID 있음)
     *
     * @param id ID
     * @param sellerId 셀러 ID (Long FK)
     * @param productCount 카운트 된 수
     * @param executedDate 실행 날짜
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     */
    private ProductCountHistoryEntity(
        Long id,
        Long sellerId,
        Integer productCount,
        LocalDateTime executedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sellerId = sellerId;
        this.productCount = productCount;
        this.executedDate = executedDate;
    }

    /**
     * Static Factory Method - 새로운 Entity 생성
     *
     * @param sellerId 셀러 ID
     * @param productCount 카운트 된 수
     * @param executedDate 실행 날짜
     * @return ProductCountHistoryEntity
     */
    public static ProductCountHistoryEntity create(
        Long sellerId,
        Integer productCount,
        LocalDateTime executedDate
    ) {
        return new ProductCountHistoryEntity(
            sellerId,
            productCount,
            executedDate,
            LocalDateTime.now()
        );
    }

    /**
     * Static Factory Method - 기존 Entity 복원
     *
     * @param id ID
     * @param sellerId 셀러 ID
     * @param productCount 카운트 된 수
     * @param executedDate 실행 날짜
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return ProductCountHistoryEntity
     */
    public static ProductCountHistoryEntity reconstitute(
        Long id,
        Long sellerId,
        Integer productCount,
        LocalDateTime executedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductCountHistoryEntity(
            id,
            sellerId,
            productCount,
            executedDate,
            createdAt,
            updatedAt
        );
    }

    // Getters (Pure Java)
    public Long getId() {
        return id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public LocalDateTime getExecutedDate() {
        return executedDate;
    }
}

