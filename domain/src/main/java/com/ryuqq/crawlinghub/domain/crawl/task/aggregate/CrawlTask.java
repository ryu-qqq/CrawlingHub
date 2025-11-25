package com.ryuqq.crawlinghub.domain.crawl.task.aggregate;

import com.ryuqq.crawlinghub.domain.crawl.task.exception.InvalidCrawlTaskStateException;
import com.ryuqq.crawlinghub.domain.crawl.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskType;

import java.time.LocalDateTime;

/**
 * CrawlTask Aggregate Root
 *
 * <p>크롤링 태스크의 핵심 비즈니스 규칙과 불변식을 관리하는 Aggregate Root
 *
 * <p><strong>상태 전환 규칙</strong>:
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
    private final Long crawlSchedulerId;
    private final Long sellerId;
    private final CrawlTaskType taskType;
    private final CrawlEndpoint endpoint;
    private CrawlTaskStatus status;
    private int retryCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CrawlTask(
            CrawlTaskId id,
            Long crawlSchedulerId,
            Long sellerId,
            CrawlTaskType taskType,
            CrawlEndpoint endpoint,
            CrawlTaskStatus status,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.crawlSchedulerId = crawlSchedulerId;
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.endpoint = endpoint;
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 CrawlTask 생성
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId         셀러 ID
     * @param taskType         태스크 유형
     * @param endpoint         크롤링 엔드포인트
     * @return 새로운 CrawlTask (WAITING 상태)
     */
    public static CrawlTask forNew(
            Long crawlSchedulerId,
            Long sellerId,
            CrawlTaskType taskType,
            CrawlEndpoint endpoint
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new CrawlTask(
                CrawlTaskId.unassigned(),
                crawlSchedulerId,
                sellerId,
                taskType,
                endpoint,
                CrawlTaskStatus.WAITING,
                0,
                now,
                now
        );
    }

    /**
     * 기존 데이터로 CrawlTask 복원 (영속성 계층 전용)
     *
     * @param id               CrawlTask ID
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId         셀러 ID
     * @param taskType         태스크 유형
     * @param endpoint         크롤링 엔드포인트
     * @param status           현재 상태
     * @param retryCount       재시도 횟수
     * @param createdAt        생성 시각
     * @param updatedAt        수정 시각
     * @return 복원된 CrawlTask
     */
    public static CrawlTask reconstitute(
            CrawlTaskId id,
            Long crawlSchedulerId,
            Long sellerId,
            CrawlTaskType taskType,
            CrawlEndpoint endpoint,
            CrawlTaskStatus status,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new CrawlTask(
                id,
                crawlSchedulerId,
                sellerId,
                taskType,
                endpoint,
                status,
                retryCount,
                createdAt,
                updatedAt
        );
    }

    /**
     * WAITING → PUBLISHED 상태 전환
     *
     * @throws InvalidCrawlTaskStateException 현재 상태가 WAITING이 아닌 경우
     */
    public void markAsPublished() {
        validateStatus(CrawlTaskStatus.WAITING, CrawlTaskStatus.PUBLISHED);
        this.status = CrawlTaskStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * PUBLISHED → RUNNING 상태 전환
     *
     * @throws InvalidCrawlTaskStateException 현재 상태가 PUBLISHED가 아닌 경우
     */
    public void markAsRunning() {
        validateStatus(CrawlTaskStatus.PUBLISHED, CrawlTaskStatus.RUNNING);
        this.status = CrawlTaskStatus.RUNNING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * RUNNING → SUCCESS 상태 전환
     *
     * @throws InvalidCrawlTaskStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void markAsSuccess() {
        validateStatus(CrawlTaskStatus.RUNNING, CrawlTaskStatus.SUCCESS);
        this.status = CrawlTaskStatus.SUCCESS;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * RUNNING → FAILED 상태 전환
     *
     * @throws InvalidCrawlTaskStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void markAsFailed() {
        validateStatus(CrawlTaskStatus.RUNNING, CrawlTaskStatus.FAILED);
        this.status = CrawlTaskStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * RUNNING → TIMEOUT 상태 전환
     *
     * @throws InvalidCrawlTaskStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void markAsTimeout() {
        validateStatus(CrawlTaskStatus.RUNNING, CrawlTaskStatus.TIMEOUT);
        this.status = CrawlTaskStatus.TIMEOUT;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @return 재시도 가능 여부
     */
    public boolean canRetry(int maxRetryCount) {
        boolean isRetryableStatus = this.status == CrawlTaskStatus.FAILED
                || this.status == CrawlTaskStatus.TIMEOUT;
        return isRetryableStatus && this.retryCount < maxRetryCount;
    }

    /**
     * 재시도 수행
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @return 재시도 성공 여부
     */
    public boolean attemptRetry(int maxRetryCount) {
        if (!canRetry(maxRetryCount)) {
            return false;
        }
        this.retryCount++;
        this.status = CrawlTaskStatus.RETRY;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * 재시도 후 다시 PUBLISHED 상태로 전환
     */
    public void markAsPublishedAfterRetry() {
        if (this.status != CrawlTaskStatus.RETRY) {
            throw new InvalidCrawlTaskStateException(this.status, CrawlTaskStatus.PUBLISHED);
        }
        this.status = CrawlTaskStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 진행 중 상태 여부 확인
     *
     * @return WAITING, PUBLISHED, RUNNING 중 하나면 true
     */
    public boolean isInProgress() {
        return this.status.isInProgress();
    }

    private void validateStatus(CrawlTaskStatus expected, CrawlTaskStatus target) {
        if (this.status != expected) {
            throw new InvalidCrawlTaskStateException(this.status, target);
        }
    }

    // Getters (Lombok 금지로 수동 작성)

    public CrawlTaskId getId() {
        return id;
    }

    public Long getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public CrawlTaskType getTaskType() {
        return taskType;
    }

    public CrawlEndpoint getEndpoint() {
        return endpoint;
    }

    public CrawlTaskStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
