package com.ryuqq.crawlinghub.application.token.component;

import com.ryuqq.crawlinghub.application.token.port.*;
import com.ryuqq.crawlinghub.application.token.service.TokenTransactionService;
import com.ryuqq.crawlinghub.domain.token.Token;
import com.ryuqq.crawlinghub.domain.token.exception.TokenAcquisitionException;
import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TokenAcquisitionManager Unit Test
 * <p>
 * ⭐ 테스트 전략:
 * - Port 기반 설계로 모든 외부 의존성 Mock
 * - Domain 비즈니스 로직 검증
 * - 예외 상황 처리 검증
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenAcquisitionManager 단위 테스트")
class TokenAcquisitionManagerTest {

    @Mock
    private UserAgentPoolPort poolPort;

    @Mock
    private DistributedLockPort lockPort;

    @Mock
    private RateLimiterPort rateLimiterPort;

    @Mock
    private CircuitBreakerPort circuitBreakerPort;

    @Mock
    private MustItTokenPort mustItTokenPort;

    @Mock
    private TokenTransactionService transactionService;

    @InjectMocks
    private TokenAcquisitionManager manager;

    @Test
    @DisplayName("토큰 획득 성공 - 토큰이 유효한 경우")
    void acquireToken_Success_WhenTokenValid() {
        // Given: Pool에서 UserAgent 선택
        UserAgent poolUserAgent = UserAgent.reconstitute(
            UserAgentId.of(123L),
            "temp", null, null, null, null, null, null
        );
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(poolUserAgent);

        // Given: 분산 락 획득
        when(lockPort.tryAcquire("token:lock:123", 5000L))
            .thenReturn("lock-uuid-123");

        // Given: DB에서 UserAgent 로드 (토큰 유효)
        Token validToken = Token.of(
            "valid_token_abc",
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusHours(23)
        );
        UserAgent loadedUserAgent = UserAgent.reconstitute(
            UserAgentId.of(123L),
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            validToken,
            TokenStatus.IDLE,
            80,
            null,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusHours(1)
        );
        when(transactionService.loadUserAgent(123L))
            .thenReturn(loadedUserAgent);

        // Given: Rate Limiter 통과
        when(rateLimiterPort.tryConsume(123L)).thenReturn(true);

        // When
        UserAgent result = manager.acquireToken();

        // Then: UserAgent 반환 검증
        assertThat(result).isNotNull();
        assertThat(result.getIdValue()).isEqualTo(123L);
        assertThat(result.getCurrentToken().getValue()).isEqualTo("valid_token_abc");
        assertThat(result.getRemainingRequests()).isEqualTo(79); // consumeRequest() 호출됨
        assertThat(result.getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);

        // Then: 호출 검증
        verify(poolPort).acquireLeastRecentlyUsed();
        verify(lockPort).tryAcquire("token:lock:123", 5000L);
        verify(transactionService).loadUserAgent(123L);
        verify(transactionService, never()).saveUserAgent(any()); // 토큰 유효하므로 저장 안함
        verify(rateLimiterPort).tryConsume(123L);
        verify(transactionService).recordUsage(loadedUserAgent);
        verify(lockPort).release("token:lock:123", "lock-uuid-123");
    }

    @Test
    @DisplayName("토큰 획득 성공 - 토큰 만료로 신규 발급")
    void acquireToken_Success_WhenTokenExpired() {
        // Given: Pool에서 UserAgent 선택
        UserAgent poolUserAgent = UserAgent.reconstitute(
            UserAgentId.of(456L),
            "temp", null, null, null, null, null, null
        );
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(poolUserAgent);

        // Given: 분산 락 획득
        when(lockPort.tryAcquire("token:lock:456", 5000L))
            .thenReturn("lock-uuid-456");

        // Given: DB에서 UserAgent 로드 (토큰 만료)
        Token expiredToken = Token.of(
            "expired_token_old",
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusDays(1) // 만료됨!
        );
        UserAgent loadedUserAgent = UserAgent.reconstitute(
            UserAgentId.of(456L),
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
            expiredToken,
            TokenStatus.IDLE,
            80,
            null,
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusDays(1)
        );
        when(transactionService.loadUserAgent(456L))
            .thenReturn(loadedUserAgent);

        // Given: Circuit Breaker 정상
        when(circuitBreakerPort.isOpen("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"))
            .thenReturn(false);

        // Given: 외부 API로 신규 토큰 발급
        Token newToken = Token.of(
            "new_token_xyz",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(24)
        );
        when(mustItTokenPort.issueToken("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"))
            .thenReturn(newToken);

        // Given: Rate Limiter 통과
        when(rateLimiterPort.tryConsume(456L)).thenReturn(true);

        // When
        UserAgent result = manager.acquireToken();

        // Then: 신규 토큰으로 갱신 검증
        assertThat(result).isNotNull();
        assertThat(result.getCurrentToken().getValue()).isEqualTo("new_token_xyz");
        assertThat(result.getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);
        assertThat(result.getRemainingRequests()).isEqualTo(79); // 초기화 후 1개 소비

        // Then: 호출 검증
        verify(mustItTokenPort).issueToken("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
        verify(circuitBreakerPort).recordSuccess("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
        verify(transactionService).saveUserAgent(loadedUserAgent); // 신규 토큰 저장됨!
        verify(transactionService).recordUsage(loadedUserAgent);
        verify(lockPort).release("token:lock:456", "lock-uuid-456");
    }

    @Test
    @DisplayName("토큰 획득 실패 - Pool에 UserAgent 없음")
    void acquireToken_Fail_NoAvailableUserAgent() {
        // Given: Pool에서 UserAgent 없음
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(null);

        // When & Then: 예외 발생 검증
        assertThatThrownBy(() -> manager.acquireToken())
            .isInstanceOf(TokenAcquisitionException.class)
            .hasMessageContaining("사용 가능한 User-Agent가 없습니다")
            .extracting(e -> ((TokenAcquisitionException) e).getErrorCode())
            .isEqualTo(TokenAcquisitionException.ErrorCode.NO_AVAILABLE_USER_AGENT);

        // Then: 락 획득 시도조차 하지 않음
        verify(lockPort, never()).tryAcquire(any(), anyLong());
        verify(transactionService, never()).loadUserAgent(anyLong());
    }

    @Test
    @DisplayName("토큰 획득 실패 - 분산 락 획득 실패")
    void acquireToken_Fail_LockAcquisitionFailed() {
        // Given: Pool에서 UserAgent 선택
        UserAgent poolUserAgent = UserAgent.reconstitute(
            UserAgentId.of(789L),
            "temp", null, null, null, null, null, null
        );
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(poolUserAgent);

        // Given: 분산 락 획득 실패
        when(lockPort.tryAcquire("token:lock:789", 5000L)).thenReturn(null);

        // When & Then: 예외 발생 검증
        assertThatThrownBy(() -> manager.acquireToken())
            .isInstanceOf(TokenAcquisitionException.class)
            .hasMessageContaining("분산 락 획득에 실패했습니다")
            .extracting(e -> ((TokenAcquisitionException) e).getErrorCode())
            .isEqualTo(TokenAcquisitionException.ErrorCode.LOCK_ACQUISITION_FAILED);

        // Then: DB 조회 시도조차 하지 않음
        verify(transactionService, never()).loadUserAgent(anyLong());
    }

    @Test
    @DisplayName("토큰 획득 실패 - Rate Limit 초과")
    void acquireToken_Fail_RateLimitExceeded() {
        // Given: 정상 흐름 (Pool → Lock → DB 조회)
        UserAgent poolUserAgent = UserAgent.reconstitute(
            UserAgentId.of(111L),
            "temp", null, null, null, null, null, null
        );
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(poolUserAgent);
        when(lockPort.tryAcquire("token:lock:111", 5000L)).thenReturn("lock-uuid-111");

        Token validToken = Token.of(
            "valid_token",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(24)
        );
        UserAgent loadedUserAgent = UserAgent.reconstitute(
            UserAgentId.of(111L),
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/537.36",
            validToken,
            TokenStatus.IDLE,
            80,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        when(transactionService.loadUserAgent(111L)).thenReturn(loadedUserAgent);

        // Given: Rate Limiter 실패
        when(rateLimiterPort.tryConsume(111L)).thenReturn(false);
        when(rateLimiterPort.getWaitTime(111L, 1)).thenReturn(5000L);

        // When & Then: 예외 발생 검증
        assertThatThrownBy(() -> manager.acquireToken())
            .isInstanceOf(TokenAcquisitionException.class)
            .hasMessageContaining("Rate Limit 초과")
            .hasMessageContaining("5000ms 후 재시도 가능")
            .extracting(e -> ((TokenAcquisitionException) e).getErrorCode())
            .isEqualTo(TokenAcquisitionException.ErrorCode.RATE_LIMIT_EXCEEDED);

        // Then: 사용 기록은 저장되지 않음
        verify(transactionService, never()).recordUsage(any());

        // Then: 락은 반드시 해제되어야 함
        verify(lockPort).release("token:lock:111", "lock-uuid-111");
    }

    @Test
    @DisplayName("토큰 획득 실패 - Circuit Breaker Open")
    void acquireToken_Fail_CircuitBreakerOpen() {
        // Given: Pool → Lock → DB 조회 (토큰 만료)
        UserAgent poolUserAgent = UserAgent.reconstitute(
            UserAgentId.of(222L),
            "temp", null, null, null, null, null, null
        );
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(poolUserAgent);
        when(lockPort.tryAcquire("token:lock:222", 5000L)).thenReturn("lock-uuid-222");

        Token expiredToken = Token.of(
            "expired_token",
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusDays(1)
        );
        UserAgent loadedUserAgent = UserAgent.reconstitute(
            UserAgentId.of(222L),
            "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36",
            expiredToken,
            TokenStatus.IDLE,
            80,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        when(transactionService.loadUserAgent(222L)).thenReturn(loadedUserAgent);

        // Given: Circuit Breaker OPEN!
        when(circuitBreakerPort.isOpen("Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36"))
            .thenReturn(true);

        // When & Then: 예외 발생 검증
        assertThatThrownBy(() -> manager.acquireToken())
            .isInstanceOf(TokenAcquisitionException.class)
            .hasMessageContaining("Circuit Breaker가 열려있어 토큰 발급이 불가능합니다")
            .extracting(e -> ((TokenAcquisitionException) e).getErrorCode())
            .isEqualTo(TokenAcquisitionException.ErrorCode.CIRCUIT_BREAKER_OPEN);

        // Then: 외부 API 호출하지 않음
        verify(mustItTokenPort, never()).issueToken(any());

        // Then: 락은 반드시 해제되어야 함
        verify(lockPort).release("token:lock:222", "lock-uuid-222");
    }

    @Test
    @DisplayName("토큰 획득 실패 - 외부 API 토큰 발급 실패")
    void acquireToken_Fail_ExternalApiTokenIssuanceFailed() {
        // Given: Pool → Lock → DB 조회 (토큰 만료)
        UserAgent poolUserAgent = UserAgent.reconstitute(
            UserAgentId.of(333L),
            "temp", null, null, null, null, null, null
        );
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(poolUserAgent);
        when(lockPort.tryAcquire("token:lock:333", 5000L)).thenReturn("lock-uuid-333");

        Token expiredToken = Token.of(
            "expired_token",
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusDays(1)
        );
        UserAgent loadedUserAgent = UserAgent.reconstitute(
            UserAgentId.of(333L),
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36",
            expiredToken,
            TokenStatus.IDLE,
            80,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        when(transactionService.loadUserAgent(333L)).thenReturn(loadedUserAgent);

        // Given: Circuit Breaker 정상
        when(circuitBreakerPort.isOpen("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"))
            .thenReturn(false);

        // Given: 외부 API 호출 실패
        when(mustItTokenPort.issueToken("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"))
            .thenThrow(new RuntimeException("MustIt API connection timeout"));

        // When & Then: 예외 발생 검증
        assertThatThrownBy(() -> manager.acquireToken())
            .isInstanceOf(TokenAcquisitionException.class)
            .hasMessageContaining("외부 API에서 토큰 발급에 실패했습니다")
            .extracting(e -> ((TokenAcquisitionException) e).getErrorCode())
            .isEqualTo(TokenAcquisitionException.ErrorCode.TOKEN_ISSUANCE_FAILED);

        // Then: Circuit Breaker에 실패 기록
        verify(circuitBreakerPort).recordFailure("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");

        // Then: 락은 반드시 해제되어야 함
        verify(lockPort).release("token:lock:333", "lock-uuid-333");
    }

    @Test
    @DisplayName("락 해제 실패 시에도 예외 전파하지 않음")
    void acquireToken_LockReleaseFailure_DoesNotPropagateException() {
        // Given: 정상 흐름
        UserAgent poolUserAgent = UserAgent.reconstitute(
            UserAgentId.of(444L),
            "temp", null, null, null, null, null, null
        );
        when(poolPort.acquireLeastRecentlyUsed()).thenReturn(poolUserAgent);
        when(lockPort.tryAcquire("token:lock:444", 5000L)).thenReturn("lock-uuid-444");

        Token validToken = Token.of(
            "valid_token",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(24)
        );
        UserAgent loadedUserAgent = UserAgent.reconstitute(
            UserAgentId.of(444L),
            "Mozilla/5.0 Test Agent",
            validToken,
            TokenStatus.IDLE,
            80,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        when(transactionService.loadUserAgent(444L)).thenReturn(loadedUserAgent);
        when(rateLimiterPort.tryConsume(444L)).thenReturn(true);

        // Given: 락 해제 실패
        doThrow(new RuntimeException("Redis connection lost"))
            .when(lockPort).release("token:lock:444", "lock-uuid-444");

        // When: 정상적으로 토큰 획득 (락 해제 실패는 무시)
        UserAgent result = manager.acquireToken();

        // Then: 결과는 정상
        assertThat(result).isNotNull();
        assertThat(result.getIdValue()).isEqualTo(444L);

        // Then: 락 해제는 시도했지만 실패는 무시됨
        verify(lockPort).release("token:lock:444", "lock-uuid-444");
    }
}
