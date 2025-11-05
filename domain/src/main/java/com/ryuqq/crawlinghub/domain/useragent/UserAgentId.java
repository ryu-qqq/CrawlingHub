package com.ryuqq.crawlinghub.domain.useragent;

/**
 * UserAgent 식별자
 */
public record UserAgentId(Long value) {

    public UserAgentId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("UserAgent ID는 양수여야 합니다");
        }
    }

    public static UserAgentId of(Long value) {
        return new UserAgentId(value);
    }
}
