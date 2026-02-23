package com.ryuqq.crawlinghub.domain.execution.vo;

/**
 * 크롤링 결과 VO
 *
 * <p>크롤러 실행 결과를 담는 불변 도메인 Value Object. HTTP 응답 정보와 성공/실패 상태를 포함.
 *
 * @param success 성공 여부
 * @param responseBody HTTP 응답 바디
 * @param httpStatusCode HTTP 상태 코드
 * @param errorMessage 에러 메시지 (실패 시)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlResult(
        boolean success, String responseBody, Integer httpStatusCode, String errorMessage) {

    /**
     * 성공 결과 생성
     *
     * @param responseBody HTTP 응답 바디
     * @param httpStatusCode HTTP 상태 코드
     * @return 성공 결과
     */
    public static CrawlResult success(String responseBody, int httpStatusCode) {
        return new CrawlResult(true, responseBody, httpStatusCode, null);
    }

    /**
     * 실패 결과 생성
     *
     * @param errorMessage 에러 메시지
     * @return 실패 결과
     */
    public static CrawlResult failure(String errorMessage) {
        return new CrawlResult(false, null, null, errorMessage);
    }

    /**
     * HTTP 에러로 인한 실패 결과 생성
     *
     * @param httpStatusCode HTTP 상태 코드
     * @param errorMessage 에러 메시지
     * @return 실패 결과
     */
    public static CrawlResult failure(int httpStatusCode, String errorMessage) {
        return new CrawlResult(false, null, httpStatusCode, errorMessage);
    }

    /**
     * 성공 여부 확인
     *
     * @return 성공이면 true
     */
    public boolean isSuccess() {
        return success;
    }
}
