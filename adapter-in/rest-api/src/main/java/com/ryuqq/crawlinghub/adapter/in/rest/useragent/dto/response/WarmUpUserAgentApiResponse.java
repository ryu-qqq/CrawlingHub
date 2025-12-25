package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

/**
 * Warm-up UserAgent API Response
 *
 * <p>UserAgent Pool Warm-up API 응답 DTO
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>addedCount: Pool에 추가된 UserAgent 수
 *   <li>message: 결과 메시지
 * </ul>
 *
 * @param addedCount Pool에 추가된 UserAgent 수
 * @param message 결과 메시지
 * @author development-team
 * @since 1.0.0
 */
public record WarmUpUserAgentApiResponse(int addedCount, String message) {

    /**
     * WarmUpUserAgentApiResponse 생성
     *
     * @param count Pool에 추가된 UserAgent 수
     * @return WarmUpUserAgentApiResponse
     */
    public static WarmUpUserAgentApiResponse of(int count) {
        String message =
                count > 0
                        ? count + " user agents added to pool (Lazy session issuance)"
                        : "No user agents to warm up";
        return new WarmUpUserAgentApiResponse(count, message);
    }
}
