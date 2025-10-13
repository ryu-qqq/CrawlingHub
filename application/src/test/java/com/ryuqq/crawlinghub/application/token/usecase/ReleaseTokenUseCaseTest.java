package com.ryuqq.crawlinghub.application.token.usecase;

import com.ryuqq.crawlinghub.application.token.port.DistributedLockPort;
import com.ryuqq.crawlinghub.application.token.port.UserAgentPoolPort;
import com.ryuqq.crawlinghub.application.token.port.UserAgentTokenPort;
import com.ryuqq.crawlinghub.domain.token.AcquiredToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * ReleaseTokenUseCase 단위 테스트
 *
 * @author crawlinghub
 */
@ExtendWith(MockitoExtension.class)
class ReleaseTokenUseCaseTest {

    @Mock
    private DistributedLockPort lockPort;

    @Mock
    private UserAgentTokenPort tokenPort;

    @Mock
    private UserAgentPoolPort poolPort;

    @InjectMocks
    private ReleaseTokenUseCase useCase;

    private AcquiredToken acquiredToken;

    @BeforeEach
    void setUp() {
        acquiredToken = new AcquiredToken(
                1L,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                "test-token-value",
                "user_agent:lock:1",
                "lock-uuid-123"
        );
    }

    @Test
    @DisplayName("토큰 해제 성공 - 성공 기록")
    void releaseTokenSuccessWithSuccessRecord() {
        // given
        when(lockPort.release(acquiredToken.lockKey(), acquiredToken.lockValue())).thenReturn(true);

        // when
        useCase.execute(acquiredToken, true);

        // then
        verify(lockPort).release(acquiredToken.lockKey(), acquiredToken.lockValue());
        verify(tokenPort).recordSuccess(acquiredToken.userAgentId());
        verify(tokenPort, never()).recordFailure(anyLong());
        verify(poolPort).returnToPool(acquiredToken.userAgentId());
    }

    @Test
    @DisplayName("토큰 해제 성공 - 실패 기록")
    void releaseTokenSuccessWithFailureRecord() {
        // given
        when(lockPort.release(acquiredToken.lockKey(), acquiredToken.lockValue())).thenReturn(true);

        // when
        useCase.execute(acquiredToken, false);

        // then
        verify(lockPort).release(acquiredToken.lockKey(), acquiredToken.lockValue());
        verify(tokenPort).recordFailure(acquiredToken.userAgentId());
        verify(tokenPort, never()).recordSuccess(anyLong());
        verify(poolPort).returnToPool(acquiredToken.userAgentId());
    }

    @Test
    @DisplayName("토큰 해제 - 락 없는 경우 (락 해제 스킵)")
    void releaseTokenWithoutLock() {
        // given
        AcquiredToken tokenWithoutLock = new AcquiredToken(
                1L,
                "Mozilla/5.0",
                "test-token-value",
                null,
                null
        );

        // when
        useCase.execute(tokenWithoutLock, true);

        // then
        verify(lockPort, never()).release(any(), any());
        verify(tokenPort).recordSuccess(tokenWithoutLock.userAgentId());
        verify(poolPort).returnToPool(tokenWithoutLock.userAgentId());
    }

    @Test
    @DisplayName("토큰 해제 - 락 해제 실패해도 통계 기록 및 Pool 반환은 수행")
    void releaseTokenWhenLockReleaseFailed() {
        // given
        when(lockPort.release(acquiredToken.lockKey(), acquiredToken.lockValue())).thenReturn(false);

        // when
        useCase.execute(acquiredToken, true);

        // then
        verify(lockPort).release(acquiredToken.lockKey(), acquiredToken.lockValue());
        verify(tokenPort).recordSuccess(acquiredToken.userAgentId());
        verify(poolPort).returnToPool(acquiredToken.userAgentId());
    }
}
