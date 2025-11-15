package com.ryuqq.crawlinghub.domain.vo;

import java.util.UUID;

/**
 * UserAgent 식별자 Value Object
 *
 * <p>UserAgent의 고유 식별자를 표현합니다.</p>
 *
 * <p>UUID 기반으로 생성되어 고유성을 보장합니다.</p>
 *
 * @param value UUID 값
 */
public record UserAgentId(UUID value) {

    /**
     * 새로운 UserAgentId 생성
     *
     * @return 고유한 UserAgentId
     */
    public static UserAgentId generate() {
        return new UserAgentId(UUID.randomUUID());
    }
}
