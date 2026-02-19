package com.ryuqq.crawlinghub.domain.execution.vo;

import java.time.Duration;
import java.time.Instant;

/**
 * 실행 시간 Value Object
 *
 * <p>크롤링 실행의 시작/종료 시간과 소요 시간을 관리합니다.
 *
 * <p><strong>생명주기</strong>:
 *
 * <ol>
 *   <li>{@code start(Instant)} - 실행 시작 (startedAt만 기록)
 *   <li>{@code complete(Instant)} - 실행 완료 (completedAt, durationMs 계산)
 * </ol>
 *
 * @param startedAt 실행 시작 시간
 * @param completedAt 실행 완료 시간 (nullable, 진행 중이면 null)
 * @param durationMs 소요 시간 (밀리초, 완료 전이면 null)
 * @author development-team
 * @since 1.0.0
 */
public record ExecutionDuration(Instant startedAt, Instant completedAt, Long durationMs) {

    /** Compact Constructor (검증 로직) */
    public ExecutionDuration {
        if (startedAt == null) {
            throw new IllegalArgumentException("시작 시간은 null일 수 없습니다.");
        }
        if (completedAt != null && completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("완료 시간은 시작 시간보다 이전일 수 없습니다.");
        }
        if (durationMs != null && durationMs < 0) {
            throw new IllegalArgumentException("소요 시간은 음수일 수 없습니다: " + durationMs);
        }
    }

    /**
     * 실행 시작
     *
     * @param now 현재 시각
     * @return 시작 상태의 ExecutionDuration
     */
    public static ExecutionDuration start(Instant now) {
        return new ExecutionDuration(now, null, null);
    }

    /**
     * 특정 시간으로 실행 시작 (테스트용)
     *
     * @param startedAt 시작 시간
     * @return 시작 상태의 ExecutionDuration
     */
    public static ExecutionDuration startAt(Instant startedAt) {
        return new ExecutionDuration(startedAt, null, null);
    }

    /**
     * 기존 데이터로 복원 (영속성 계층 전용)
     *
     * @param startedAt 시작 시간
     * @param completedAt 완료 시간 (nullable)
     * @param durationMs 소요 시간 (nullable)
     * @return 복원된 ExecutionDuration
     */
    public static ExecutionDuration reconstitute(
            Instant startedAt, Instant completedAt, Long durationMs) {
        return new ExecutionDuration(startedAt, completedAt, durationMs);
    }

    /**
     * 실행 완료 처리
     *
     * @param now 현재 시각
     * @return 완료 상태의 ExecutionDuration (새 인스턴스)
     */
    public ExecutionDuration complete(Instant now) {
        long duration = Duration.between(this.startedAt, now).toMillis();
        return new ExecutionDuration(this.startedAt, now, duration);
    }

    /**
     * 특정 시간으로 실행 완료 (테스트용)
     *
     * @param completedAt 완료 시간
     * @return 완료 상태의 ExecutionDuration (새 인스턴스)
     */
    public ExecutionDuration completeAt(Instant completedAt) {
        long duration = Duration.between(this.startedAt, completedAt).toMillis();
        return new ExecutionDuration(this.startedAt, completedAt, duration);
    }

    /**
     * 실행 중 여부 확인
     *
     * @return 완료되지 않았으면 true
     */
    public boolean isRunning() {
        return completedAt == null;
    }

    /**
     * 실행 완료 여부 확인
     *
     * @return 완료되었으면 true
     */
    public boolean isCompleted() {
        return completedAt != null;
    }

    /**
     * 소요 시간 (초 단위) 반환
     *
     * @return 소요 시간 (초), 미완료 시 null
     */
    public Long getDurationSeconds() {
        if (durationMs == null) {
            return null;
        }
        return durationMs / 1000;
    }
}
