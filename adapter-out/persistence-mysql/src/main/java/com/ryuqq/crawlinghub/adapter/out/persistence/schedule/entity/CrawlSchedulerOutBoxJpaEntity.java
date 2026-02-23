package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity;

import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

/**
 * CrawlSchedulerOutBoxJpaEntity - CrawlSchedulerOutBox JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 crawl_scheduler_outbox 테이블과 매핑됩니다.
 *
 * <p><strong>Optimistic Locking:</strong>
 *
 * <ul>
 *   <li>@Version 어노테이션으로 동시성 제어
 *   <li>아웃박스 처리 시 충돌 방지
 * </ul>
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>historyId는 Long 타입으로 직접 관리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "crawl_scheduler_outbox")
public class CrawlSchedulerOutBoxJpaEntity {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 히스토리 ID (Long FK 전략) */
    @Column(name = "history_id", nullable = false)
    private Long historyId;

    /** 아웃박스 상태 (PENDING/COMPLETED/FAILED) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CrawlSchedulerOubBoxStatus status;

    /** 스케줄러 ID */
    @Column(name = "scheduler_id", nullable = false)
    private Long schedulerId;

    /** 셀러 ID */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /** 스케줄러 이름 */
    @Column(name = "scheduler_name", nullable = false, length = 100)
    private String schedulerName;

    /** 크론 표현식 */
    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    /** 스케줄러 상태 (ACTIVE/INACTIVE) */
    @Column(name = "scheduler_status", nullable = false, length = 20)
    private String schedulerStatus;

    /** 에러 메시지 (실패 시) */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /** 버전 (Optimistic Locking) */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 처리 일시 */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** JPA 기본 생성자 (protected) */
    protected CrawlSchedulerOutBoxJpaEntity() {}

    /** 전체 필드 생성자 (private) */
    private CrawlSchedulerOutBoxJpaEntity(
            Long id,
            Long historyId,
            CrawlSchedulerOubBoxStatus status,
            Long schedulerId,
            Long sellerId,
            String schedulerName,
            String cronExpression,
            String schedulerStatus,
            String errorMessage,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        this.id = id;
        this.historyId = historyId;
        this.status = status;
        this.schedulerId = schedulerId;
        this.sellerId = sellerId;
        this.schedulerName = schedulerName;
        this.cronExpression = cronExpression;
        this.schedulerStatus = schedulerStatus;
        this.errorMessage = errorMessage;
        this.version = version;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * of() 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * @param id 기본 키
     * @param historyId 히스토리 ID
     * @param status 아웃박스 상태
     * @param schedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param schedulerStatus 스케줄러 상태
     * @param errorMessage 에러 메시지
     * @param version 버전
     * @param createdAt 생성 일시
     * @param processedAt 처리 일시
     * @return CrawlSchedulerOutBoxJpaEntity 인스턴스
     */
    public static CrawlSchedulerOutBoxJpaEntity of(
            Long id,
            Long historyId,
            CrawlSchedulerOubBoxStatus status,
            Long schedulerId,
            Long sellerId,
            String schedulerName,
            String cronExpression,
            String schedulerStatus,
            String errorMessage,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        return new CrawlSchedulerOutBoxJpaEntity(
                id,
                historyId,
                status,
                schedulerId,
                sellerId,
                schedulerName,
                cronExpression,
                schedulerStatus,
                errorMessage,
                version,
                createdAt,
                processedAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public CrawlSchedulerOubBoxStatus getStatus() {
        return status;
    }

    public Long getSchedulerId() {
        return schedulerId;
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

    public String getSchedulerStatus() {
        return schedulerStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
