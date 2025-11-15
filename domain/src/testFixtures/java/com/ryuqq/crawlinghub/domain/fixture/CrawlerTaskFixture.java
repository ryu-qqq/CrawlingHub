package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.aggregate.CrawlerTask;
import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskStatus;
import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskType;
import com.ryuqq.crawlinghub.domain.vo.SellerId;
import com.ryuqq.crawlinghub.domain.vo.TaskId;

/**
 * 크롤러 작업 관련 테스트 데이터 생성 Fixture
 *
 * <p>CrawlerTask Aggregate와 관련 Value Object의 테스트 데이터를 제공합니다.</p>
 *
 * <p>제공 메서드:</p>
 * <ul>
 *   <li>{@link #defaultTaskId()} - 새로운 TaskId 생성</li>
 *   <li>{@link #defaultCrawlerTaskType()} - 기본 작업 타입 (MINISHOP)</li>
 *   <li>{@link #defaultCrawlerTaskStatus()} - 기본 작업 상태 (WAITING)</li>
 *   <li>{@link #waitingTask()} - WAITING 상태의 CrawlerTask</li>
 *   <li>{@link #publishedTask()} - PUBLISHED 상태의 CrawlerTask</li>
 *   <li>{@link #inProgressTask()} - IN_PROGRESS 상태의 CrawlerTask</li>
 *   <li>{@link #taskWithRetryCount(int)} - 특정 retryCount를 가진 CrawlerTask</li>
 * </ul>
 */
public class CrawlerTaskFixture {

    private static final String DEFAULT_REQUEST_URL = "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123";

    /**
     * 기본 TaskId 생성
     *
     * @return 새로운 UUID 기반 TaskId
     */
    public static TaskId defaultTaskId() {
        return TaskId.generate();
    }

    /**
     * 기본 CrawlerTaskType 반환
     *
     * @return MINISHOP 타입
     */
    public static CrawlerTaskType defaultCrawlerTaskType() {
        return CrawlerTaskType.MINISHOP;
    }

    /**
     * 기본 CrawlerTaskStatus 반환
     *
     * @return WAITING 상태
     */
    public static CrawlerTaskStatus defaultCrawlerTaskStatus() {
        return CrawlerTaskStatus.WAITING;
    }

    /**
     * WAITING 상태의 CrawlerTask 생성
     *
     * @return 새로 생성된 WAITING 상태의 CrawlerTask
     */
    public static CrawlerTask waitingTask() {
        return CrawlerTask.create(
            new SellerId("seller_test_001"),
            CrawlerTaskType.MINISHOP,
            DEFAULT_REQUEST_URL
        );
    }

    /**
     * PUBLISHED 상태의 CrawlerTask 생성
     *
     * @return PUBLISHED 상태의 CrawlerTask
     */
    public static CrawlerTask publishedTask() {
        CrawlerTask task = waitingTask();
        task.publish();
        return task;
    }

    /**
     * IN_PROGRESS 상태의 CrawlerTask 생성
     *
     * @return IN_PROGRESS 상태의 CrawlerTask
     */
    public static CrawlerTask inProgressTask() {
        CrawlerTask task = publishedTask();
        task.start();
        return task;
    }

    /**
     * 특정 retryCount를 가진 FAILED 상태의 CrawlerTask 생성
     *
     * <p>재시도 로직을 테스트하기 위해 사용됩니다.</p>
     * <p>retryCount = 2인 경우, 이미 최대 재시도 횟수에 도달한 상태입니다.</p>
     *
     * @param retryCount 재시도 횟수 (0-2)
     * @return 지정된 retryCount를 가진 FAILED 상태의 CrawlerTask
     */
    public static CrawlerTask taskWithRetryCount(int retryCount) {
        CrawlerTask task = inProgressTask();

        for (int i = 0; i < retryCount; i++) {
            task.fail("Test error");
            if (i < 2) { // MAX_RETRY_COUNT = 2
                task.retry();
                task.start();
            }
        }

        // 최종적으로 FAILED 상태로 만들기
        if (retryCount < 2) {
            task.fail("Test error");
        }

        return task;
    }
}
