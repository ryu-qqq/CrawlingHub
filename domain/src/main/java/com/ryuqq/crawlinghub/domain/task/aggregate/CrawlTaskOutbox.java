package com.ryuqq.crawlinghub.domain.task.aggregate;

import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Clock;
import java.time.Instant;

/**
 * CrawlTask Outbox Aggregate
 *
 * <p>Transactional Outbox 패턴 구현을 위한 메시지 발행 상태 관리
 *
 * <p><strong>Outbox 패턴 흐름</strong>:
 *
 * <pre>
 * 1. CrawlTask 저장 시 Outbox도 함께 저장 (같은 트랜잭션)
 * 2. 별도 스케줄러가 PENDING 상태 Outbox 조회
 * 3. SQS 발행 성공 시 SENT로 변경 또는 삭제
 * 4. 실패 시 FAILED로 변경 후 재시도
 * </pre>
 *
 * <p><strong>Idempotency Key 보장</strong>:
 *
 * <ul>
 *   <li>동일 Task에 대해 고유한 발행 키 생성
 *   <li>SQS 중복 발행 방지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlTaskOutbox {

    private static final int MAX_RETRY_COUNT = 3;

    private final CrawlTaskId crawlTaskId;
    private final String idempotencyKey;
    private final String payload;
    private OutboxStatus status;
    private int retryCount;
    private final Instant createdAt;
    private Instant processedAt;

    private CrawlTaskOutbox(
            CrawlTaskId crawlTaskId,
            String idempotencyKey,
            String payload,
            OutboxStatus status,
            int retryCount,
            Instant createdAt,
            Instant processedAt) {
        this.crawlTaskId = crawlTaskId;
        this.idempotencyKey = idempotencyKey;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * 신규 Outbox 생성
     *
     * @param crawlTaskId Task ID
     * @param payload 발행할 메시지 페이로드 (JSON)
     * @param clock 시간 제어
     * @return 새로운 Outbox (PENDING 상태)
     */
    public static CrawlTaskOutbox forNew(CrawlTaskId crawlTaskId, String payload, Clock clock) {
        String idempotencyKey = generateIdempotencyKey(crawlTaskId);
        Instant now = clock.instant();
        return new CrawlTaskOutbox(
                crawlTaskId, idempotencyKey, payload, OutboxStatus.PENDING, 0, now, null);
    }

    /**
     * 기존 데이터로 Outbox 복원 (영속성 계층 전용)
     *
     * @param crawlTaskId Task ID
     * @param idempotencyKey 멱등성 키
     * @param payload 페이로드
     * @param status 현재 상태
     * @param retryCount 재시도 횟수
     * @param createdAt 생성 시각
     * @param processedAt 처리 시각
     * @return 복원된 Outbox
     */
    public static CrawlTaskOutbox reconstitute(
            CrawlTaskId crawlTaskId,
            String idempotencyKey,
            String payload,
            OutboxStatus status,
            int retryCount,
            Instant createdAt,
            Instant processedAt) {
        return new CrawlTaskOutbox(
                crawlTaskId, idempotencyKey, payload, status, retryCount, createdAt, processedAt);
    }

    /**
     * Idempotency Key 생성
     *
     * <p>CrawlTaskId 기반으로 결정적(deterministic) 키 생성 동일 Task에 대해 항상 동일한 키가 생성되어 SQS 중복 발행 방지
     */
    private static String generateIdempotencyKey(CrawlTaskId crawlTaskId) {
        return String.format("outbox-%s", crawlTaskId.value());
    }

    /**
     * 발행 성공 처리
     *
     * @param clock 시간 제어
     */
    public void markAsSent(Clock clock) {
        this.status = OutboxStatus.SENT;
        this.processedAt = clock.instant();
    }

    /**
     * 발행 실패 처리
     *
     * @param clock 시간 제어
     */
    public void markAsFailed(Clock clock) {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        this.processedAt = clock.instant();
    }

    /** 재시도를 위해 PENDING으로 복귀 */
    public void resetToPending() {
        if (canRetry()) {
            this.status = OutboxStatus.PENDING;
        }
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @return 최대 재시도 횟수 미만이면 true
     */
    public boolean canRetry() {
        return this.retryCount < MAX_RETRY_COUNT;
    }

    /**
     * 발행 대기 상태인지 확인
     *
     * @return PENDING 상태이면 true
     */
    public boolean isPending() {
        return this.status.isPending();
    }

    /**
     * 발행 완료 상태인지 확인
     *
     * @return SENT 상태이면 true
     */
    public boolean isSent() {
        return this.status.isSent();
    }

    // Getters

    public CrawlTaskId getCrawlTaskId() {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
