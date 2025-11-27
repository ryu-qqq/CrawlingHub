package com.ryuqq.crawlinghub.domain.useragent.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;

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
 * @author development-team
 * @since 1.0.0
 */
public final class SessionRequiredEvent implements DomainEvent {

    private final UserAgentId userAgentId;
    private final String userAgentValue;

    public SessionRequiredEvent(UserAgentId userAgentId, String userAgentValue) {
        if (userAgentId == null) {
            throw new IllegalArgumentException("userAgentId must not be null");
        }
        if (userAgentValue == null || userAgentValue.isBlank()) {
            throw new IllegalArgumentException("userAgentValue must not be null or blank");
        }
        this.userAgentId = userAgentId;
        this.userAgentValue = userAgentValue;
    }

    public UserAgentId userAgentId() {
        return userAgentId;
    }

    public String userAgentValue() {
        return userAgentValue;
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
                + '}';
    }

    private String truncateUserAgent(String value) {
        if (value == null) {
            return "null";
        }
        return value.length() > 50 ? value.substring(0, 50) + "..." : value;
    }
}
