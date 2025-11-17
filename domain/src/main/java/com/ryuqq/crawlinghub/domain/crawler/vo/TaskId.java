package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * 크롤러 작업 식별자 Value Object
 *
 * <p>Auto-increment Long 기반 크롤링 작업 고유 식별자</p>
 *
 * @param value Task ID (null이면 새로운 엔티티)
 */
public record TaskId(Long value) {

    /**
     * 새로운 Task ID 생성 (null)
     *
     * @return null을 가진 TaskId (새 엔티티 표시)
     */
    public static TaskId forNew() {
        return new TaskId(null);
    }
    /**
     * 기존 ID 값으로 TaskId 생성 (정적 팩토리 메서드)
     *
     * @param value ID 값
     * @return TaskId 인스턴스
     */
    public static TaskId of(Long value) {
        return new TaskId(value);
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
