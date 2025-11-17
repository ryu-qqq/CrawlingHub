package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * UserAgent 식별자 Value Object
 *
 * <p>Auto-increment Long 기반 UserAgent 고유 식별자</p>
 *
 * @param value UserAgent ID (null이면 새로운 엔티티)
 */
public record UserAgentId(Long value) {

    /**
     * 새로운 UserAgent ID 생성 (null)
     *
     * @return null을 가진 UserAgentId (새 엔티티 표시)
     */
    public static UserAgentId forNew() {
        return new UserAgentId(null);
    }
    /**
     * 기존 ID 값으로 UserAgentId 생성 (정적 팩토리 메서드)
     *
     * @param value ID 값
     * @return UserAgentId 인스턴스
     */
    public static UserAgentId of(Long value) {
        return new UserAgentId(value);
    }


    /**
     * 새로운 엔티티인지 확인
     *
     * @return value가 null이면 true (아직 DB에 저장되지 않음)
     */
    public boolean isNew() {
        return value == null;
    }
}
