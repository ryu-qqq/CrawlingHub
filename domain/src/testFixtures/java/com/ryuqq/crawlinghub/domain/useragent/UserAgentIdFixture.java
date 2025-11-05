package com.ryuqq.crawlinghub.domain.useragent;

/**
 * UserAgentId Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class UserAgentIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 UserAgentId 생성
     *
     * @return UserAgentId
     */
    public static UserAgentId create() {
        return UserAgentId.of(DEFAULT_ID);
    }

    /**
     * 지정된 ID로 UserAgentId 생성
     *
     * @param id ID 값
     * @return UserAgentId
     */
    public static UserAgentId createWithId(Long id) {
        return UserAgentId.of(id);
    }

    /**
     * null ID로 UserAgentId 생성 (신규 엔티티용)
     *
     * @return null
     */
    public static UserAgentId createNull() {
        return null;
    }
}
