package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.aggregate.CrawlerTask;
import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskStatus;
import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskType;
import com.ryuqq.crawlinghub.domain.vo.SellerId;
import com.ryuqq.crawlinghub.domain.vo.TaskId;

import java.time.Clock;

/**
 * 크롤러 작업 관련 테스트 데이터 생성 Fixture
 *
 * <p>CrawlerTask Aggregate와 관련 Value Object의 테스트 데이터를 제공합니다.</p>
 *
 * <p>표준 패턴 준수:</p>
 * <ul>
 *   <li>{@link #forNew()} - 새 CrawlerTask 생성 (ID 자동 생성)</li>
 *   <li>{@link #of(SellerId, CrawlerTaskType, String)} - 불변 속성으로 재구성</li>
 *   <li>{@link #reconstitute(TaskId, SellerId, CrawlerTaskType, String, CrawlerTaskStatus, Integer, String)} - 완전한 재구성</li>
 * </ul>
 *
 * <p>레거시 호환 메서드:</p>
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

    private static final SellerId DEFAULT_SELLER_ID = new SellerId("seller_test_001");
    private static final CrawlerTaskType DEFAULT_TASK_TYPE = CrawlerTaskType.MINISHOP;
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
     * 새로운 CrawlerTask 생성 (표준 패턴)
     *
     * <p>forNew() 패턴: 신규 엔티티 생성</p>
     * <ul>
     *   <li>초기 상태: WAITING</li>
     *   <li>초기 재시도 횟수: 0</li>
     * </ul>
     *
     * @return 새로 생성된 CrawlerTask
     */
    public static CrawlerTask forNew() {
        return forNew(Clock.systemDefaultZone());
    }

    /**
     * 새로운 CrawlerTask 생성 (표준 패턴 + Clock 주입)
     *
     * <p>forNew(Clock) 패턴: ID 자동 생성, WAITING 상태, Clock 주입</p>
     *
     * @param clock 시간 제어 (테스트 가능성)
     * @return 새로 생성된 CrawlerTask
     */
    public static CrawlerTask forNew(Clock clock) {
        return CrawlerTask.forNew(DEFAULT_SELLER_ID, DEFAULT_TASK_TYPE, DEFAULT_REQUEST_URL, clock);
    }

    /**
     * 불변 속성으로 CrawlerTask 재구성 (표준 패턴)
     *
     * <p>of() 패턴: 테스트용 간편 생성</p>
     * <ul>
     *   <li>초기 상태: WAITING</li>
     *   <li>초기 재시도 횟수: 0</li>
     * </ul>
     *
     * @param sellerId Seller ID
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @return 재구성된 CrawlerTask
     */
    public static CrawlerTask of(SellerId sellerId, CrawlerTaskType taskType, String requestUrl) {
        return of(sellerId, taskType, requestUrl, Clock.systemDefaultZone());
    }

    /**
     * 불변 속성으로 CrawlerTask 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>of(Clock) 패턴: 테스트용 간편 생성, Clock 주입</p>
     *
     * @param sellerId Seller ID
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @param clock 시간 제어
     * @return 재구성된 CrawlerTask
     */
    public static CrawlerTask of(SellerId sellerId, CrawlerTaskType taskType, String requestUrl, Clock clock) {
        return CrawlerTask.of(sellerId, taskType, requestUrl, clock);
    }

    /**
     * 완전한 CrawlerTask 재구성 (표준 패턴)
     *
     * <p>reconstitute() 패턴: DB에서 조회한 엔티티 재구성</p>
     *
     * @param taskId Task ID
     * @param sellerId Seller ID
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @param status 작업 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @return 재구성된 CrawlerTask
     */
    public static CrawlerTask reconstitute(TaskId taskId, SellerId sellerId, CrawlerTaskType taskType,
                                            String requestUrl, CrawlerTaskStatus status,
                                            Integer retryCount, String errorMessage) {
        return reconstitute(taskId, sellerId, taskType, requestUrl, status, retryCount, errorMessage, Clock.systemDefaultZone());
    }

    /**
     * 완전한 CrawlerTask 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>reconstitute(Clock) 패턴: 모든 필드 포함, DB 조회 시뮬레이션, Clock 주입</p>
     *
     * @param taskId Task ID
     * @param sellerId Seller ID
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @param status 작업 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param clock 시간 제어
     * @return 재구성된 CrawlerTask
     */
    public static CrawlerTask reconstitute(TaskId taskId, SellerId sellerId, CrawlerTaskType taskType,
                                            String requestUrl, CrawlerTaskStatus status,
                                            Integer retryCount, String errorMessage, Clock clock) {
        return CrawlerTask.reconstitute(taskId, sellerId, taskType, requestUrl, status, retryCount, errorMessage, clock);
    }

    /**
     * WAITING 상태의 CrawlerTask 생성 (레거시)
     *
     * @deprecated Use {@link #forNew()} instead
     * @return 새로 생성된 WAITING 상태의 CrawlerTask
     */
    @Deprecated
    public static CrawlerTask waitingTask() {
        return forNew();
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
