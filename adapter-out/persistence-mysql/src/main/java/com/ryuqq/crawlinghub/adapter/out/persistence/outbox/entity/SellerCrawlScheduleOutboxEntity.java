package com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity;

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
 *   <li>payload: EventBridge Schedule 생성/수정을 위한 JSON 페이로드</li>
 *   <li>operationState: 작업 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)</li>
 *   <li>walState: WAL 상태 (PENDING, COMPLETED)</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
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
public class SellerCrawlScheduleOutboxEntity {

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

    @Column(name = "domain", nullable = false, length = 50)
    private String domain;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "biz_key", nullable = false, length = 100)
    private String bizKey;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "outcome_json", columnDefinition = "TEXT")
    private String outcomeJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_state", nullable = false, length = 20)
    private OperationState operationState;

    @Enumerated(EnumType.STRING)
    @Column(name = "wal_state", nullable = false, length = 20)
    private WriteAheadState walState;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "timeout_millis", nullable = false)
    private Long timeoutMillis;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected SellerCrawlScheduleOutboxEntity() {
    }

    /**
     * Entity 생성자 (초기 생성 시)
     *
     * @param commandInfo Orchestrator Command 정보
     * @param sellerId    Long FK (Seller PK)
     * @param payload     EventBridge 페이로드 JSON
     */
    public SellerCrawlScheduleOutboxEntity(
            CommandInfo commandInfo,
            Long sellerId,
            String payload
    ) {
        this.opId = null; // OpId는 Orchestrator.start() 후 설정
        this.sellerId = sellerId;
        this.idemKey = commandInfo.idemKey();
        this.domain = commandInfo.domain();
        this.eventType = commandInfo.eventType();
        this.bizKey = commandInfo.bizKey();
        this.payload = payload;
        this.operationState = OperationState.PENDING;
        this.walState = WriteAheadState.PENDING;
        this.retryCount = 0;
        this.maxRetries = commandInfo.maxRetries();
        this.timeoutMillis = commandInfo.timeoutMillis();
    }

    /**
     * Private all-args constructor (불변 객체 생성용)
     * <p>
     * Static factory method에서만 사용됩니다.
     * </p>
     */
    private SellerCrawlScheduleOutboxEntity(
            Long id,
            String opId,
            Long sellerId,
            String idemKey,
            String domain,
            String eventType,
            String bizKey,
            String payload,
            String outcomeJson,
            OperationState operationState,
            WriteAheadState walState,
            String errorMessage,
            Integer retryCount,
            Integer maxRetries,
            Long timeoutMillis,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.opId = opId;
        this.sellerId = sellerId;
        this.idemKey = idemKey;
        this.domain = domain;
        this.eventType = eventType;
        this.bizKey = bizKey;
        this.payload = payload;
        this.outcomeJson = outcomeJson;
        this.operationState = operationState;
        this.walState = walState;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.timeoutMillis = timeoutMillis;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
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

    // ========================================
    // Static Factory Methods (불변 객체 생성)
    // ========================================

    /**
     * OpId를 설정한 새 Entity 반환
     * <p>
     * Orchestrator.start() 후 OpId 설정 시 사용됩니다.
     * </p>
     *
     * @param existing 기존 Entity
     * @param opId     Orchestrator OpId (UUID String)
     * @return OpId가 설정된 새 Entity
     */
    public static SellerCrawlScheduleOutboxEntity withOpId(
            SellerCrawlScheduleOutboxEntity existing,
            String opId
    ) {
        return new SellerCrawlScheduleOutboxEntity(
                existing.id,
                opId, // 변경할 값
                existing.sellerId,
                existing.idemKey,
                existing.domain,
                existing.eventType,
                existing.bizKey,
                existing.payload,
                existing.outcomeJson,
                existing.operationState,
                existing.walState,
                existing.errorMessage,
                existing.retryCount,
                existing.maxRetries,
                existing.timeoutMillis,
                existing.createdAt,
                existing.updatedAt,
                existing.completedAt
        );
    }

    /**
     * Write-Ahead Log 기록 (IN_PROGRESS 전환)
     *
     * @param existing    기존 Entity
     * @param outcomeJson Outcome JSON
     * @return IN_PROGRESS 상태로 전환된 새 Entity
     */
    public static SellerCrawlScheduleOutboxEntity withWriteAhead(
            SellerCrawlScheduleOutboxEntity existing,
            String outcomeJson
    ) {
        return new SellerCrawlScheduleOutboxEntity(
                existing.id,
                existing.opId,
                existing.sellerId,
                existing.idemKey,
                existing.domain,
                existing.eventType,
                existing.bizKey,
                existing.payload,
                outcomeJson, // 변경할 값
                OperationState.IN_PROGRESS, // 변경할 값
                WriteAheadState.PENDING, // 변경할 값
                existing.errorMessage,
                existing.retryCount,
                existing.maxRetries,
                existing.timeoutMillis,
                existing.createdAt,
                existing.updatedAt,
                existing.completedAt
        );
    }

    /**
     * 작업 완료 (COMPLETED/FAILED 전환)
     *
     * @param existing   기존 Entity
     * @param finalState 최종 상태 (COMPLETED/FAILED)
     * @return 최종 상태로 전환된 새 Entity
     */
    public static SellerCrawlScheduleOutboxEntity withFinalized(
            SellerCrawlScheduleOutboxEntity existing,
            OperationState finalState
    ) {
        return new SellerCrawlScheduleOutboxEntity(
                existing.id,
                existing.opId,
                existing.sellerId,
                existing.idemKey,
                existing.domain,
                existing.eventType,
                existing.bizKey,
                existing.payload,
                existing.outcomeJson,
                finalState, // 변경할 값
                WriteAheadState.COMPLETED, // 변경할 값
                existing.errorMessage,
                existing.retryCount,
                existing.maxRetries,
                existing.timeoutMillis,
                existing.createdAt,
                existing.updatedAt,
                LocalDateTime.now() // 변경할 값
        );
    }

    /**
     * 재시도 횟수 증가
     *
     * @param existing 기존 Entity
     * @return 재시도 횟수가 증가된 새 Entity
     */
    public static SellerCrawlScheduleOutboxEntity withIncrementedRetry(
            SellerCrawlScheduleOutboxEntity existing
    ) {
        return new SellerCrawlScheduleOutboxEntity(
                existing.id,
                existing.opId,
                existing.sellerId,
                existing.idemKey,
                existing.domain,
                existing.eventType,
                existing.bizKey,
                existing.payload,
                existing.outcomeJson,
                existing.operationState,
                existing.walState,
                existing.errorMessage,
                existing.retryCount + 1, // 변경할 값
                existing.maxRetries,
                existing.timeoutMillis,
                existing.createdAt,
                existing.updatedAt,
                existing.completedAt
        );
    }

    /**
     * 에러 메시지 설정
     *
     * @param existing     기존 Entity
     * @param errorMessage 에러 메시지
     * @return 에러 메시지가 설정된 새 Entity
     */
    public static SellerCrawlScheduleOutboxEntity withErrorMessage(
            SellerCrawlScheduleOutboxEntity existing,
            String errorMessage
    ) {
        return new SellerCrawlScheduleOutboxEntity(
                existing.id,
                existing.opId,
                existing.sellerId,
                existing.idemKey,
                existing.domain,
                existing.eventType,
                existing.bizKey,
                existing.payload,
                existing.outcomeJson,
                existing.operationState,
                existing.walState,
                errorMessage, // 변경할 값
                existing.retryCount,
                existing.maxRetries,
                existing.timeoutMillis,
                existing.createdAt,
                existing.updatedAt,
                existing.completedAt
        );
    }

    // ========================================
    // Getters (불변 접근만 제공)
    // ========================================

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

    public String getDomain() {
        return domain;
    }

    public String getEventType() {
        return eventType;
    }

    public String getBizKey() {
        return bizKey;
    }

    public String getPayload() {
        return payload;
    }

    public String getOutcomeJson() {
        return outcomeJson;
    }

    public OperationState getOperationState() {
        return operationState;
    }

    public WriteAheadState getWalState() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * Orchestrator Command 정보 Record
     *
     * @param domain        도메인 (e.g., "seller-crawl-schedule")
     * @param eventType     이벤트 타입 (e.g., "SCHEDULE.CREATE.REQUEST")
     * @param bizKey        비즈니스 키 (e.g., "seller-123")
     * @param idemKey       멱등성 키 (e.g., "seller-123-create-schedule")
     * @param maxRetries    최대 재시도 횟수
     * @param timeoutMillis 타임아웃 (밀리초)
     */
    public record CommandInfo(
            String domain,
            String eventType,
            String bizKey,
            String idemKey,
            Integer maxRetries,
            Long timeoutMillis
    ) {
        /**
         * CommandInfo 기본값 생성
         * <p>
         * 재시도 3회, 타임아웃 5분(300초)으로 기본 설정됩니다.
         * </p>
         *
         * @param domain    도메인
         * @param eventType 이벤트 타입
         * @param bizKey    비즈니스 키
         * @param idemKey   멱등성 키
         * @return CommandInfo (기본 재시도/타임아웃 설정)
         */
        public static CommandInfo of(
                String domain,
                String eventType,
                String bizKey,
                String idemKey
        ) {
            return new CommandInfo(
                    domain,
                    eventType,
                    bizKey,
                    idemKey,
                    3, // 기본 재시도 3회
                    300000L // 기본 타임아웃 5분
            );
        }
    }

    /**
     * 작업 상태 Enum
     */
    public enum OperationState {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    /**
     * Write-Ahead Log 상태 Enum
     */
    public enum WriteAheadState {
        PENDING,
        COMPLETED
    }
}
