package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity;

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
 * CrawlSchedulerHistoryJpaEntity - CrawlSchedulerHistory JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 crawl_scheduler_history 테이블과 매핑됩니다.
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지
 *   <li>crawlSchedulerId, sellerId는 Long 타입으로 직접 관리
 * </ul>
 *
 * <p><strong>불변 객체:</strong>
 *
 * <ul>
 *   <li>스케줄러 변경 이력은 한 번 저장되면 수정되지 않음
 *   <li>updatedAt 필드 없음
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "crawl_scheduler_history")
public class CrawlSchedulerHistoryJpaEntity {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 스케줄러 ID (Long FK 전략) */
    @Column(name = "crawl_scheduler_id", nullable = false)
    private Long crawlSchedulerId;

    /** 셀러 ID (Long FK 전략) */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /** 스케줄러 이름 */
    @Column(name = "scheduler_name", nullable = false, length = 100)
    private String schedulerName;

    /** 크론 표현식 */
    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    /** 스케줄러 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SchedulerStatus status;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** JPA 기본 생성자 (protected) */
    protected CrawlSchedulerHistoryJpaEntity() {}

    /** 전체 필드 생성자 (private) */
    private CrawlSchedulerHistoryJpaEntity(
            Long id,
            Long crawlSchedulerId,
            Long sellerId,
            String schedulerName,
            String cronExpression,
            SchedulerStatus status,
            LocalDateTime createdAt) {
        this.id = id;
        this.crawlSchedulerId = crawlSchedulerId;
        this.sellerId = sellerId;
        this.schedulerName = schedulerName;
        this.cronExpression = cronExpression;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * of() 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * @param id 기본 키
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @param createdAt 생성 일시
     * @return CrawlSchedulerHistoryJpaEntity 인스턴스
     */
    public static CrawlSchedulerHistoryJpaEntity of(
            Long id,
            Long crawlSchedulerId,
            Long sellerId,
            String schedulerName,
            String cronExpression,
            SchedulerStatus status,
            LocalDateTime createdAt) {
        return new CrawlSchedulerHistoryJpaEntity(
                id, crawlSchedulerId, sellerId, schedulerName, cronExpression, status, createdAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public Long getCrawlSchedulerId() {
        return crawlSchedulerId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
