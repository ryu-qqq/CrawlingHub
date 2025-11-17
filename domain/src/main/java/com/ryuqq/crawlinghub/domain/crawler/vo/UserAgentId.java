package com.ryuqq.crawlinghub.domain.crawler.vo;

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

    /**
     * 새로운 UserAgentId 생성 (표준 패턴)
     *
     * @return 새로 생성된 UserAgentId
     */
    public static UserAgentId forNew() {
        return generate();
    }

    /**
     * 새로운 ID인지 확인 (표준 패턴)
     *
     * <p>UUID 기반 ID는 생성 시점에서만 의미가 있으므로 항상 true를 반환합니다.</p>
     * <p>실제 영속성 상태는 Aggregate Root에서 관리됩니다.</p>
     *
     * @return 항상 true
     */
    public boolean isNew() {
        return true;
    }
}
