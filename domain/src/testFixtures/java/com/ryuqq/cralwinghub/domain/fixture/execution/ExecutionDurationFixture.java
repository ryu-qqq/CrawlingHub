package com.ryuqq.cralwinghub.domain.fixture.execution;

import com.ryuqq.crawlinghub.domain.execution.vo.ExecutionDuration;
import java.time.LocalDateTime;

/**
 * ExecutionDuration Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class ExecutionDurationFixture {

    private static final LocalDateTime DEFAULT_START = LocalDateTime.of(2025, 11, 27, 12, 0, 0);
    private static final LocalDateTime DEFAULT_END = LocalDateTime.of(2025, 11, 27, 12, 0, 5);
    private static final Long DEFAULT_DURATION_MS = 5000L;

    /**
     * 실행 시작 상태 생성
     *
     * @return ExecutionDuration (running)
     */
    public static ExecutionDuration aRunningDuration() {
        return ExecutionDuration.startAt(DEFAULT_START);
    }

    /**
     * 실행 완료 상태 생성 (5초 소요)
     *
     * @return ExecutionDuration (completed)
     */
    public static ExecutionDuration aCompletedDuration() {
        return ExecutionDuration.reconstitute(DEFAULT_START, DEFAULT_END, DEFAULT_DURATION_MS);
    }

    /**
     * 특정 시작 시간으로 실행 시작 상태 생성
     *
     * @param startedAt 시작 시간
     * @return ExecutionDuration (running)
     */
    public static ExecutionDuration aRunningDuration(LocalDateTime startedAt) {
        return ExecutionDuration.startAt(startedAt);
    }

    /**
     * 특정 소요 시간으로 완료 상태 생성
     *
     * @param durationMs 소요 시간 (밀리초)
     * @return ExecutionDuration (completed)
     */
    public static ExecutionDuration aCompletedDuration(Long durationMs) {
        return ExecutionDuration.reconstitute(
                DEFAULT_START,
                DEFAULT_START.plusNanos(durationMs * 1_000_000),
                durationMs);
    }

    private ExecutionDurationFixture() {
        // Utility class
    }
}
