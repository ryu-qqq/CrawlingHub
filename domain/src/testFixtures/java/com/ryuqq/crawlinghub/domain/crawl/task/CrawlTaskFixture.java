package com.ryuqq.crawlinghub.domain.crawl.task;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * CrawlTask Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class CrawlTaskFixture {

    private static final Long DEFAULT_ID = 1L;
    private static final Long DEFAULT_SELLER_ID = 100L;
    private static final TaskType DEFAULT_TASK_TYPE = TaskType.MINI_SHOP;
    private static final Integer DEFAULT_PAGE_NUMBER = 1;
    private static final Clock DEFAULT_CLOCK = Clock.fixed(
        Instant.parse("2025-01-01T00:00:00Z"),
        ZoneId.systemDefault()
    );

    /**
     * 기본 CrawlTask 생성 (신규)
     *
     * @return CrawlTask
     */
    public static CrawlTask create() {
        return CrawlTask.forNew(
            MustitSellerId.of(DEFAULT_SELLER_ID),
            DEFAULT_TASK_TYPE,
            RequestUrlFixture.create(),
            DEFAULT_PAGE_NUMBER,
            generateIdempotencyKey(),
            LocalDateTime.now(DEFAULT_CLOCK)
        );
    }

    /**
     * ID를 가진 CrawlTask 생성
     *
     * @param id CrawlTask ID
     * @return CrawlTask
     */
    public static CrawlTask createWithId(Long id) {
        return CrawlTask.of(
            CrawlTaskId.of(id),
            MustitSellerId.of(DEFAULT_SELLER_ID),
            DEFAULT_TASK_TYPE,
            RequestUrlFixture.create(),
            DEFAULT_PAGE_NUMBER,
            generateIdempotencyKey(),
            LocalDateTime.now(DEFAULT_CLOCK)
        );
    }

    /**
     * 특정 셀러 ID로 CrawlTask 생성
     *
     * @param sellerId 셀러 ID
     * @return CrawlTask
     */
    public static CrawlTask createWithSellerId(Long sellerId) {
        return CrawlTask.forNew(
            MustitSellerId.of(sellerId),
            DEFAULT_TASK_TYPE,
            RequestUrlFixture.create(),
            DEFAULT_PAGE_NUMBER,
            generateIdempotencyKey(),
            LocalDateTime.now(DEFAULT_CLOCK)
        );
    }

    /**
     * 특정 작업 유형으로 CrawlTask 생성
     *
     * @param taskType 작업 유형
     * @return CrawlTask
     */
    public static CrawlTask createWithTaskType(TaskType taskType) {
        RequestUrl requestUrl = taskType == TaskType.MINI_SHOP
            ? RequestUrlFixture.createListUrl() 
            : RequestUrlFixture.createDetailUrl();

        return CrawlTask.forNew(
            MustitSellerId.of(DEFAULT_SELLER_ID),
            taskType,
            requestUrl,
            DEFAULT_PAGE_NUMBER,
            generateIdempotencyKey(),
            LocalDateTime.now(DEFAULT_CLOCK)
        );
    }

    /**
     * 특정 페이지 번호로 CrawlTask 생성
     *
     * @param pageNumber 페이지 번호
     * @return CrawlTask
     */
    public static CrawlTask createWithPage(int pageNumber) {
        return CrawlTask.forNew(
            MustitSellerId.of(DEFAULT_SELLER_ID),
            DEFAULT_TASK_TYPE,
            RequestUrlFixture.createWithPage(pageNumber),
            pageNumber,
            generateIdempotencyKey(),
            LocalDateTime.now(DEFAULT_CLOCK)
        );
    }

    /**
     * WAITING 상태의 CrawlTask 생성
     *
     * @return CrawlTask
     */
    public static CrawlTask createWaiting() {
        return reconstitute(DEFAULT_ID, DEFAULT_SELLER_ID, TaskStatus.WAITING, 0);
    }

    /**
     * PUBLISHED 상태의 CrawlTask 생성
     *
     * @return CrawlTask
     */
    public static CrawlTask createPublished() {
        return reconstitute(DEFAULT_ID, DEFAULT_SELLER_ID, TaskStatus.PUBLISHED, 0);
    }

    /**
     * RUNNING 상태의 CrawlTask 생성
     *
     * @return CrawlTask
     */
    public static CrawlTask createRunning() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return CrawlTask.reconstitute(
            CrawlTaskId.of(DEFAULT_ID),
            MustitSellerId.of(DEFAULT_SELLER_ID),
            DEFAULT_TASK_TYPE,
            TaskStatus.RUNNING,
            RequestUrlFixture.create(),
            DEFAULT_PAGE_NUMBER,
            0,
            generateIdempotencyKey(),
            now,
            now,
            null,
            now,
            now
        );
    }

    /**
     * SUCCESS 상태의 CrawlTask 생성
     *
     * @return CrawlTask
     */
    public static CrawlTask createSuccess() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return CrawlTask.reconstitute(
            CrawlTaskId.of(DEFAULT_ID),
            MustitSellerId.of(DEFAULT_SELLER_ID),
            DEFAULT_TASK_TYPE,
            TaskStatus.SUCCESS,
            RequestUrlFixture.create(),
            DEFAULT_PAGE_NUMBER,
            0,
            generateIdempotencyKey(),
            now,
            now,
            now,
            now,
            now
        );
    }

    /**
     * FAILED 상태의 CrawlTask 생성
     *
     * @param retryCount 재시도 횟수
     * @return CrawlTask
     */
    public static CrawlTask createFailed(int retryCount) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return CrawlTask.reconstitute(
            CrawlTaskId.of(DEFAULT_ID),
            MustitSellerId.of(DEFAULT_SELLER_ID),
            DEFAULT_TASK_TYPE,
            TaskStatus.FAILED,
            RequestUrlFixture.create(),
            DEFAULT_PAGE_NUMBER,
            retryCount,
            generateIdempotencyKey(),
            now,
            now,
            now,
            now,
            now
        );
    }

    /**
     * RETRY 상태의 CrawlTask 생성
     *
     * @param retryCount 재시도 횟수
     * @return CrawlTask
     */
    public static CrawlTask createRetry(int retryCount) {
        return reconstitute(DEFAULT_ID, DEFAULT_SELLER_ID, TaskStatus.RETRY, retryCount);
    }

    /**
     * DB reconstitute용 CrawlTask 생성
     *
     * @param id CrawlTask ID
     * @param sellerId 셀러 ID
     * @param status 작업 상태
     * @param retryCount 재시도 횟수
     * @return CrawlTask
     */
    public static CrawlTask reconstitute(
        Long id,
        Long sellerId,
        TaskStatus status,
        int retryCount
    ) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        LocalDateTime startedAt = status.isRunning() || status.isCompleted() ? now : null;
        LocalDateTime completedAt = status.isCompleted() ? now : null;

        return CrawlTask.reconstitute(
            CrawlTaskId.of(id),
            MustitSellerId.of(sellerId),
            DEFAULT_TASK_TYPE,
            status,
            RequestUrlFixture.create(),
            DEFAULT_PAGE_NUMBER,
            retryCount,
            generateIdempotencyKey(),
            now,
            startedAt,
            completedAt,
            now,
            now
        );
    }

    /**
     * 완전한 커스텀 CrawlTask 생성
     *
     * @param id CrawlTask ID (null 가능)
     * @param sellerId 셀러 ID
     * @param taskType 작업 유형
     * @param requestUrl 요청 URL
     * @param pageNumber 페이지 번호
     * @return CrawlTask
     */
    public static CrawlTask createCustom(
        Long id,
        Long sellerId,
        TaskType taskType,
        RequestUrl requestUrl,
        Integer pageNumber
    ) {
        String idempotencyKey = generateIdempotencyKey();
        LocalDateTime scheduledAt = LocalDateTime.now(DEFAULT_CLOCK);

        if (id == null) {
            return CrawlTask.forNew(
                MustitSellerId.of(sellerId),
                taskType,
                requestUrl,
                pageNumber,
                idempotencyKey,
                scheduledAt
            );
        }
        return CrawlTask.of(
            CrawlTaskId.of(id),
            MustitSellerId.of(sellerId),
            taskType,
            requestUrl,
            pageNumber,
            idempotencyKey,
            scheduledAt
        );
    }

    /**
     * 멱등성 키 생성
     *
     * @return 멱등성 키
     */
    private static String generateIdempotencyKey() {
        return UUID.randomUUID().toString();
    }
}
