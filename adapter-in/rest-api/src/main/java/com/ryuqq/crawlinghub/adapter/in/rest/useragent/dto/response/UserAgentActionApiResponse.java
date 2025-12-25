package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

/**
 * UserAgent Action API Response
 *
 * <p>UserAgent 액션 (suspend, recover, reset-health) API 응답 DTO
 *
 * @param userAgentId 대상 UserAgent ID
 * @param action 수행된 액션
 * @param message 결과 메시지
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentActionApiResponse(long userAgentId, String action, String message) {

    public static UserAgentActionApiResponse suspended(long userAgentId) {
        return new UserAgentActionApiResponse(
                userAgentId,
                "SUSPEND",
                String.format("User agent %d suspended successfully.", userAgentId));
    }

    public static UserAgentActionApiResponse recovered(long userAgentId) {
        return new UserAgentActionApiResponse(
                userAgentId,
                "RECOVER",
                String.format("User agent %d recovered successfully.", userAgentId));
    }

    public static UserAgentActionApiResponse healthReset(long userAgentId) {
        return new UserAgentActionApiResponse(
                userAgentId,
                "RESET_HEALTH",
                String.format("User agent %d health score reset to 100.", userAgentId));
    }
}
