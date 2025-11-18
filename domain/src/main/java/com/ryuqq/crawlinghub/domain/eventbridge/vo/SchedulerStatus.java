package com.ryuqq.crawlinghub.domain.eventbridge.vo;

/**
 * Scheduler 상태 Enum
 *
 * <p>EventBridge 스케줄러의 라이프사이클 상태를 표현합니다.</p>
 */
public enum SchedulerStatus {
    PENDING,
    ACTIVE,
    INACTIVE;

    public boolean canTransitionTo(SchedulerStatus target) {
        if (target == null) {
            return false;
        }

        return switch (this) {
            case PENDING -> target == ACTIVE;
            case ACTIVE -> target == INACTIVE;
            case INACTIVE -> target == ACTIVE;
        };
    }
}

