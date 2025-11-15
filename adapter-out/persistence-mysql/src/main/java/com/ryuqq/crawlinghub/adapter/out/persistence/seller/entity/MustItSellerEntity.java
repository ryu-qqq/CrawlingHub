package com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.crawlinghub.domain.seller.SellerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * 머스트잇 셀러 JPA Entity
 * <p>
 * Long FK 전략을 사용하여 관계 어노테이션을 배제하고
 * Long 타입의 FK만 사용합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Entity
@Table(
        name = "mustIt_seller",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seller_code", columnNames = "seller_code")
        },
        indexes = {
                @Index(name = "idx_seller_code", columnList = "seller_code"),
                @Index(name = "idx_status", columnList = "status")
        }
)
public class MustItSellerEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_code", nullable = false, unique = true, length = 100)
    private String sellerCode;

    @Column(name = "seller_name", nullable = false, length = 255)
    private String sellerName;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SellerStatus status;

    @Column(name = "total_product_count", nullable = false)
    private Integer totalProductCount;

    @Column(name = "last_crawled_at")
    private LocalDateTime lastCrawledAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected MustItSellerEntity() {
        super();
    }

    /**
     * 전체 필드 생성자 (private)
     * <p>
     * Static Factory Method에서만 호출됩니다.
     * </p>
     *
     * @param id                PK ID
     * @param sellerCode        셀러 코드
     * @param sellerName        셀러 이름
     * @param status            셀러 상태
     * @param totalProductCount 총 상품 수
     * @param lastCrawledAt     마지막 크롤링 시간
     */
    private MustItSellerEntity(
            Long id,
            String sellerCode,
            String sellerName,
            SellerStatus status,
            Integer totalProductCount,
            LocalDateTime lastCrawledAt
    ) {
        super();
        this.id = id;
        this.sellerCode = sellerCode;
        this.sellerName = sellerName;
        this.status = status;
        this.totalProductCount = totalProductCount;
        this.lastCrawledAt = lastCrawledAt;
    }

    /**
     * 신규 Entity 생성 (PK 없음)
     * <p>
     * 새로운 셀러를 생성할 때 사용합니다.
     * PK는 JPA가 자동으로 생성합니다.
     * </p>
     *
     * @param sellerCode        셀러 코드
     * @param sellerName        셀러 이름
     * @param status            셀러 상태
     * @param totalProductCount 총 상품 수
     * @param lastCrawledAt     마지막 크롤링 시간
     * @return 생성된 Entity
     * @throws IllegalArgumentException 파라미터 검증 실패 시
     */
    public static MustItSellerEntity create(
            String sellerCode,
            String sellerName,
            SellerStatus status,
            Integer totalProductCount,
            LocalDateTime lastCrawledAt
    ) {
        validateSellerCode(sellerCode);
        validateSellerName(sellerName);
        validateStatus(status);
        validateTotalProductCount(totalProductCount);

        return new MustItSellerEntity(
                null,
                sellerCode,
                sellerName,
                status,
                totalProductCount,
                lastCrawledAt
        );
    }

    /**
     * 기존 Entity 재구성 (조회 후 복원)
     * <p>
     * DB에서 조회된 데이터로 Entity를 재구성할 때 사용합니다.
     * </p>
     *
     * @param id                PK ID
     * @param sellerCode        셀러 코드
     * @param sellerName        셀러 이름
     * @param status            셀러 상태
     * @param totalProductCount 총 상품 수
     * @param lastCrawledAt     마지막 크롤링 시간
     * @return 재구성된 Entity
     */
    public static MustItSellerEntity reconstitute(
            Long id,
            String sellerCode,
            String sellerName,
            SellerStatus status,
            Integer totalProductCount,
            LocalDateTime lastCrawledAt
    ) {
        return new MustItSellerEntity(
                id,
                sellerCode,
                sellerName,
                status,
                totalProductCount,
                lastCrawledAt
        );
    }

    /**
     * sellerCode 유효성 검증
     *
     * @param sellerCode 검증할 셀러 코드
     * @throws IllegalArgumentException sellerCode가 null이거나 빈 문자열이거나 길이 초과인 경우
     */
    private static void validateSellerCode(String sellerCode) {
        if (sellerCode == null || sellerCode.isBlank()) {
            throw new IllegalArgumentException("sellerCode must not be null or blank");
        }
        if (sellerCode.length() > 100) {
            throw new IllegalArgumentException("sellerCode must not exceed 100 characters");
        }
    }

    /**
     * sellerName 유효성 검증
     *
     * @param sellerName 검증할 셀러 이름
     * @throws IllegalArgumentException sellerName이 null이거나 빈 문자열이거나 길이 초과인 경우
     */
    private static void validateSellerName(String sellerName) {
        if (sellerName == null || sellerName.isBlank()) {
            throw new IllegalArgumentException("sellerName must not be null or blank");
        }
        if (sellerName.length() > 255) {
            throw new IllegalArgumentException("sellerName must not exceed 255 characters");
        }
    }

    /**
     * status 유효성 검증
     *
     * @param status 검증할 셀러 상태
     * @throws IllegalArgumentException status가 null인 경우
     */
    private static void validateStatus(SellerStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }

    /**
     * totalProductCount 유효성 검증
     *
     * @param totalProductCount 검증할 총 상품 수
     * @throws IllegalArgumentException totalProductCount가 null이거나 음수인 경우
     */
    private static void validateTotalProductCount(Integer totalProductCount) {
        if (totalProductCount == null) {
            throw new IllegalArgumentException("totalProductCount must not be null");
        }
        if (totalProductCount < 0) {
            throw new IllegalArgumentException("totalProductCount must not be negative");
        }
    }

    public Long getId() {
        return id;
    }

    public String getSellerCode() {
        return sellerCode;
    }

    public String getSellerName() {
        return sellerName;
    }

    public SellerStatus getStatus() {
        return status;
    }

    public Integer getTotalProductCount() {
        return totalProductCount;
    }

    public LocalDateTime getLastCrawledAt() {
        return lastCrawledAt;
    }
}
