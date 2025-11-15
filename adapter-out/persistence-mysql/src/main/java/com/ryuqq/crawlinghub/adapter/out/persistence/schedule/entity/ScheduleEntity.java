package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity;

import com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 크롤링 스케줄 JPA Entity
 *
 * <p>테이블: crawl_schedule</p>
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
 * @since 2025-11-06
 */
@Entity
@Table(name = "crawl_schedule")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ScheduleStatus status;

    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * No-args 생성자 (JPA 필수)
     */
    protected ScheduleEntity() {
        this.id = null;
        this.sellerId = null;
        this.cronExpression = null;
        this.status = null;
        this.nextExecutionTime = null;
        this.lastExecutedAt = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    /**
     * 신규 생성용 생성자 (ID 없음)
     *
     * @param sellerId 셀러 ID
     * @param cronExpression Cron 표현식
     * @param status 스케줄 상태
     * @param nextExecutionTime 다음 실행 시간
     */
    protected ScheduleEntity(
        Long sellerId,
        String cronExpression,
        ScheduleStatus status,
        LocalDateTime nextExecutionTime
    ) {
        this.id = null;
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.cronExpression = Objects.requireNonNull(cronExpression, "cronExpression must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.nextExecutionTime = nextExecutionTime;
        this.lastExecutedAt = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Static Factory Method - 신규 생성
     *
     * @param sellerId 셀러 ID
     * @param cronExpression Cron 표현식
     * @param status 스케줄 상태
     * @param nextExecutionTime 다음 실행 시간
     * @return ScheduleEntity
     */
    public static ScheduleEntity create(
        Long sellerId,
        String cronExpression,
        ScheduleStatus status,
        LocalDateTime nextExecutionTime
    ) {
        return new ScheduleEntity(sellerId, cronExpression, status, nextExecutionTime);
    }

    /**
     * DB reconstitute용 전체 생성자
     *
     * @param id ID
     * @param sellerId 셀러 ID
     * @param cronExpression Cron 표현식
     * @param status 스케줄 상태
     * @param nextExecutionTime 다음 실행 시간
     * @param lastExecutedAt 마지막 실행 시간
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     */
    private ScheduleEntity(
        Long id,
        Long sellerId,
        String cronExpression,
        ScheduleStatus status,
        LocalDateTime nextExecutionTime,
        LocalDateTime lastExecutedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.cronExpression = Objects.requireNonNull(cronExpression, "cronExpression must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.nextExecutionTime = nextExecutionTime;
        this.lastExecutedAt = lastExecutedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * Static Factory Method - DB reconstitute
     *
     * @param id ID
     * @param sellerId 셀러 ID
     * @param cronExpression Cron 표현식
     * @param status 스케줄 상태
     * @param nextExecutionTime 다음 실행 시간
     * @param lastExecutedAt 마지막 실행 시간
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @return ScheduleEntity
     */
    public static ScheduleEntity reconstitute(
        Long id,
        Long sellerId,
        String cronExpression,
        ScheduleStatus status,
        LocalDateTime nextExecutionTime,
        LocalDateTime lastExecutedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ScheduleEntity(
            id, sellerId, cronExpression, status,
            nextExecutionTime, lastExecutedAt,
            createdAt, updatedAt
        );
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public LocalDateTime getNextExecutionTime() {
        return nextExecutionTime;
    }

    public LocalDateTime getLastExecutedAt() {
        return lastExecutedAt;
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
        ScheduleEntity that = (ScheduleEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ScheduleEntity{" +
            "id=" + id +
            ", sellerId=" + sellerId +
            ", cronExpression='" + cronExpression + '\'' +
            ", status=" + status +
            ", nextExecutionTime=" + nextExecutionTime +
            ", lastExecutedAt=" + lastExecutedAt +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
