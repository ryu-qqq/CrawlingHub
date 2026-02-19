package com.ryuqq.crawlinghub.domain.useragent.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.time.Instant;

/**
 * 세션 발급 필요 이벤트
 *
 * <p>UserAgent가 세션 토큰 발급이 필요한 상태가 되었을 때 발행되는 이벤트입니다.
 *
 * <p><strong>발행 시점</strong>:
 *
 * <ul>
 *   <li>UserAgent가 처음 풀에 추가될 때 (SESSION_REQUIRED 상태)
 *   <li>기존 세션이 만료되어 재발급이 필요할 때
 *   <li>429 응답 등으로 세션이 무효화되었을 때
 * </ul>
 *
 * @param userAgentId UserAgent ID
 * @param userAgentValue User-Agent 문자열
 * @param occurredAt 이벤트 발생 시각
 * @author development-team
 * @since 1.0.0
 */
public record SessionRequiredEvent(
        UserAgentId userAgentId, String userAgentValue, Instant occurredAt) implements DomainEvent {

    /** Compact Constructor (검증 로직) */
    public SessionRequiredEvent {
        if (userAgentId == null) {
            throw new IllegalArgumentException("userAgentId must not be null");
        }
        if (userAgentValue == null || userAgentValue.isBlank()) {
            throw new IllegalArgumentException("userAgentValue must not be null or blank");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
    }

    /**
     * 팩토리 메서드
     *
     * @param userAgentId UserAgent ID
     * @param userAgentValue User-Agent 문자열
     * @param now 현재 시각
     * @return SessionRequiredEvent
     */
    public static SessionRequiredEvent of(
            UserAgentId userAgentId, String userAgentValue, Instant now) {
        return new SessionRequiredEvent(userAgentId, userAgentValue, now);
    }

    public Long getUserAgentIdValue() {
        return userAgentId.value();
    }

    @Override
    public String toString() {
        return "SessionRequiredEvent{"
                + "userAgentId="
                + userAgentId.value()
                + ", userAgentValue='"
                + truncateUserAgent(userAgentValue)
                + '\''
                + ", occurredAt="
                + occurredAt
                + '}';
    }

    private String truncateUserAgent(String value) {
        if (value == null) {
            return "null";
        }
        return value.length() > 50 ? value.substring(0, 50) + "..." : value;
    }
}
