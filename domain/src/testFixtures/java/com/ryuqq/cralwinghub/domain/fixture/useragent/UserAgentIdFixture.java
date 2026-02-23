package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;

/**
 * UserAgentId Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class UserAgentIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 할당된 ID 생성
     *
     * @return UserAgentId (value = 1L)
     */
    public static UserAgentId anAssignedId() {
        return UserAgentId.of(DEFAULT_ID);
    }

    /**
     * 특정 값으로 할당된 ID 생성
     *
     * @param value ID 값
     * @return UserAgentId
     */
    public static UserAgentId anAssignedId(Long value) {
        return UserAgentId.of(value);
    }

    /**
     * 미할당 ID 생성
     *
     * @return UserAgentId (value = null)
     */
    public static UserAgentId anUnassignedId() {
        return UserAgentId.forNew();
    }

    private UserAgentIdFixture() {
        // Utility class
    }
}
