package com.ryuqq.crawlinghub.domain.crawler.exception;

import com.ryuqq.crawlinghub.domain.common.ErrorCode;

/**
 * CrawlerErrorCode - Crawler Bounded Context 에러 코드
 *
 * <p>Crawler 도메인에서 발생하는 모든 비즈니스 예외의 에러 코드를 정의합니다.</p>
 *
 * <p><strong>에러 코드 규칙:</strong></p>
 * <ul>
 *   <li>✅ 형식: CRAWLER-{3자리 숫자}</li>
 *   <li>✅ HTTP 상태 코드 매핑</li>
 *   <li>✅ 명확한 에러 메시지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public enum CrawlerErrorCode implements ErrorCode {

    /**
     * CrawlerTask를 찾을 수 없음
     */
    CRAWLER_TASK_NOT_FOUND("CRAWLER-001", 404, "Crawler task not found"),

    /**
     * UserAgent를 찾을 수 없음
     */
    USER_AGENT_NOT_FOUND("CRAWLER-002", 404, "User agent not found"),

    /**
     * 유효하지 않은 Crawler 파라미터
     */
    INVALID_CRAWLER_ARGUMENT("CRAWLER-003", 400, "Invalid crawler argument"),

    /**
     * CrawlerTask 상태 전환 불가
     */
    INVALID_TASK_STATE("CRAWLER-004", 400, "Invalid task state transition"),

    /**
     * UserAgent 요청 제한 초과
     */
    USER_AGENT_RATE_LIMIT_EXCEEDED("CRAWLER-005", 429, "User agent rate limit exceeded"),

    /**
     * 유효하지 않은 RequestUrl
     */
    INVALID_REQUEST_URL("CRAWLER-006", 400, "Invalid request URL");

    private final String code;
    private final int httpStatus;
    private final String message;

    /**
     * Constructor - ErrorCode 생성
     *
     * @param code 에러 코드 (CRAWLER-XXX)
     * @param httpStatus HTTP 상태 코드
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-11-17
     */
    CrawlerErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
