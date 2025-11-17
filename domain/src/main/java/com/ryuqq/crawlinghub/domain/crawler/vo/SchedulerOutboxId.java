package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * SchedulerOutbox ID Value Object
 *
 * <p>Auto-increment Long 기반 SchedulerOutbox 고유 식별자</p>
 *
 * @param value SchedulerOutbox ID (null이면 새로운 엔티티)
 */
public record SchedulerOutboxId(Long value) {

    /**
     * 새로운 SchedulerOutbox ID 생성 (null)
     *
     * @return null을 가진 SchedulerOutboxId (새 엔티티 표시)
     */
    public static SchedulerOutboxId forNew() {
        return new SchedulerOutboxId(null);
    }
    /**
     * 기존 ID 값으로 SchedulerOutboxId 생성 (정적 팩토리 메서드)
     *
     * @param value ID 값
     * @return SchedulerOutboxId 인스턴스
     */
    public static SchedulerOutboxId of(Long value) {
        return new SchedulerOutboxId(value);
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
