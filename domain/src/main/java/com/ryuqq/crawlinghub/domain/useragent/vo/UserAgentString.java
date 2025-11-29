package com.ryuqq.crawlinghub.domain.useragent.vo;

/**
 * UserAgent String Value Object
 *
 * <p>실제 User-Agent 헤더 문자열을 관리합니다.
 *
 * <p><strong>예시</strong>:
 *
 * <pre>
 * Mozilla/5.0 (iPhone; CPU iPhone OS 13_0 like Mac OS X) AppleWebKit/605.1.15
 * Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36
 * </pre>
 *
 * <p><strong>검증 규칙</strong>:
 *
 * <ul>
 *   <li>null 또는 빈 문자열 불가
 *   <li>최대 길이: 500자
 *   <li>최소 길이: 10자 (최소한의 User-Agent 형식)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentString(String value) {

    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 500;

    /** Compact Constructor (검증 로직) */
    public UserAgentString {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("User-Agent 문자열은 null이거나 빈 문자열일 수 없습니다.");
        }
        if (value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "User-Agent 문자열은 최소 " + MIN_LENGTH + "자 이상이어야 합니다: " + value.length());
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "User-Agent 문자열은 최대 " + MAX_LENGTH + "자를 초과할 수 없습니다: " + value.length());
        }
    }

    /**
     * User-Agent 문자열로 생성
     *
     * @param value User-Agent 헤더 문자열
     * @return UserAgentString
     */
    public static UserAgentString of(String value) {
        return new UserAgentString(value);
    }
}
