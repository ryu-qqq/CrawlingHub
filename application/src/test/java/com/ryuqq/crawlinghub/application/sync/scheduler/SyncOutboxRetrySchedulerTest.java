package com.ryuqq.crawlinghub.application.sync.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.common.port.out.lock.DistributedLockPort;
import com.ryuqq.crawlinghub.application.sync.dto.command.SyncRetryResult;
import com.ryuqq.crawlinghub.application.sync.port.in.command.RetrySyncUseCase;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SyncOutboxRetryScheduler 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SyncOutboxRetryScheduler 테스트")
class SyncOutboxRetrySchedulerTest {

    @Mock private RetrySyncUseCase retrySyncUseCase;
    @Mock private DistributedLockPort distributedLockPort;

    private SyncOutboxRetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SyncOutboxRetryScheduler(retrySyncUseCase, distributedLockPort);
    }

    @Nested
    @DisplayName("retry() 테스트")
    class Retry {

        @Test
        @DisplayName("[성공] 락 획득 후 재시도 실행")
        void shouldExecuteRetryWhenLockAcquired() {
            // Given
            given(
                            distributedLockPort.executeWithLock(
                                    eq("scheduler:sync-outbox-retry"),
                                    eq(0L),
                                    eq(300L),
                                    eq(TimeUnit.SECONDS),
                                    any(Runnable.class)))
                    .willAnswer(
                            invocation -> {
                                Runnable task = invocation.getArgument(4);
                                task.run();
                                return true;
                            });

            given(retrySyncUseCase.execute()).willReturn(SyncRetryResult.of(10, 8, 2, false));

            // When
            scheduler.retry();

            // Then
            verify(distributedLockPort)
                    .executeWithLock(
                            eq("scheduler:sync-outbox-retry"),
                            eq(0L),
                            eq(300L),
                            eq(TimeUnit.SECONDS),
                            any(Runnable.class));
            verify(retrySyncUseCase).execute();
        }

        @Test
        @DisplayName("[성공] 락 획득 실패 시 스킵")
        void shouldSkipWhenLockNotAcquired() {
            // Given
            given(
                            distributedLockPort.executeWithLock(
                                    eq("scheduler:sync-outbox-retry"),
                                    anyLong(),
                                    anyLong(),
                                    any(TimeUnit.class),
                                    any(Runnable.class)))
                    .willReturn(false);

            // When
            scheduler.retry();

            // Then
            verify(retrySyncUseCase, never()).execute();
        }

        @Test
        @DisplayName("[성공] 더 처리할 데이터 있으면 반복 실행")
        void shouldIterateWhenHasMore() {
            // Given
            given(
                            distributedLockPort.executeWithLock(
                                    eq("scheduler:sync-outbox-retry"),
                                    eq(0L),
                                    eq(300L),
                                    eq(TimeUnit.SECONDS),
                                    any(Runnable.class)))
                    .willAnswer(
                            invocation -> {
                                Runnable task = invocation.getArgument(4);
                                task.run();
                                return true;
                            });

            given(retrySyncUseCase.execute())
                    .willReturn(SyncRetryResult.of(100, 95, 5, true))
                    .willReturn(SyncRetryResult.of(100, 98, 2, true))
                    .willReturn(SyncRetryResult.of(50, 50, 0, false));

            // When
            scheduler.retry();

            // Then
            verify(retrySyncUseCase, times(3)).execute();
        }

        @Test
        @DisplayName("[성공] 최대 반복 횟수 제한")
        void shouldLimitMaxIterations() {
            // Given
            given(
                            distributedLockPort.executeWithLock(
                                    eq("scheduler:sync-outbox-retry"),
                                    eq(0L),
                                    eq(300L),
                                    eq(TimeUnit.SECONDS),
                                    any(Runnable.class)))
                    .willAnswer(
                            invocation -> {
                                Runnable task = invocation.getArgument(4);
                                task.run();
                                return true;
                            });

            // 계속 hasMore=true 반환
            given(retrySyncUseCase.execute()).willReturn(SyncRetryResult.of(100, 100, 0, true));

            // When
            scheduler.retry();

            // Then - MAX_ITERATIONS(10)번만 실행
            verify(retrySyncUseCase, times(10)).execute();
        }
    }
}
