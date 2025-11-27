package com.ryuqq.crawlinghub.adapter.out.persistence.task.entity;

import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * CrawlTaskOutboxJpaEntity - CrawlTaskOutbox JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 crawl_task_outbox 테이블과 매핑됩니다.
 *
 * <p><strong>Outbox 패턴:</strong>
 *
 * <ul>
 *   <li>CrawlTask와 같은 트랜잭션에서 저장
 *   <li>별도 스케줄러가 PENDING 상태 Outbox 조회 후 SQS 발행
 *   <li>발행 성공 시 SENT로 변경 또는 삭제
 * </ul>
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지
 *   <li>crawlTaskId는 Long 타입으로 직접 관리 (PK 역할)
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
@Table(name = "crawl_task_outbox")
public class CrawlTaskOutboxJpaEntity {

    /** 기본 키 - CrawlTask ID (1:1 관계이므로 Task ID를 PK로 사용) */
    @Id
    @Column(name = "crawl_task_id")
    private Long crawlTaskId;

    /** Idempotency Key (SQS 중복 발행 방지) */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    /** 발행 페이로드 (JSON) */
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    /** 현재 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    /** 재시도 횟수 */
    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 처리 일시 (발행 성공/실패 시각) */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** JPA 기본 생성자 (protected) */
    protected CrawlTaskOutboxJpaEntity() {}

    /** 전체 필드 생성자 (private) */
    private CrawlTaskOutboxJpaEntity(
            Long crawlTaskId,
            String idempotencyKey,
            String payload,
            OutboxStatus status,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        this.crawlTaskId = crawlTaskId;
        this.idempotencyKey = idempotencyKey;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * of() 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * <p>Entity 생성은 반드시 이 메서드를 통해서만 가능합니다.
     *
     * <p>Mapper에서 Domain → Entity 변환 시 사용합니다.
     *
     * @param crawlTaskId CrawlTask ID (PK)
     * @param idempotencyKey Idempotency Key
     * @param payload 발행 페이로드 (JSON)
     * @param status 현재 상태
     * @param retryCount 재시도 횟수
     * @param createdAt 생성 일시
     * @param processedAt 처리 일시
     * @return CrawlTaskOutboxJpaEntity 인스턴스
     */
    public static CrawlTaskOutboxJpaEntity of(
            Long crawlTaskId,
            String idempotencyKey,
            String payload,
            OutboxStatus status,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        return new CrawlTaskOutboxJpaEntity(
                crawlTaskId, idempotencyKey, payload, status, retryCount, createdAt, processedAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getCrawlTaskId() {
        return crawlTaskId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
