package com.ryuqq.crawlinghub.domain.task;

import java.util.UUID;

/**
 * Task 식별자 (Value Object)
 *
 * <p>크롤링 태스크의 고유 식별자
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public record TaskId(Long value) {

    /**
     * Compact Constructor (Validation)
     */
    public TaskId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Task ID는 양수여야 합니다: " + value);
        }
    }

    /**
     * 팩토리 메서드
     */
    public static TaskId of(Long value) {
        return new TaskId(value);
    }

    /**
     * 새 ID 생성 (Persistence 저장 전 임시 ID)
     *
     * <p>실제 DB 저장 시 Auto Increment로 대체됨
     */
    public static TaskId newId() {
        // UUID 기반 임시 Long ID 생성
        return new TaskId(Math.abs((long) UUID.randomUUID().hashCode()));
    }
}
