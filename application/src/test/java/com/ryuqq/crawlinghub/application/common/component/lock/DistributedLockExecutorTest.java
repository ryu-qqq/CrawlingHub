package com.ryuqq.crawlinghub.application.common.component.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.common.port.out.lock.DistributedLockPort;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DistributedLockExecutor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DistributedLockExecutor 테스트")
class DistributedLockExecutorTest {

    @Mock private DistributedLockPort distributedLockPort;

    private DistributedLockExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new DistributedLockExecutor(distributedLockPort);
    }

    @Nested
    @DisplayName("tryExecuteWithLock(LockType, Object, Supplier) 테스트")
    class TryExecuteWithLockSupplier {

        @Test
        @DisplayName("[성공] 락 획득 후 작업 실행")
        void shouldExecuteActionWhenLockAcquired() {
            // Given
            Long schedulerId = 123L;
            String expectedResult = "success";

            given(
                            distributedLockPort.executeWithLock(
                                    eq("trigger:123"),
                                    eq(0L),
                                    eq(60000L),
                                    eq(TimeUnit.MILLISECONDS),
                                    any(Supplier.class)))
                    .willReturn(expectedResult);

            // When
            Optional<String> result =
                    executor.tryExecuteWithLock(
                            LockType.CRAWL_TRIGGER, schedulerId, () -> expectedResult);

            // Then
            assertThat(result).isPresent().contains(expectedResult);
        }

        @Test
        @DisplayName("[성공] 락 획득 실패 시 empty 반환")
        void shouldReturnEmptyWhenLockNotAcquired() {
            // Given
            Long schedulerId = 123L;

            given(
                            distributedLockPort.executeWithLock(
                                    eq("trigger:123"),
                                    anyLong(),
                                    anyLong(),
                                    any(TimeUnit.class),
                                    any(Supplier.class)))
                    .willReturn(null);

            // When
            Optional<String> result =
                    executor.tryExecuteWithLock(LockType.CRAWL_TRIGGER, schedulerId, () -> "value");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("tryExecuteWithLock(LockType, Object, Runnable) 테스트")
    class TryExecuteWithLockRunnable {

        @Test
        @DisplayName("[성공] 락 획득 후 작업 실행")
        void shouldReturnTrueWhenLockAcquired() {
            // Given
            Long taskId = 456L;

            given(
                            distributedLockPort.executeWithLock(
                                    eq("task:456"),
                                    eq(0L),
                                    eq(60000L),
                                    eq(TimeUnit.MILLISECONDS),
                                    any(Runnable.class)))
                    .willReturn(true);

            // When
            boolean result = executor.tryExecuteWithLock(LockType.CRAWL_TASK, taskId, () -> {});

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[성공] 락 획득 실패 시 false 반환")
        void shouldReturnFalseWhenLockNotAcquired() {
            // Given
            Long taskId = 456L;

            given(
                            distributedLockPort.executeWithLock(
                                    eq("task:456"),
                                    anyLong(),
                                    anyLong(),
                                    any(TimeUnit.class),
                                    any(Runnable.class)))
                    .willReturn(false);

            // When
            boolean result = executor.tryExecuteWithLock(LockType.CRAWL_TASK, taskId, () -> {});

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("tryExecuteWithLock(커스텀 타임아웃) 테스트")
    class TryExecuteWithCustomTimeout {

        @Test
        @DisplayName("[성공] 커스텀 타임아웃으로 락 획득")
        void shouldUseCustomTimeout() {
            // Given
            Long schedulerId = 123L;
            long waitTime = 1000L;
            long leaseTime = 30000L;
            String expectedResult = "custom-result";

            given(
                            distributedLockPort.executeWithLock(
                                    eq("trigger:123"),
                                    eq(waitTime),
                                    eq(leaseTime),
                                    eq(TimeUnit.MILLISECONDS),
                                    any(Supplier.class)))
                    .willReturn(expectedResult);

            // When
            Optional<String> result =
                    executor.tryExecuteWithLock(
                            LockType.CRAWL_TRIGGER,
                            schedulerId,
                            waitTime,
                            leaseTime,
                            () -> expectedResult);

            // Then
            assertThat(result).isPresent().contains(expectedResult);
            verify(distributedLockPort)
                    .executeWithLock(
                            eq("trigger:123"),
                            eq(waitTime),
                            eq(leaseTime),
                            eq(TimeUnit.MILLISECONDS),
                            any(Supplier.class));
        }
    }

    @Nested
    @DisplayName("isLocked() 테스트")
    class IsLocked {

        @Test
        @DisplayName("[성공] 락 보유 중이면 true 반환")
        void shouldReturnTrueWhenLocked() {
            // Given
            Long taskId = 789L;
            given(distributedLockPort.isLocked("task:789")).willReturn(true);

            // When
            boolean result = executor.isLocked(LockType.CRAWL_TASK, taskId);

            // Then
            assertThat(result).isTrue();
            verify(distributedLockPort).isLocked("task:789");
        }

        @Test
        @DisplayName("[성공] 락 미보유 시 false 반환")
        void shouldReturnFalseWhenNotLocked() {
            // Given
            Long taskId = 789L;
            given(distributedLockPort.isLocked("task:789")).willReturn(false);

            // When
            boolean result = executor.isLocked(LockType.CRAWL_TASK, taskId);

            // Then
            assertThat(result).isFalse();
        }
    }
}
