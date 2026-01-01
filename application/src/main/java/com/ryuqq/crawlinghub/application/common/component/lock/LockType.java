package com.ryuqq.crawlinghub.application.common.component.lock;

/**
 * 분산 락 타입
 *
 * <p><strong>용도</strong>: 락 종류별 설정 (대기 시간, 유지 시간) 구분
 *
 * @author development-team
 * @since 1.0.0
 */
public enum LockType {

    /**
     * CrawlTask 트리거 락
     *
     * <p>EventBridge 트리거 시 중복 Task 생성 방지
     *
     * <ul>
     *   <li>락 키 패턴: trigger:{schedulerId}
     *   <li>대기 시간: 0ms (즉시 반환, 대기 없음)
     *   <li>유지 시간: 60초 (Task 생성 + Outbox 저장)
     * </ul>
     */
    CRAWL_TRIGGER("trigger:", 0L, 60000L),

    /**
     * CrawlTask 실행 락
     *
     * <p>CrawlTask 중복 실행 방지
     *
     * <ul>
     *   <li>락 키 패턴: task:{taskId}
     *   <li>대기 시간: 0ms (즉시 반환, 대기 없음)
     *   <li>유지 시간: 60초 (크롤링 작업 시간)
     * </ul>
     *
     * <p><strong>SQS 설정</strong>:
     *
     * <ul>
     *   <li>visibility timeout: 2분 (leaseTime보다 길게)
     *   <li>maxReceiveCount: 1 (한 번 실패 시 DLQ)
     * </ul>
     */
    CRAWL_TASK("task:", 0L, 60000L),

    /**
     * ProductImageOutbox 처리 락
     *
     * <p>이미지 업로드 중복 처리 방지
     *
     * <ul>
     *   <li>락 키 패턴: image-outbox:{outboxId}
     *   <li>대기 시간: 0ms (즉시 반환, 대기 없음)
     *   <li>유지 시간: 120초 (이미지 업로드 시간)
     * </ul>
     */
    PRODUCT_IMAGE_OUTBOX("image-outbox:", 0L, 120000L),

    /**
     * ProductSyncOutbox 처리 락
     *
     * <p>외부 서버 동기화 중복 처리 방지
     *
     * <ul>
     *   <li>락 키 패턴: sync-outbox:{outboxId}
     *   <li>대기 시간: 0ms (즉시 반환, 대기 없음)
     *   <li>유지 시간: 120초 (동기화 작업 시간)
     * </ul>
     */
    PRODUCT_SYNC_OUTBOX("sync-outbox:", 0L, 120000L);

    private final String keyPrefix;
    private final long defaultWaitTimeMs;
    private final long defaultLeaseTimeMs;

    LockType(String keyPrefix, long defaultWaitTimeMs, long defaultLeaseTimeMs) {
        this.keyPrefix = keyPrefix;
        this.defaultWaitTimeMs = defaultWaitTimeMs;
        this.defaultLeaseTimeMs = defaultLeaseTimeMs;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public long getDefaultWaitTimeMs() {
        return defaultWaitTimeMs;
    }

    public long getDefaultLeaseTimeMs() {
        return defaultLeaseTimeMs;
    }

    /**
     * 전체 락 키 생성
     *
     * @param identifier 식별자 (schedulerId, taskId 등)
     * @return 전체 락 키
     */
    public String buildKey(Object identifier) {
        return keyPrefix + identifier;
    }
}
