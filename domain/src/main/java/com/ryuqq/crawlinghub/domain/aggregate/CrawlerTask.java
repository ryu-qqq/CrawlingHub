package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskStatus;
import com.ryuqq.crawlinghub.domain.vo.CrawlerTaskType;
import com.ryuqq.crawlinghub.domain.vo.SellerId;
import com.ryuqq.crawlinghub.domain.vo.TaskId;

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

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성
     *
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     */
    private CrawlerTask(SellerId sellerId, CrawlerTaskType taskType, String requestUrl) {
        validateRequestUrl(taskType, requestUrl);
        this.taskId = TaskId.generate();
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.requestUrl = requestUrl;
        this.status = CrawlerTaskStatus.WAITING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 새로운 CrawlerTask 생성
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>초기 상태: WAITING</li>
     *   <li>초기 재시도 횟수: 0</li>
     *   <li>requestUrl 형식은 taskType에 따라 검증</li>
     * </ul>
     *
     * @param sellerId 셀러 식별자
     * @param taskType 크롤링 작업 타입
     * @param requestUrl 요청 URL
     * @return 새로 생성된 CrawlerTask
     * @throws IllegalArgumentException requestUrl 형식이 올바르지 않은 경우
     */
    public static CrawlerTask create(SellerId sellerId, CrawlerTaskType taskType, String requestUrl) {
        return new CrawlerTask(sellerId, taskType, requestUrl);
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
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Task 시작 (PUBLISHED → IN_PROGRESS)
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>PUBLISHED 상태에서만 시작 가능</li>
     *   <li>시작 시 updatedAt 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException PUBLISHED 상태가 아닌 경우
     */
    public void start() {
        if (status != CrawlerTaskStatus.PUBLISHED) {
            throw new IllegalStateException("PUBLISHED 상태에서만 시작할 수 있습니다");
        }
        this.status = CrawlerTaskStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
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
        this.updatedAt = LocalDateTime.now();
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
        this.updatedAt = LocalDateTime.now();
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
}
