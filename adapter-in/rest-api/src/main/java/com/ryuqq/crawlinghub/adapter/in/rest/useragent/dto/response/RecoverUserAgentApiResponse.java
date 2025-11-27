package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

/**
 * Recover UserAgent API Response
 *
 * <p>UserAgent 복구 API 응답 DTO
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>recoveredCount: 복구된 UserAgent 수
 *   <li>message: 결과 메시지
 * </ul>
 *
 * @param recoveredCount 복구된 UserAgent 수
 * @param message 결과 메시지
 * @author development-team
 * @since 1.0.0
 */
public record RecoverUserAgentApiResponse(int recoveredCount, String message) {

    /**
     * RecoverUserAgentApiResponse 생성
     *
     * @param count 복구된 UserAgent 수
     * @return RecoverUserAgentApiResponse
     */
    public static RecoverUserAgentApiResponse of(int count) {
        String message =
                count > 0
                        ? count + " user agents recovered successfully"
                        : "No user agents to recover";
        return new RecoverUserAgentApiResponse(count, message);
    }
}
