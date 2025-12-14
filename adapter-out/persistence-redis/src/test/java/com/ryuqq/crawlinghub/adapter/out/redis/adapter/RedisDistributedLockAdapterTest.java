package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.out.redis.config.RedisProperties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * RedisDistributedLockAdapter 단위 테스트
 *
 * <p>분산 락 Adapter의 비즈니스 로직을 검증합니다.
 *
 * <p>테스트 범위:
 *
 * <ul>
 *   <li>tryLock - 락 획득 시도
 *   <li>unlock - 락 해제
 *   <li>executeWithLock - 락 내 작업 실행
 *   <li>isLocked/isHeldByCurrentThread - 락 상태 확인
 *   <li>키 접두사 추가 동작
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("lock")
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisDistributedLockAdapter 단위 테스트")
class RedisDistributedLockAdapterTest {

    @Mock private RedissonClient redissonClient;

    @Mock private RLock rLock;

    private RedisProperties redisProperties;
    private RedisDistributedLockAdapter adapter;

    private static final String KEY_PREFIX = "crawlinghub:lock:";
    private static final String LOCK_KEY = "test-lock";
    private static final String FULL_KEY = KEY_PREFIX + LOCK_KEY;

    @BeforeEach
    void setUp() {
        redisProperties = new RedisProperties();
        redisProperties.setKeyPrefix(KEY_PREFIX);
        adapter = new RedisDistributedLockAdapter(redissonClient, redisProperties);
    }

    @Nested
    @DisplayName("tryLock - 락 획득 시도")
    class TryLockTests {

        @Test
        @DisplayName("성공 - 락 획득 성공 시 true 반환")
        void shouldReturnTrueWhenLockAcquired() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(5L, 30L, TimeUnit.SECONDS)).willReturn(true);

            // When
            boolean result = adapter.tryLock(LOCK_KEY, 5L, 30L, TimeUnit.SECONDS);

            // Then
            assertThat(result).isTrue();
            verify(rLock).tryLock(5L, 30L, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("실패 - 락 획득 실패 시 false 반환")
        void shouldReturnFalseWhenLockNotAcquired() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(5L, 30L, TimeUnit.SECONDS)).willReturn(false);

            // When
            boolean result = adapter.tryLock(LOCK_KEY, 5L, 30L, TimeUnit.SECONDS);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - InterruptedException 발생 시 false 반환")
        void shouldReturnFalseWhenInterrupted() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
                    .willThrow(new InterruptedException("Test interrupt"));

            // When
            boolean result = adapter.tryLock(LOCK_KEY, 5L, 30L, TimeUnit.SECONDS);

            // Then
            assertThat(result).isFalse();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            Thread.interrupted();
        }
    }

    @Nested
    @DisplayName("unlock - 락 해제")
    class UnlockTests {

        @Test
        @DisplayName("성공 - 현재 스레드가 락 보유 시 해제")
        void shouldUnlockWhenHeldByCurrentThread() {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.isHeldByCurrentThread()).willReturn(true);

            // When
            adapter.unlock(LOCK_KEY);

            // Then
            verify(rLock).unlock();
        }

        @Test
        @DisplayName("경고 - 현재 스레드가 락 미보유 시 해제 미시도")
        void shouldNotUnlockWhenNotHeldByCurrentThread() {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.isHeldByCurrentThread()).willReturn(false);

            // When
            adapter.unlock(LOCK_KEY);

            // Then
            verify(rLock, never()).unlock();
        }

        @Test
        @DisplayName("경고 - IllegalMonitorStateException 발생 시 예외 무시")
        void shouldHandleIllegalMonitorStateException() {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.isHeldByCurrentThread())
                    .willThrow(new IllegalMonitorStateException("Already released"));

            // When - Then: 예외가 전파되지 않아야 함
            adapter.unlock(LOCK_KEY);
        }
    }

    @Nested
    @DisplayName("executeWithLock (Supplier) - 락 내 작업 실행 (결과 반환)")
    class ExecuteWithLockSupplierTests {

        @Test
        @DisplayName("성공 - 락 획득 후 작업 실행 및 결과 반환")
        void shouldExecuteActionAndReturnResult() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(true);
            given(rLock.isHeldByCurrentThread()).willReturn(true);

            Supplier<String> action = () -> "result";

            // When
            String result = adapter.executeWithLock(LOCK_KEY, 5L, 30L, TimeUnit.SECONDS, action);

            // Then
            assertThat(result).isEqualTo("result");
            verify(rLock).unlock();
        }

        @Test
        @DisplayName("실패 - 락 획득 실패 시 null 반환")
        void shouldReturnNullWhenLockNotAcquired() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(false);

            AtomicBoolean actionExecuted = new AtomicBoolean(false);
            Supplier<String> action =
                    () -> {
                        actionExecuted.set(true);
                        return "result";
                    };

            // When
            String result = adapter.executeWithLock(LOCK_KEY, 5L, 30L, TimeUnit.SECONDS, action);

            // Then
            assertThat(result).isNull();
            assertThat(actionExecuted.get()).isFalse();
        }

        @Test
        @DisplayName("실패 - 작업 중 RuntimeException 발생 시 예외 전파 및 락 해제")
        void shouldPropagateExceptionAndUnlockOnFailure() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(true);
            given(rLock.isHeldByCurrentThread()).willReturn(true);

            Supplier<String> action =
                    () -> {
                        throw new RuntimeException("Test exception");
                    };

            // When - Then
            assertThatThrownBy(
                            () ->
                                    adapter.executeWithLock(
                                            LOCK_KEY, 5L, 30L, TimeUnit.SECONDS, action))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test exception");

            verify(rLock).unlock();
        }

        @Test
        @DisplayName("실패 - InterruptedException 발생 시 null 반환")
        void shouldReturnNullWhenInterruptedDuringExecution() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
                    .willThrow(new InterruptedException("Test interrupt"));

            // When
            String result =
                    adapter.executeWithLock(LOCK_KEY, 5L, 30L, TimeUnit.SECONDS, () -> "result");

            // Then
            assertThat(result).isNull();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            Thread.interrupted();
        }
    }

    @Nested
    @DisplayName("executeWithLock (Runnable) - 락 내 작업 실행 (void)")
    class ExecuteWithLockRunnableTests {

        @Test
        @DisplayName("성공 - 락 획득 후 작업 실행")
        void shouldExecuteActionSuccessfully() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(true);
            given(rLock.isHeldByCurrentThread()).willReturn(true);

            AtomicBoolean executed = new AtomicBoolean(false);
            Runnable action = () -> executed.set(true);

            // When
            boolean result = adapter.executeWithLock(LOCK_KEY, 5L, 30L, TimeUnit.SECONDS, action);

            // Then
            assertThat(result).isTrue();
            assertThat(executed.get()).isTrue();
            verify(rLock).unlock();
        }

        @Test
        @DisplayName("실패 - 락 획득 실패 시 false 반환")
        void shouldReturnFalseWhenLockNotAcquired() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(false);

            AtomicBoolean executed = new AtomicBoolean(false);
            Runnable action = () -> executed.set(true);

            // When
            boolean result = adapter.executeWithLock(LOCK_KEY, 5L, 30L, TimeUnit.SECONDS, action);

            // Then
            assertThat(result).isFalse();
            assertThat(executed.get()).isFalse();
        }

        @Test
        @DisplayName("실패 - 작업 중 RuntimeException 발생 시 예외 전파")
        void shouldPropagateExceptionOnFailure() throws InterruptedException {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(true);
            given(rLock.isHeldByCurrentThread()).willReturn(true);

            Runnable action =
                    () -> {
                        throw new RuntimeException("Test exception");
                    };

            // When - Then
            assertThatThrownBy(
                            () ->
                                    adapter.executeWithLock(
                                            LOCK_KEY, 5L, 30L, TimeUnit.SECONDS, action))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test exception");

            verify(rLock).unlock();
        }
    }

    @Nested
    @DisplayName("isLocked - 락 상태 확인")
    class IsLockedTests {

        @Test
        @DisplayName("성공 - 락이 걸려있으면 true 반환")
        void shouldReturnTrueWhenLocked() {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.isLocked()).willReturn(true);

            // When
            boolean result = adapter.isLocked(LOCK_KEY);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 락이 없으면 false 반환")
        void shouldReturnFalseWhenNotLocked() {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.isLocked()).willReturn(false);

            // When
            boolean result = adapter.isLocked(LOCK_KEY);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isHeldByCurrentThread - 현재 스레드 락 보유 확인")
    class IsHeldByCurrentThreadTests {

        @Test
        @DisplayName("성공 - 현재 스레드가 락 보유 시 true 반환")
        void shouldReturnTrueWhenHeldByCurrentThread() {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.isHeldByCurrentThread()).willReturn(true);

            // When
            boolean result = adapter.isHeldByCurrentThread(LOCK_KEY);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 현재 스레드가 락 미보유 시 false 반환")
        void shouldReturnFalseWhenNotHeldByCurrentThread() {
            // Given
            given(redissonClient.getLock(FULL_KEY)).willReturn(rLock);
            given(rLock.isHeldByCurrentThread()).willReturn(false);

            // When
            boolean result = adapter.isHeldByCurrentThread(LOCK_KEY);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("키 접두사 처리")
    class KeyPrefixTests {

        @Test
        @DisplayName("성공 - 설정된 접두사가 키에 추가됨")
        void shouldAddConfiguredPrefixToKey() throws InterruptedException {
            // Given
            String customPrefix = "custom:prefix:";
            redisProperties.setKeyPrefix(customPrefix);
            adapter = new RedisDistributedLockAdapter(redissonClient, redisProperties);

            String expectedFullKey = customPrefix + LOCK_KEY;
            given(redissonClient.getLock(expectedFullKey)).willReturn(rLock);
            given(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(true);

            // When
            adapter.tryLock(LOCK_KEY, 5L, 30L, TimeUnit.SECONDS);

            // Then
            verify(redissonClient).getLock(expectedFullKey);
        }
    }
}
