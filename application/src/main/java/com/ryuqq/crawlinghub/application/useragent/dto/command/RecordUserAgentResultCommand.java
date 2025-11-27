package com.ryuqq.crawlinghub.application.useragent.dto.command;

/**
 * UserAgent 결과 기록 Command
 *
 * <p>크롤링 결과를 기록하기 위한 Command 객체입니다.
 *
 * <p><strong>결과 처리 규칙</strong>:
 *
 * <ul>
 *   <li>성공: Health Score +5
 *   <li>429 응답: 즉시 SUSPENDED
 *   <li>기타 에러: 단순 로깅
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record RecordUserAgentResultCommand(Long userAgentId, int httpStatusCode, boolean success) {
    private static final int HTTP_OK = 200;
    private static final int HTTP_RATE_LIMITED = 429;

    /**
     * 성공 결과 Command 생성
     *
     * @param userAgentId UserAgent ID
     * @return 성공 Command (statusCode=200, success=true)
     */
    public static RecordUserAgentResultCommand success(Long userAgentId) {
        return new RecordUserAgentResultCommand(userAgentId, HTTP_OK, true);
    }

    /**
     * 실패 결과 Command 생성
     *
     * @param userAgentId UserAgent ID
     * @param httpStatusCode HTTP 상태 코드
     * @return 실패 Command (success=false)
     */
    public static RecordUserAgentResultCommand failure(Long userAgentId, int httpStatusCode) {
        return new RecordUserAgentResultCommand(userAgentId, httpStatusCode, false);
    }

    /**
     * Rate Limit (429) 응답인지 확인
     *
     * @return httpStatusCode == 429이면 true
     */
    public boolean isRateLimited() {
        return httpStatusCode == HTTP_RATE_LIMITED;
    }

    /**
     * 서버 에러 (5xx) 응답인지 확인
     *
     * @return httpStatusCode >= 500이면 true
     */
    public boolean isServerError() {
        return httpStatusCode >= 500;
    }
}
