package com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.entity.BaseAuditEntity;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * SellerJpaEntity - Seller JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 seller 테이블과 매핑됩니다.
 *
 * <p><strong>BaseAuditEntity 상속:</strong>
 *
 * <ul>
 *   <li>공통 감사 필드 상속: createdAt, updatedAt
 *   <li>시간 정보는 Domain에서 관리하여 전달
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
@Table(name = "seller")
public class SellerJpaEntity extends BaseAuditEntity {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 머스트잇 셀러명 (UNIQUE)
     *
     * <p>중복 불가 제약 조건이 있는 필드입니다.
     */
    @Column(name = "must_it_seller_name", nullable = false, unique = true, length = 100)
    private String mustItSellerName;

    /**
     * 커머스 셀러명 (UNIQUE)
     *
     * <p>중복 불가 제약 조건이 있는 필드입니다.
     */
    @Column(name = "seller_name", nullable = false, unique = true, length = 100)
    private String sellerName;

    /** 셀러 상태 (ACTIVE/INACTIVE) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SellerStatus status;

    /**
     * JPA 기본 생성자 (protected)
     *
     * <p>JPA 스펙 요구사항으로 반드시 필요합니다.
     */
    protected SellerJpaEntity() {}

    /**
     * 전체 필드 생성자 (private)
     *
     * <p>직접 호출 금지, of() 스태틱 메서드로만 생성하세요.
     *
     * @param id 기본 키
     * @param mustItSellerName 머스트잇 셀러명
     * @param sellerName 커머스 셀러명
     * @param status 셀러 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     */
    private SellerJpaEntity(
            Long id,
            String mustItSellerName,
            String sellerName,
            SellerStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.mustItSellerName = mustItSellerName;
        this.sellerName = sellerName;
        this.status = status;
    }

    /**
     * of() 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * <p>Entity 생성은 반드시 이 메서드를 통해서만 가능합니다.
     *
     * <p>Mapper에서 Domain → Entity 변환 시 사용합니다.
     *
     * @param id 기본 키
     * @param mustItSellerName 머스트잇 셀러명
     * @param sellerName 커머스 셀러명
     * @param status 셀러 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return SellerJpaEntity 인스턴스
     */
    public static SellerJpaEntity of(
            Long id,
            String mustItSellerName,
            String sellerName,
            SellerStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new SellerJpaEntity(id, mustItSellerName, sellerName, status, createdAt, updatedAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public String getMustItSellerName() {
        return mustItSellerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public SellerStatus getStatus() {
        return status;
    }
}
