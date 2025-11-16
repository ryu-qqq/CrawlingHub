package com.ryuqq.crawlinghub.domain.vo;

/**
 * UserAgent 상태 Enum
 *
 * <p>UserAgent의 생명주기 상태를 표현합니다.</p>
 *
 * <p>상태 정의:</p>
 * <ul>
 *   <li>{@link #ACTIVE} - 활성 상태 (정상 사용 가능)</li>
 *   <li>{@link #SUSPENDED} - 일시 중단 (429 응답 등으로 일시 차단)</li>
 *   <li>{@link #BLOCKED} - 영구 차단 (더 이상 사용 불가)</li>
 * </ul>
 *
 * <p>상태 전이 흐름:</p>
 * <pre>
 * ACTIVE ↔ SUSPENDED → BLOCKED
 * </pre>
 */
public enum UserAgentStatus {

    /**
     * 활성 상태 - 정상 사용 가능
     */
    ACTIVE,

    /**
     * 일시 중단 - 429 응답 등으로 일시 차단
     */
    SUSPENDED,

    /**
     * 영구 차단 - 더 이상 사용 불가
     */
    BLOCKED;

    /**
     * String 값으로부터 UserAgentStatus 생성 (표준 패턴)
     *
     * @param value 문자열 값
     * @return UserAgentStatus enum
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     */
    public static UserAgentStatus of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("UserAgentStatus cannot be null");
        }
        return valueOf(value.toUpperCase());
    }
}
