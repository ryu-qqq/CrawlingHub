package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * ProductOutbox 식별자 Value Object
 *
 * <p>Auto-increment Long 기반 ProductOutbox 고유 식별자</p>
 *
 * @param value Outbox ID (null이면 새로운 엔티티)
 */
public record OutboxId(Long value) {

    /**
     * 새로운 Outbox ID 생성 (null)
     *
     * @return null을 가진 OutboxId (새 엔티티 표시)
     */
    public static OutboxId forNew() {
        return new OutboxId(null);
    }
    /**
     * 기존 ID 값으로 OutboxId 생성 (정적 팩토리 메서드)
     *
     * @param value ID 값
     * @return OutboxId 인스턴스
     */
    public static OutboxId of(Long value) {
        return new OutboxId(value);
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
