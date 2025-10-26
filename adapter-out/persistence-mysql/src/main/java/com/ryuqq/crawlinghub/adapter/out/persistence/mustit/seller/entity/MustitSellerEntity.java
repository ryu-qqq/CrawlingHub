package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Entity
@Table(
        name = "mustit_seller",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seller_id", columnNames = "seller_id")
        },
        indexes = {
                @Index(name = "idx_seller_id", columnList = "seller_id"),
                @Index(name = "idx_is_active", columnList = "is_active")
        }
)
public class MustitSellerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_id", nullable = false, unique = true, length = 100)
    private String sellerId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "interval_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private IntervalType intervalType;

    @Column(name = "interval_value", nullable = false)
    private Integer intervalValue;

    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected MustitSellerEntity() {
    }

    /**
     * Entity 생성자 (불변성 보장)
     *
     * @param basicInfo  기본 정보 (sellerId, name, isActive)
     * @param crawlInfo  크롤링 주기 정보 (type, value, cron)
     */
    public MustitSellerEntity(BasicInfo basicInfo, CrawlInfo crawlInfo) {
        this.sellerId = basicInfo.sellerId();
        this.name = basicInfo.name();
        this.isActive = basicInfo.isActive();
        this.intervalType = crawlInfo.intervalType();
        this.intervalValue = crawlInfo.intervalValue();
        this.cronExpression = crawlInfo.cronExpression();
    }

    /**
     * JPA 영속화 전 자동 호출
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * JPA 업데이트 전 자동 호출
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Entity 업데이트를 위한 정적 팩토리 메서드
     *
     * @param basicInfo 변경된 기본 정보
     * @param crawlInfo 변경된 크롤링 정보
     * @return 새로운 Entity 인스턴스
     */
    public MustitSellerEntity update(BasicInfo basicInfo, CrawlInfo crawlInfo) {
        MustitSellerEntity updated = new MustitSellerEntity(basicInfo, crawlInfo);
        updated.id = this.id;
        updated.createdAt = this.createdAt;
        return updated;
    }

    // Getters (불변 접근만 제공)

    public Long getId() {
        return id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public IntervalType getIntervalType() {
        return intervalType;
    }

    public Integer getIntervalValue() {
        return intervalValue;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 기본 정보 Record
     *
     * @param sellerId 셀러 ID
     * @param name     셀러명
     * @param isActive 활성 상태
     */
    public record BasicInfo(String sellerId, String name, Boolean isActive) {
    }

    /**
     * 크롤링 정보 Record
     *
     * @param intervalType   크롤링 주기 타입
     * @param intervalValue  크롤링 주기 값
     * @param cronExpression cron 표현식
     */
    public record CrawlInfo(
            IntervalType intervalType,
            Integer intervalValue,
            String cronExpression
    ) {
    }

    /**
     * Interval Type Enum (Entity용)
     */
    public enum IntervalType {
        HOURLY,
        DAILY,
        WEEKLY
    }
}
