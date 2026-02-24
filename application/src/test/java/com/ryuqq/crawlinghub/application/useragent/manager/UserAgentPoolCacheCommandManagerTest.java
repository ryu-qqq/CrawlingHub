package com.ryuqq.crawlinghub.application.useragent.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPoolCacheCommandPort;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentPoolCacheCommandManager 단위 테스트
 *
 * <p>Redis Pool 캐시 쓰기 Manager 위임 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolCacheCommandManager 테스트")
class UserAgentPoolCacheCommandManagerTest {

    @Mock private UserAgentPoolCacheCommandPort commandPort;

    @InjectMocks private UserAgentPoolCacheCommandManager manager;

    @Nested
    @DisplayName("consumeToken() 테스트")
    class ConsumeToken {

        @Test
        @DisplayName("[성공] 토큰 소비 시 CachedUserAgent 반환")
        void shouldReturnCachedUserAgentWhenTokenConsumed() {
            // Given
            CachedUserAgent cached = createCachedUserAgent(1L);
            given(commandPort.consumeToken()).willReturn(Optional.of(cached));

            // When
            Optional<CachedUserAgent> result = manager.consumeToken();

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(cached);
            then(commandPort).should().consumeToken();
        }

        @Test
        @DisplayName("[성공] 사용 가능한 토큰 없으면 빈 Optional 반환")
        void shouldReturnEmptyWhenNoTokenAvailable() {
            // Given
            given(commandPort.consumeToken()).willReturn(Optional.empty());

            // When
            Optional<CachedUserAgent> result = manager.consumeToken();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateSession() 테스트")
    class UpdateSession {

        @Test
        @DisplayName("[성공] 세션 업데이트 위임")
        void shouldDelegateSessionUpdate() {
            // Given
            UserAgentId id = UserAgentId.of(1L);
            Instant expiresAt = Instant.now().plusSeconds(3600);
            willDoNothing().given(commandPort).updateSession(id, "token", "nid", "uid", expiresAt);

            // When
            manager.updateSession(id, "token", "nid", "uid", expiresAt);

            // Then
            then(commandPort).should().updateSession(id, "token", "nid", "uid", expiresAt);
        }
    }

    @Nested
    @DisplayName("isPoolInitialized() 테스트")
    class IsPoolInitialized {

        @Test
        @DisplayName("[성공] Pool 초기화 여부 확인")
        void shouldReturnPoolInitializationStatus() {
            // Given
            given(commandPort.isPoolInitialized()).willReturn(true);

            // When
            boolean result = manager.isPoolInitialized();

            // Then
            assertThat(result).isTrue();
            then(commandPort).should().isPoolInitialized();
        }
    }

    @Nested
    @DisplayName("warmUp() 테스트")
    class WarmUp {

        @Test
        @DisplayName("[성공] WarmUp 완료 후 건수 반환")
        void shouldReturnWarmUpCount() {
            // Given
            List<CachedUserAgent> agents = List.of(createCachedUserAgent(1L));
            given(commandPort.warmUp(agents)).willReturn(1);

            // When
            int result = manager.warmUp(agents);

            // Then
            assertThat(result).isEqualTo(1);
            then(commandPort).should().warmUp(agents);
        }
    }

    @Nested
    @DisplayName("detectLeakedAgents() 테스트")
    class DetectLeakedAgents {

        @Test
        @DisplayName("[성공] 누수된 UserAgent ID 목록 반환")
        void shouldReturnLeakedAgentIds() {
            // Given
            long leakThresholdMillis = 30000L;
            given(commandPort.detectLeakedAgents(leakThresholdMillis)).willReturn(List.of(1L, 2L));

            // When
            List<Long> result = manager.detectLeakedAgents(leakThresholdMillis);

            // Then
            assertThat(result).containsExactly(1L, 2L);
            then(commandPort).should().detectLeakedAgents(leakThresholdMillis);
        }
    }

    @Nested
    @DisplayName("expireSession() 테스트")
    class ExpireSession {

        @Test
        @DisplayName("[성공] 세션 만료 위임")
        void shouldDelegateExpireSession() {
            // Given
            UserAgentId id = UserAgentId.of(1L);

            // When
            manager.expireSession(id);

            // Then
            then(commandPort).should().expireSession(id);
        }
    }

    @Nested
    @DisplayName("removeFromPool() 테스트")
    class RemoveFromPool {

        @Test
        @DisplayName("[성공] Pool에서 제거 위임")
        void shouldDelegateRemoveFromPool() {
            // Given
            UserAgentId id = UserAgentId.of(2L);

            // When
            manager.removeFromPool(id);

            // Then
            then(commandPort).should().removeFromPool(id);
        }
    }

    @Nested
    @DisplayName("suspendForRateLimit() 테스트")
    class SuspendForRateLimit {

        @Test
        @DisplayName("[성공] Rate Limit SUSPENDED 처리 위임")
        void shouldDelegateSuspendForRateLimit() {
            // Given
            UserAgentId id = UserAgentId.of(3L);

            // When
            manager.suspendForRateLimit(id);

            // Then
            then(commandPort).should().suspendForRateLimit(id);
        }
    }

    @Nested
    @DisplayName("restoreToPool() 테스트")
    class RestoreToPool {

        @Test
        @DisplayName("[성공] Pool 복구 위임")
        void shouldDelegateRestoreToPool() {
            // Given
            UserAgentId id = UserAgentId.of(4L);
            String userAgentValue = "Mozilla/5.0 (Test)";

            // When
            manager.restoreToPool(id, userAgentValue);

            // Then
            then(commandPort).should().restoreToPool(id, userAgentValue);
        }
    }

    @Nested
    @DisplayName("borrow() 테스트")
    class Borrow {

        @Test
        @DisplayName("[성공] IDLE -> BORROWED 전환 및 CachedUserAgent 반환")
        void shouldReturnCachedUserAgentOnBorrow() {
            // Given
            CachedUserAgent cached = createCachedUserAgent(5L);
            given(commandPort.borrow()).willReturn(Optional.of(cached));

            // When
            Optional<CachedUserAgent> result = manager.borrow();

            // Then
            assertThat(result).isPresent().contains(cached);
            then(commandPort).should().borrow();
        }

        @Test
        @DisplayName("[성공] IDLE UserAgent 없으면 empty 반환")
        void shouldReturnEmptyWhenNoIdleAgent() {
            // Given
            given(commandPort.borrow()).willReturn(Optional.empty());

            // When
            Optional<CachedUserAgent> result = manager.borrow();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("returnAgent() 테스트")
    class ReturnAgent {

        @Test
        @DisplayName("[성공] returnAgent 위임 및 결과 반환")
        void shouldDelegateReturnAgent() {
            // Given
            given(commandPort.returnAgent(1L, true, 200, 5, null, 0)).willReturn(0);

            // When
            int result = manager.returnAgent(1L, true, 200, 5, null, 0);

            // Then
            assertThat(result).isZero();
            then(commandPort).should().returnAgent(1L, true, 200, 5, null, 0);
        }

        @Test
        @DisplayName("[성공] SUSPENDED 결과(2) 반환")
        void shouldReturnTwoWhenSuspended() {
            // Given
            given(commandPort.returnAgent(1L, false, 500, -10, null, 0)).willReturn(2);

            // When
            int result = manager.returnAgent(1L, false, 500, -10, null, 0);

            // Then
            assertThat(result).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("recoverExpiredCooldowns() 테스트")
    class RecoverExpiredCooldowns {

        @Test
        @DisplayName("[성공] 만료된 Cooldown 복구 및 건수 반환")
        void shouldReturnRecoveredCount() {
            // Given
            given(commandPort.recoverExpiredCooldowns()).willReturn(3);

            // When
            int result = manager.recoverExpiredCooldowns();

            // Then
            assertThat(result).isEqualTo(3);
            then(commandPort).should().recoverExpiredCooldowns();
        }
    }

    @Nested
    @DisplayName("tryAcquireWarmUpLock() 테스트")
    class TryAcquireWarmUpLock {

        @Test
        @DisplayName("[성공] WarmUp Lock 획득 성공")
        void shouldReturnTrueWhenLockAcquired() {
            // Given
            given(commandPort.tryAcquireWarmUpLock()).willReturn(true);

            // When
            boolean result = manager.tryAcquireWarmUpLock();

            // Then
            assertThat(result).isTrue();
            then(commandPort).should().tryAcquireWarmUpLock();
        }

        @Test
        @DisplayName("[성공] WarmUp Lock 이미 획득됨 -> false 반환")
        void shouldReturnFalseWhenLockAlreadyHeld() {
            // Given
            given(commandPort.tryAcquireWarmUpLock()).willReturn(false);

            // When
            boolean result = manager.tryAcquireWarmUpLock();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("markPoolInitialized() 테스트")
    class MarkPoolInitialized {

        @Test
        @DisplayName("[성공] Pool 초기화 완료 마킹 위임")
        void shouldDelegateMarkPoolInitialized() {
            // When
            manager.markPoolInitialized();

            // Then
            then(commandPort).should().markPoolInitialized();
        }
    }

    private CachedUserAgent createCachedUserAgent(long id) {
        return new CachedUserAgent(
                id,
                "Mozilla/5.0",
                null,
                null,
                null,
                null,
                80,
                80,
                null,
                null,
                90,
                UserAgentStatus.IDLE,
                null,
                null,
                null,
                0);
    }
}
