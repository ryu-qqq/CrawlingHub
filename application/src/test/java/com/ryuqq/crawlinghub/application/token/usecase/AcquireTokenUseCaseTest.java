package com.ryuqq.crawlinghub.application.token.usecase;

import com.ryuqq.crawlinghub.application.token.port.*;
import com.ryuqq.crawlinghub.domain.token.AcquiredToken;
import com.ryuqq.crawlinghub.domain.token.TokenAcquisitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * AcquireTokenUseCase 단위 테스트
 *
 * @author crawlinghub
 */
@ExtendWith(MockitoExtension.class)
class AcquireTokenUseCaseTest {

    @Mock
    private UserAgentPoolPort poolPort;

    @Mock
    private DistributedLockPort lockPort;

    @Mock
    private CircuitBreakerPort circuitBreakerPort;

    @Mock
    private UserAgentTokenPort tokenPort;

    @Mock
    private RateLimiterPort rateLimiterPort;

    @InjectMocks
    private AcquireTokenUseCase useCase;

    private UserAgentInfo validTokenInfo;

    @BeforeEach
    void setUp() {
        validTokenInfo = new UserAgentInfo(
                1L,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                100L,
                "test-token-value",
                LocalDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("토큰 획득 성공 - 전체 플로우")
    void acquireTokenSuccess() {
        // given
        Long userAgentId = 1L;
        String lockValue = "lock-uuid-123";

        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(userAgentId);
        when(lockPort.tryAcquire(any())).thenReturn(lockValue);
        when(circuitBreakerPort.isOpen(userAgentId)).thenReturn(false);
        when(tokenPort.findActiveToken(userAgentId)).thenReturn(validTokenInfo);
        when(rateLimiterPort.tryConsume(userAgentId)).thenReturn(true);

        // when
        AcquiredToken result = useCase.execute();

        // then
        assertThat(result).isNotNull();
        assertThat(result.userAgentId()).isEqualTo(userAgentId);
        assertThat(result.userAgent()).isEqualTo(validTokenInfo.userAgent());
        assertThat(result.tokenValue()).isEqualTo(validTokenInfo.tokenValue());
        assertThat(result.lockValue()).isEqualTo(lockValue);
        assertThat(result.isAcquired()).isTrue();
        assertThat(result.hasLock()).isTrue();

        verify(poolPort).acquireLeastRecentlyUsed();
        verify(lockPort).tryAcquire(any());
        verify(circuitBreakerPort).isOpen(userAgentId);
        verify(tokenPort).findActiveToken(userAgentId);
        verify(tokenPort).recordUsage(userAgentId);
        verify(rateLimiterPort).tryConsume(userAgentId);
    }

    @Test
    @DisplayName("토큰 획득 실패 - Pool 고갈")
    void acquireTokenFailureWhenPoolExhausted() {
        // given
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(TokenAcquisitionException.class)
                .hasMessageContaining("사용 가능한 User-Agent가 없습니다");

        verify(poolPort).acquireLeastRecentlyUsed();
        verify(lockPort, never()).tryAcquire(any());
    }

    @Test
    @DisplayName("토큰 획득 실패 - 분산 락 획득 실패")
    void acquireTokenFailureWhenLockFailed() {
        // given
        Long userAgentId = 1L;
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(userAgentId);
        when(lockPort.tryAcquire(any())).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(TokenAcquisitionException.class)
                .hasMessageContaining("분산 락 획득에 실패했습니다");

        verify(poolPort).acquireLeastRecentlyUsed();
        verify(lockPort).tryAcquire(any());
        verify(poolPort).returnToPool(userAgentId);
    }

    @Test
    @DisplayName("토큰 획득 실패 - Circuit Breaker OPEN")
    void acquireTokenFailureWhenCircuitBreakerOpen() {
        // given
        Long userAgentId = 1L;
        String lockValue = "lock-uuid-123";

        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(userAgentId);
        when(lockPort.tryAcquire(any())).thenReturn(lockValue);
        when(circuitBreakerPort.isOpen(userAgentId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(TokenAcquisitionException.class)
                .hasMessageContaining("Circuit Breaker가 OPEN 상태입니다");

        verify(circuitBreakerPort).isOpen(userAgentId);
        verify(lockPort).release(any(), eq(lockValue));
        verify(poolPort).returnToPool(userAgentId);
    }

    @Test
    @DisplayName("토큰 획득 실패 - Token 만료")
    void acquireTokenFailureWhenTokenExpired() {
        // given
        Long userAgentId = 1L;
        String lockValue = "lock-uuid-123";
        UserAgentInfo expiredToken = new UserAgentInfo(
                1L,
                "Mozilla/5.0",
                100L,
                "expired-token",
                LocalDateTime.now().minusHours(1) // 만료된 토큰
        );

        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(userAgentId);
        when(lockPort.tryAcquire(any())).thenReturn(lockValue);
        when(circuitBreakerPort.isOpen(userAgentId)).thenReturn(false);
        when(tokenPort.findActiveToken(userAgentId)).thenReturn(expiredToken);

        // when & then
        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(TokenAcquisitionException.class)
                .hasMessageContaining("토큰이 만료되었습니다");

        verify(lockPort).release(any(), eq(lockValue));
        verify(poolPort).returnToPool(userAgentId);
    }

    @Test
    @DisplayName("토큰 획득 실패 - Token 없음")
    void acquireTokenFailureWhenTokenNotFound() {
        // given
        Long userAgentId = 1L;
        String lockValue = "lock-uuid-123";

        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(userAgentId);
        when(lockPort.tryAcquire(any())).thenReturn(lockValue);
        when(circuitBreakerPort.isOpen(userAgentId)).thenReturn(false);
        when(tokenPort.findActiveToken(userAgentId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(TokenAcquisitionException.class)
                .hasMessageContaining("유효하지 않은 User-Agent입니다");

        verify(lockPort).release(any(), eq(lockValue));
        verify(poolPort).returnToPool(userAgentId);
    }

    @Test
    @DisplayName("토큰 획득 실패 - Rate Limit 초과")
    void acquireTokenFailureWhenRateLimitExceeded() {
        // given
        Long userAgentId = 1L;
        String lockValue = "lock-uuid-123";

        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(userAgentId);
        when(lockPort.tryAcquire(any())).thenReturn(lockValue);
        when(circuitBreakerPort.isOpen(userAgentId)).thenReturn(false);
        when(tokenPort.findActiveToken(userAgentId)).thenReturn(validTokenInfo);
        when(rateLimiterPort.tryConsume(userAgentId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(TokenAcquisitionException.class)
                .hasMessageContaining("Rate Limit을 초과했습니다");

        verify(lockPort).release(any(), eq(lockValue));
        verify(poolPort).returnToPool(userAgentId);
    }
}
