package com.ryuqq.crawlinghub.domain.task.aggregate;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.event.CrawlTaskRegisteredEvent;
import com.ryuqq.crawlinghub.domain.task.exception.InvalidCrawlTaskStateException;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.crawlinghub.domain.task.vo.RetryCount;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * CrawlTask Aggregate Root
 *
 * <p>크롤링 태스크의 핵심 비즈니스 규칙과 불변식을 관리하는 Aggregate Root
 *
 * <p><strong>상태 전환 규칙</strong>:
 *
 * <pre>
 * WAITING → PUBLISHED → RUNNING → SUCCESS
 *                         ↓
 *                       FAILED → RETRY → PUBLISHED
 *                         ↓
 *                      TIMEOUT → RETRY → PUBLISHED
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlTask {

    private final CrawlTaskId id;
    private final CrawlSchedulerId crawlSchedulerId;
    private final SellerId sellerId;
    private final CrawlTaskType taskType;
    private final CrawlEndpoint endpoint;
    private CrawlTaskStatus status;
    private RetryCount retryCount;
    private CrawlTaskOutbox outbox;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private CrawlTask(
            CrawlTaskId id,
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            CrawlTaskType taskType,
            CrawlEndpoint endpoint,
            CrawlTaskStatus status,
            RetryCount retryCount,
            CrawlTaskOutbox outbox,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.crawlSchedulerId = crawlSchedulerId;
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.endpoint = endpoint;
        this.status = status;
        this.retryCount = retryCount;
        this.outbox = outbox;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 CrawlTask 생성
     *
     * <p>Outbox 없이 생성 (별도로 initializeOutbox 호출 필요)
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param taskType 태스크 유형
     * @param endpoint 크롤링 엔드포인트
     * @param now 현재 시각
     * @return 새로운 CrawlTask (WAITING 상태)
     */
    public static CrawlTask forNew(
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            CrawlTaskType taskType,
            CrawlEndpoint endpoint,
            Instant now) {
        return new CrawlTask(
                CrawlTaskId.unassigned(),
                crawlSchedulerId,
                sellerId,
                taskType,
                endpoint,
                CrawlTaskStatus.WAITING,
                RetryCount.zero(),
                null,
                now,
                now);
    }

    /**
     * 기존 데이터로 CrawlTask 복원 (영속성 계층 전용)
     *
     * @param id CrawlTask ID
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param taskType 태스크 유형
     * @param endpoint 크롤링 엔드포인트
     * @param status 현재 상태
     * @param retryCount 재시도 횟수
     * @param outbox Outbox (nullable)
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return 복원된 CrawlTask
     */
    public static CrawlTask reconstitute(
            CrawlTaskId id,
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            CrawlTaskType taskType,
            CrawlEndpoint endpoint,
            CrawlTaskStatus status,
            RetryCount retryCount,
            CrawlTaskOutbox outbox,
            Instant createdAt,
            Instant updatedAt) {
        return new CrawlTask(
                id,
                crawlSchedulerId,
                sellerId,
                taskType,
                endpoint,
                status,
                retryCount,
                outbox,
                createdAt,
                updatedAt);
    }

    /**
     * Idempotency Key 생성
     *
     * <p>UUID로 고유 키 생성
     */
    public String generateIdempotencyKey() {
        return String.format(
                "%s-%s-%s",
                crawlSchedulerId.value(), id.value(), UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * WAITING → PUBLISHED 상태 전환
     *
     * @param now 현재 시각
     * @throws InvalidCrawlTaskStateException 현재 상태가 WAITING이 아닌 경우
     */
    public void markAsPublished(Instant now) {
        validateStatus(CrawlTaskStatus.WAITING, CrawlTaskStatus.PUBLISHED);
        this.status = CrawlTaskStatus.PUBLISHED;
        this.updatedAt = now;
    }

    /**
     * PUBLISHED → RUNNING 상태 전환
     *
     * @param now 현재 시각
     * @throws InvalidCrawlTaskStateException 현재 상태가 PUBLISHED가 아닌 경우
     */
    public void markAsRunning(Instant now) {
        validateStatus(CrawlTaskStatus.PUBLISHED, CrawlTaskStatus.RUNNING);
        this.status = CrawlTaskStatus.RUNNING;
        this.updatedAt = now;
    }

    /**
     * RUNNING → SUCCESS 상태 전환
     *
     * @param now 현재 시각
     * @throws InvalidCrawlTaskStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void markAsSuccess(Instant now) {
        validateStatus(CrawlTaskStatus.RUNNING, CrawlTaskStatus.SUCCESS);
        this.status = CrawlTaskStatus.SUCCESS;
        this.updatedAt = now;
    }

    /**
     * RUNNING → FAILED 상태 전환
     *
     * @param now 현재 시각
     * @throws InvalidCrawlTaskStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void markAsFailed(Instant now) {
        validateStatus(CrawlTaskStatus.RUNNING, CrawlTaskStatus.FAILED);
        this.status = CrawlTaskStatus.FAILED;
        this.updatedAt = now;
    }

    /**
     * RUNNING → TIMEOUT 상태 전환
     *
     * @param now 현재 시각
     * @throws InvalidCrawlTaskStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void markAsTimeout(Instant now) {
        validateStatus(CrawlTaskStatus.RUNNING, CrawlTaskStatus.TIMEOUT);
        this.status = CrawlTaskStatus.TIMEOUT;
        this.updatedAt = now;
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @return 재시도 가능 여부
     */
    public boolean canRetry() {
        boolean isRetryableStatus =
                this.status == CrawlTaskStatus.FAILED || this.status == CrawlTaskStatus.TIMEOUT;
        return isRetryableStatus && this.retryCount.canRetry();
    }

    /**
     * 재시도 수행
     *
     * @param now 현재 시각
     * @return 재시도 성공 여부
     */
    public boolean attemptRetry(Instant now) {
        if (!canRetry()) {
            return false;
        }
        this.retryCount = this.retryCount.increment();
        this.status = CrawlTaskStatus.RETRY;
        this.updatedAt = now;
        return true;
    }

    /**
     * 재시도 후 다시 PUBLISHED 상태로 전환
     *
     * @param now 현재 시각
     */
    public void markAsPublishedAfterRetry(Instant now) {
        if (this.status != CrawlTaskStatus.RETRY) {
            throw new InvalidCrawlTaskStateException(this.status, CrawlTaskStatus.PUBLISHED);
        }
        this.status = CrawlTaskStatus.PUBLISHED;
        this.updatedAt = now;
    }

    /**
     * 진행 중 상태 여부 확인
     *
     * @return WAITING, PUBLISHED, RUNNING 중 하나면 true
     */
    public boolean isInProgress() {
        return this.status.isInProgress();
    }

    // === Outbox 관련 메서드 ===

    /**
     * Outbox 초기화
     *
     * <p>Task 저장 전에 Outbox를 생성하여 같은 트랜잭션에서 저장
     *
     * @param payload SQS로 발행할 메시지 페이로드 (JSON)
     * @param now 현재 시각
     */
    public void initializeOutbox(String payload, Instant now) {
        if (this.outbox != null) {
            throw new IllegalStateException("Outbox가 이미 초기화되었습니다.");
        }
        this.outbox = CrawlTaskOutbox.forNew(this.id, payload, now);
    }

    /**
     * Outbox 발행 성공 처리
     *
     * @param now 현재 시각
     */
    public void markOutboxAsSent(Instant now) {
        if (this.outbox == null) {
            throw new IllegalStateException("Outbox가 초기화되지 않았습니다.");
        }
        this.outbox.markAsSent(now);
    }

    /**
     * Outbox 발행 실패 처리
     *
     * @param now 현재 시각
     */
    public void markOutboxAsFailed(Instant now) {
        if (this.outbox == null) {
            throw new IllegalStateException("Outbox가 초기화되지 않았습니다.");
        }
        this.outbox.markAsFailed(now);
    }

    /**
     * Outbox 존재 여부 확인
     *
     * @return Outbox가 있으면 true
     */
    public boolean hasOutbox() {
        return this.outbox != null;
    }

    /**
     * Outbox 발행 대기 상태인지 확인
     *
     * @return Outbox가 PENDING 상태이면 true
     */
    public boolean hasOutboxPending() {
        return this.outbox != null && this.outbox.isPending();
    }

    private void validateStatus(CrawlTaskStatus expected, CrawlTaskStatus target) {
        if (this.status != expected) {
            throw new InvalidCrawlTaskStateException(this.status, target);
        }
    }

    // Getters

    public CrawlTaskId getId() {
        return id;
    }

    public Long getIdValue() {
        return id.value();
    }

    public CrawlSchedulerId getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    public Long getCrawlSchedulerIdValue() {
        return crawlSchedulerId.value();
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public Long getSellerIdValue() {
        return sellerId.value();
    }

    public CrawlTaskType getTaskType() {
        return taskType;
    }

    public CrawlEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * 머스트잇 셀러명 조회
     *
     * <p>endpoint에서 mustItSellerName을 조회합니다. META, MINI_SHOP 타입에서만 유효한 값입니다.
     *
     * @return 머스트잇 셀러명 (DETAIL, OPTION 타입은 null)
     */
    public String getMustItSellerName() {
        return endpoint.getMustItSellerName();
    }

    public CrawlTaskStatus getStatus() {
        return status;
    }

    public RetryCount getRetryCount() {
        return retryCount;
    }

    public CrawlTaskOutbox getOutbox() {
        return outbox;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // === 도메인 이벤트 관련 메서드 ===

    /**
     * 등록 이벤트 추가 (영속화 후 호출)
     *
     * <p>ID 할당 후 호출해야 합니다.
     *
     * @param outboxPayload Outbox 페이로드 (JSON)
     * @param now 현재 시각
     */
    public void addRegisteredEvent(String outboxPayload, Instant now) {
        if (this.id == null || !this.id.isAssigned()) {
            throw new IllegalStateException("등록 이벤트는 ID 할당 후 발행해야 합니다.");
        }
        this.domainEvents.add(
                CrawlTaskRegisteredEvent.of(
                        this.id,
                        this.crawlSchedulerId,
                        this.sellerId,
                        this.taskType,
                        this.endpoint,
                        outboxPayload,
                        now));
    }

    /**
     * 도메인 이벤트 목록 (읽기 전용)
     *
     * @return 불변 이벤트 목록
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /** 도메인 이벤트 초기화 */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
