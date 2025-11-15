package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.vo.UserAgentId;
import com.ryuqq.crawlinghub.domain.vo.UserAgentStatus;

/**
 * UserAgent 관련 테스트 데이터 생성 Fixture
 *
 * <p>UserAgent와 관련된 Value Object와 Enum의 기본값을 제공합니다.</p>
 *
 * <p>제공 메서드:</p>
 * <ul>
 *   <li>{@link #defaultUserAgentId()} - 새로운 UserAgentId 생성</li>
 *   <li>{@link #defaultUserAgentStatus()} - 기본 상태 (ACTIVE)</li>
 * </ul>
 */
public class UserAgentFixture {

    /**
     * 기본 UserAgentId 생성
     *
     * @return 새로운 UUID 기반 UserAgentId
     */
    public static UserAgentId defaultUserAgentId() {
        return UserAgentId.generate();
    }

    /**
     * 기본 UserAgentStatus 반환
     *
     * @return ACTIVE 상태
     */
    public static UserAgentStatus defaultUserAgentStatus() {
        return UserAgentStatus.ACTIVE;
    }
}
