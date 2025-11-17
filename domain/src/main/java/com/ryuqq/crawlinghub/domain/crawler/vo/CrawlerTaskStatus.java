package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * 크롤러 작업 상태 Enum
 *
 * <p>크롤링 작업의 생명주기 상태를 표현합니다.</p>
 *
 * <p>상태 전이 흐름:</p>
 * <pre>
 * WAITING → PUBLISHED → IN_PROGRESS → COMPLETED
 *                     ↓
 *                   FAILED → RETRY (최대 2회)
 * </pre>
 *
 * <p>상태 정의:</p>
 * <ul>
 *   <li>{@link #WAITING} - 작업 생성 대기</li>
 *   <li>{@link #PUBLISHED} - 작업 큐에 발행됨</li>
 *   <li>{@link #IN_PROGRESS} - 실행 중</li>
 *   <li>{@link #COMPLETED} - 완료</li>
 *   <li>{@link #FAILED} - 실패</li>
 *   <li>{@link #RETRY} - 재시도 대기</li>
 * </ul>
 */
public enum CrawlerTaskStatus {

    /**
     * 작업 생성 대기 중
     */
    WAITING,

    /**
     * 작업 큐에 발행됨
     */
    PUBLISHED,

    /**
     * 크롤링 실행 중
     */
    IN_PROGRESS,

    /**
     * 크롤링 완료
     */
    COMPLETED,

    /**
     * 크롤링 실패
     */
    FAILED,

    /**
     * 재시도 대기 중
     */
    RETRY;

    /**
     * String 값으로부터 CrawlerTaskStatus 생성 (표준 패턴)
     *
     * @param value 문자열 값
     * @return CrawlerTaskStatus enum
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     */
    public static CrawlerTaskStatus of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("CrawlerTaskStatus cannot be null");
        }
        return valueOf(value.toUpperCase());
    }
}
