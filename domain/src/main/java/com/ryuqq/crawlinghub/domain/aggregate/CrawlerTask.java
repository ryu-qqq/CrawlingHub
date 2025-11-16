package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskStatus;
import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskType;
import com.ryuqq.crawlinghub.domain.vo.SellerId;
import com.ryuqq.crawlinghub.domain.vo.TaskId;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CrawlerTask Aggregate Root
 *
 * <p>머스트잇 크롤링 작업을 표현하는 Aggregate Root입니다.</p>
 *
 * <p>Zero-Tolerance Rules 준수:</p>
 * <ul>
 *   <li>Lombok 금지 - Plain Java 사용</li>
 *   <li>Tell, Don't Ask - 비즈니스 로직은 CrawlerTask 내부에 캡슐화</li>
 *   <li>Long FK 전략 - JPA 관계 어노테이션 없음</li>
 * </ul>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>생성 시 상태는 항상 WAITING</li>
 *   <li>생성 시 retryCount는 0</li>
 *   <li>taskType에 따라 requestUrl 형식 검증</li>
 * </ul>
 */
public class CrawlerTask {

    private final TaskId taskId;
    private final SellerId sellerId;
    private final CrawlerTaskType taskType;
    private final String requestUrl;
    private CrawlerTaskStatus status;
    private Integer retryCount;
    private String errorMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Clock clock;

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성 (신규)
     *
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @param clock 시간 제어
     */
    private CrawlerTask(SellerId sellerId, CrawlerTaskType taskType, String requestUrl, Clock clock) {
        validateRequestUrl(taskType, requestUrl);
        this.taskId = TaskId.generate();
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.requestUrl = requestUrl;
        this.status = CrawlerTaskStatus.WAITING;
        this.retryCount = 0;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성 (재구성)
     *
     * @param taskId Task 식별자
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @param status 작업 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param clock 시간 제어
     */
    private CrawlerTask(TaskId taskId, SellerId sellerId, CrawlerTaskType taskType, String requestUrl,
                         CrawlerTaskStatus status, Integer retryCount, String errorMessage, Clock clock) {
        validateRequestUrl(taskType, requestUrl);
        this.taskId = taskId;
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.requestUrl = requestUrl;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
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
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @return 새로 생성된 CrawlerTask
     * @throws IllegalArgumentException requestUrl 형식이 올바르지 않은 경우
     */
    public static CrawlerTask forNew(SellerId sellerId, CrawlerTaskType taskType, String requestUrl) {
        return forNew(sellerId, taskType, requestUrl, Clock.systemDefaultZone());
    }

    /**
     * 새로운 CrawlerTask 생성 (표준 패턴 + Clock 주입)
     *
     * <p>forNew(Clock) 패턴: 신규 엔티티 생성, Clock 주입</p>
     * <ul>
     *   <li>초기 상태: WAITING</li>
     *   <li>초기 재시도 횟수: 0</li>
     * </ul>
     *
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @param clock 시간 제어
     * @return 새로 생성된 CrawlerTask
     * @throws IllegalArgumentException requestUrl 형식이 올바르지 않은 경우
     */
    public static CrawlerTask forNew(SellerId sellerId, CrawlerTaskType taskType, String requestUrl, Clock clock) {
        return new CrawlerTask(sellerId, taskType, requestUrl, clock);
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
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @return 재구성된 CrawlerTask
     * @throws IllegalArgumentException requestUrl 형식이 올바르지 않은 경우
     */
    public static CrawlerTask of(SellerId sellerId, CrawlerTaskType taskType, String requestUrl) {
        return of(sellerId, taskType, requestUrl, Clock.systemDefaultZone());
    }

    /**
     * 불변 속성으로 CrawlerTask 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>of(Clock) 패턴: 테스트용 간편 생성, Clock 주입</p>
     * <ul>
     *   <li>초기 상태: WAITING</li>
     *   <li>초기 재시도 횟수: 0</li>
     * </ul>
     *
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @param clock 시간 제어
     * @return 재구성된 CrawlerTask
     * @throws IllegalArgumentException requestUrl 형식이 올바르지 않은 경우
     */
    public static CrawlerTask of(SellerId sellerId, CrawlerTaskType taskType, String requestUrl, Clock clock) {
        return new CrawlerTask(sellerId, taskType, requestUrl, clock);
    }

    /**
     * 완전한 CrawlerTask 재구성 (표준 패턴)
     *
     * <p>reconstitute() 패턴: DB에서 조회한 엔티티 재구성</p>
     *
     * @param taskId Task 식별자
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @param status 작업 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @return 재구성된 CrawlerTask
     * @throws IllegalArgumentException requestUrl 형식이 올바르지 않은 경우
     */
    public static CrawlerTask reconstitute(TaskId taskId, SellerId sellerId, CrawlerTaskType taskType,
                                             String requestUrl, CrawlerTaskStatus status,
                                             Integer retryCount, String errorMessage) {
        return reconstitute(taskId, sellerId, taskType, requestUrl, status, retryCount, errorMessage, Clock.systemDefaultZone());
    }

    /**
     * 완전한 CrawlerTask 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>reconstitute(Clock) 패턴: DB에서 조회한 엔티티 재구성, Clock 주입</p>
     *
     * @param taskId Task 식별자
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @param status 작업 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param clock 시간 제어
     * @return 재구성된 CrawlerTask
     * @throws IllegalArgumentException requestUrl 형식이 올바르지 않은 경우
     */
    public static CrawlerTask reconstitute(TaskId taskId, SellerId sellerId, CrawlerTaskType taskType,
                                             String requestUrl, CrawlerTaskStatus status,
                                             Integer retryCount, String errorMessage, Clock clock) {
        return new CrawlerTask(taskId, sellerId, taskType, requestUrl, status, retryCount, errorMessage, clock);
    }

    /**
     * 새로운 CrawlerTask 생성 (레거시)
     *
     * @deprecated Use {@link #forNew(SellerId, CrawlerTaskType, String)} instead
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @return 새로 생성된 CrawlerTask
     * @throws IllegalArgumentException requestUrl 형식이 올바르지 않은 경우
     */
    @Deprecated
    public static CrawlerTask create(SellerId sellerId, CrawlerTaskType taskType, String requestUrl) {
        return forNew(sellerId, taskType, requestUrl);
    }

    /**
     * requestUrl 형식 검증
     *
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @throws IllegalArgumentException URL 형식이 올바르지 않은 경우
     */
    private void validateRequestUrl(CrawlerTaskType taskType, String requestUrl) {
        switch (taskType) {
            case MINISHOP -> {
                if (!requestUrl.contains("/searchmini-shop-search")) {
                    throw new IllegalArgumentException("MINISHOP URL 형식이 올바르지 않습니다");
                }
            }
            case PRODUCT_DETAIL -> {
                if (!requestUrl.matches(".*/item/\\d+/detail/top")) {
                    throw new IllegalArgumentException("PRODUCT_DETAIL URL 형식이 올바르지 않습니다");
                }
            }
            case PRODUCT_OPTION -> {
                if (!requestUrl.matches(".*/auction_products/\\d+/options")) {
                    throw new IllegalArgumentException("PRODUCT_OPTION URL 형식이 올바르지 않습니다");
                }
            }
        }
    }

    /**
     * Task 발행 (WAITING → PUBLISHED)
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>WAITING 상태에서만 발행 가능</li>
     *   <li>발행 시 updatedAt 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException WAITING 상태가 아닌 경우
     */
    public void publish() {
        if (status != CrawlerTaskStatus.WAITING) {
            throw new IllegalStateException("WAITING 상태에서만 발행할 수 있습니다");
        }
        this.status = CrawlerTaskStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Task 시작 (PUBLISHED → IN_PROGRESS 또는 RETRY → IN_PROGRESS)
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>PUBLISHED 또는 RETRY 상태에서만 시작 가능</li>
     *   <li>시작 시 updatedAt 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException PUBLISHED 또는 RETRY 상태가 아닌 경우
     */
    public void start() {
        if (status != CrawlerTaskStatus.PUBLISHED && status != CrawlerTaskStatus.RETRY) {
            throw new IllegalStateException("PUBLISHED 또는 RETRY 상태에서만 시작할 수 있습니다");
        }
        this.status = CrawlerTaskStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Task 완료 (IN_PROGRESS → COMPLETED)
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>IN_PROGRESS 상태에서만 완료 가능</li>
     *   <li>완료 시 updatedAt 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException IN_PROGRESS 상태가 아닌 경우
     */
    public void complete() {
        if (status != CrawlerTaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("IN_PROGRESS 상태에서만 완료할 수 있습니다");
        }
        this.status = CrawlerTaskStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Task 실패 (IN_PROGRESS → FAILED)
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>IN_PROGRESS 상태에서만 실패 처리 가능</li>
     *   <li>에러 메시지 기록</li>
     *   <li>실패 시 updatedAt 갱신</li>
     * </ul>
     *
     * @param errorMessage 실패 사유
     * @throws IllegalStateException IN_PROGRESS 상태가 아닌 경우
     */
    public void fail(String errorMessage) {
        if (status != CrawlerTaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("IN_PROGRESS 상태에서만 실패 처리할 수 있습니다");
        }
        this.status = CrawlerTaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Task 재시도 (FAILED → RETRY)
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>FAILED 상태에서만 재시도 가능</li>
     *   <li>최대 재시도 횟수는 2회</li>
     *   <li>재시도 시 retryCount 증가</li>
     *   <li>재시도 시 errorMessage 초기화</li>
     *   <li>재시도 시 updatedAt 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException FAILED 상태가 아니거나 재시도 횟수 초과 시
     */
    public void retry() {
        if (status != CrawlerTaskStatus.FAILED) {
            throw new IllegalStateException("FAILED 상태에서만 재시도할 수 있습니다");
        }
        if (retryCount >= 2) {
            throw new IllegalStateException("재시도 횟수를 초과했습니다 (최대 2회)");
        }
        this.status = CrawlerTaskStatus.RETRY;
        this.retryCount++;
        this.errorMessage = null;
        this.updatedAt = LocalDateTime.now(clock);
    }

    // Getters (필요한 것만)
    public TaskId getTaskId() {
        return taskId;
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public CrawlerTaskType getTaskType() {
        return taskType;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public CrawlerTaskStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
