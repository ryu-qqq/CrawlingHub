package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * ExecutionId - 스케줄 실행 ID Value Object
 *
 * <p>Auto-increment Long 기반 스케줄 실행 고유 식별자</p>
 *
 * @param value Execution ID (null이면 새로운 엔티티)
 */
public record ExecutionId(Long value) {

    /**
     * 새로운 Execution ID 생성 (null)
     *
     * @return null을 가진 ExecutionId (새 엔티티 표시)
     */
    public static ExecutionId forNew() {
        return new ExecutionId(null);
    }
    /**
     * 기존 ID 값으로 ExecutionId 생성 (정적 팩토리 메서드)
     *
     * @param value ID 값
     * @return ExecutionId 인스턴스
     */
    public static ExecutionId of(Long value) {
        return new ExecutionId(value);
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
