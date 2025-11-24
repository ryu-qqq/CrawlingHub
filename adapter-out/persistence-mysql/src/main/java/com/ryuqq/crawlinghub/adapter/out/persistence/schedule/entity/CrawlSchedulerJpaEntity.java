package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.entity.BaseAuditEntity;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
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
 * CrawlSchedulerJpaEntity - CrawlScheduler JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 crawl_scheduler 테이블과 매핑됩니다.
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
 *   <li>sellerId는 Long 타입으로 직접 관리
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
@Table(name = "crawl_scheduler")
public class CrawlSchedulerJpaEntity extends BaseAuditEntity {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 셀러 ID (Long FK 전략) */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /** 스케줄러 이름 (셀러별 UNIQUE) */
    @Column(name = "scheduler_name", nullable = false, length = 100)
    private String schedulerName;

    /** 크론 표현식 */
    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    /** 스케줄러 상태 (ACTIVE/INACTIVE) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SchedulerStatus status;

    /**
     * JPA 기본 생성자 (protected)
     *
     * <p>JPA 스펙 요구사항으로 반드시 필요합니다.
     */
    protected CrawlSchedulerJpaEntity() {}

    /**
     * 전체 필드 생성자 (private)
     *
     * <p>직접 호출 금지, of() 스태틱 메서드로만 생성하세요.
     *
     * @param id 기본 키
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     */
    private CrawlSchedulerJpaEntity(
            Long id,
            Long sellerId,
            String schedulerName,
            String cronExpression,
            SchedulerStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sellerId = sellerId;
        this.schedulerName = schedulerName;
        this.cronExpression = cronExpression;
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
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return CrawlSchedulerJpaEntity 인스턴스
     */
    public static CrawlSchedulerJpaEntity of(
            Long id,
            Long sellerId,
            String schedulerName,
            String cronExpression,
            SchedulerStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new CrawlSchedulerJpaEntity(
                id, sellerId, schedulerName, cronExpression, status, createdAt, updatedAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public SchedulerStatus getStatus() {
        return status;
    }
}
