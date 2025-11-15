package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.crawlinghub.domain.schedule.outbox.EventType;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
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
 * 셀러 크롤링 스케줄 Outbox JPA Entity
 * <p>
 * Orchestrator SDK의 Write-Ahead Log (WAL) 패턴을 위한 Outbox 테이블입니다.
 * </p>
 * <p>
 * Long FK 전략: seller_id는 Long 타입으로 Seller Entity의 PK를 참조합니다.
 * </p>
 * <p>
 * 주요 필드:
 * <ul>
 *   <li>opId: Orchestrator OpId (UUID) - 초기 저장 시 null, Orchestrator.submit() 후 업데이트</li>
 *   <li>sellerId: Long FK - Seller Entity의 PK</li>
 *   <li>idemKey: Idempotency Key - 중복 실행 방지 (sellerId + eventType)</li>
 *   <li>eventType: EventBridge 이벤트 타입 Enum (REGISTER, UPDATE, DELETE)</li>
 *   <li>payload: EventBridge Schedule 생성/수정을 위한 JSON 페이로드</li>
 *   <li>operationState: 작업 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)</li>
 *   <li>walState: WAL 상태 (PENDING, COMPLETED)</li>
 *   <li>retryPolicy: 재시도 정책 (retryCount, maxRetries, timeoutMillis)</li>
 * </ul>
 * </p>
 * <p>
 * 제거된 필드:
 * <ul>
 *   <li>domain: Domain 모델에서 상수로 관리 ("SELLER_CRAWL_SCHEDULE")</li>
 *   <li>bizKey: 동적 생성으로 변경 (Domain 모델의 getBizKey() 메서드)</li>
 * </ul>
 * </p>
 *
 * @author windsurf
 * @since 1.0.0
 */
@Entity
@Table(
        name = "seller_crawl_schedule_outbox",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_op_id", columnNames = "op_id"),
                @UniqueConstraint(name = "uk_idem_key", columnNames = "idem_key")
        },
        indexes = {
                @Index(name = "idx_seller_id", columnList = "seller_id"),
                @Index(name = "idx_operation_state", columnList = "operation_state"),
                @Index(name = "idx_wal_state", columnList = "wal_state"),
                @Index(name = "idx_created_at", columnList = "created_at")
        }
)
public class ScheduleOutboxEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "op_id", nullable = true, unique = true, length = 36)
    private String opId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "idem_key", nullable = false, unique = true, length = 100)
    private String idemKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "outcome_json", columnDefinition = "TEXT")
    private String outcomeJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_state", nullable = false, length = 20)
    private ScheduleOutbox.OperationState operationState;

    @Enumerated(EnumType.STRING)
    @Column(name = "wal_state", nullable = false, length = 20)
    private ScheduleOutbox.WriteAheadState walState;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "timeout_millis", nullable = false)
    private Long timeoutMillis;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected ScheduleOutboxEntity() {
        super();
    }

    /**
     * PK ID 포함 생성자 (조회 후 수정 시 사용)
     *
     * @param id             PK ID
     * @param opId           Orchestrator OpId
     * @param sellerId       셀러 ID (Long FK)
     * @param idemKey        멱등성 키
     * @param eventType      이벤트 타입 Enum
     * @param payload        페이로드 JSON
     * @param outcomeJson    결과 JSON
     * @param operationState 작업 상태
     * @param walState       WAL 상태
     * @param errorMessage   에러 메시지
     * @param retryCount     재시도 횟수
     * @param maxRetries     최대 재시도 횟수
     * @param timeoutMillis  타임아웃 (밀리초)
     * @param completedAt    완료 일시
     */
    public ScheduleOutboxEntity(
            Long id,
            String opId,
            Long sellerId,
            String idemKey,
            EventType eventType,
            String payload,
            String outcomeJson,
            ScheduleOutbox.OperationState operationState,
            ScheduleOutbox.WriteAheadState walState,
            String errorMessage,
            Integer retryCount,
            Integer maxRetries,
            Long timeoutMillis,
            LocalDateTime completedAt
    ) {
        super();
        this.id = id;
        this.opId = opId;
        this.sellerId = sellerId;
        this.idemKey = idemKey;
        this.eventType = eventType;
        this.payload = payload;
        this.outcomeJson = outcomeJson;
        this.operationState = operationState;
        this.walState = walState;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.timeoutMillis = timeoutMillis;
        this.completedAt = completedAt;
    }

    /**
     * PK ID 제외 생성자 (새로 생성 시 사용)
     *
     * @param opId           Orchestrator OpId
     * @param sellerId       셀러 ID (Long FK)
     * @param idemKey        멱등성 키
     * @param eventType      이벤트 타입 Enum
     * @param payload        페이로드 JSON
     * @param outcomeJson    결과 JSON
     * @param operationState 작업 상태
     * @param walState       WAL 상태
     * @param errorMessage   에러 메시지
     * @param retryCount     재시도 횟수
     * @param maxRetries     최대 재시도 횟수
     * @param timeoutMillis  타임아웃 (밀리초)
     * @param completedAt    완료 일시
     */
    public ScheduleOutboxEntity(
            String opId,
            Long sellerId,
            String idemKey,
            EventType eventType,
            String payload,
            String outcomeJson,
            ScheduleOutbox.OperationState operationState,
            ScheduleOutbox.WriteAheadState walState,
            String errorMessage,
            Integer retryCount,
            Integer maxRetries,
            Long timeoutMillis,
            LocalDateTime completedAt
    ) {
        super();
        this.opId = opId;
        this.sellerId = sellerId;
        this.idemKey = idemKey;
        this.eventType = eventType;
        this.payload = payload;
        this.outcomeJson = outcomeJson;
        this.operationState = operationState;
        this.walState = walState;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.timeoutMillis = timeoutMillis;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public String getOpId() {
        return opId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getIdemKey() {
        return idemKey;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getOutcomeJson() {
        return outcomeJson;
    }

    public ScheduleOutbox.OperationState getOperationState() {
        return operationState;
    }

    public ScheduleOutbox.WriteAheadState getWalState() {
        return walState;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public Long getTimeoutMillis() {
        return timeoutMillis;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
