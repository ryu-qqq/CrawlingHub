package com.ryuqq.crawlinghub.domain.useragent.identifier;

/**
 * UserAgent ID Value Object
 *
 * <p><strong>생성 패턴</strong>:
 *
 * <ul>
 *   <li>{@code unassigned()} - 미할당 상태 (ID = null, Auto Increment 대비)
 *   <li>{@code of(Long value)} - 값 기반 생성 (null 체크 필수)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentId(Long value) {

    /**
     * Compact Constructor (검증 로직)
     *
     * <p>null이 아닌 값은 양수여야 합니다.
     */
    public UserAgentId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("UserAgentId 값은 양수여야 합니다: " + value);
        }
    }

    /**
     * 미할당 ID 생성 (Auto Increment 대비)
     *
     * @return UserAgentId (value = null)
     */
    public static UserAgentId forNew() {
        return new UserAgentId(null);
    }

    /**
     * 미할당 ID 생성 (forNew 별칭)
     *
     * @return UserAgentId (value = null)
     */
    public static UserAgentId unassigned() {
        return forNew();
    }

    /**
     * 값 기반 생성
     *
     * @param value ID 값 (null 불가)
     * @return UserAgentId
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public static UserAgentId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("UserAgentId 값은 null일 수 없습니다.");
        }
        return new UserAgentId(value);
    }
}
