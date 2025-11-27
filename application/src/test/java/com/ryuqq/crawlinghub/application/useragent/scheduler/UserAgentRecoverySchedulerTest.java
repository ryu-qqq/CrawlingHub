package com.ryuqq.crawlinghub.application.useragent.scheduler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentRecoveryScheduler 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PoolManager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentRecoveryScheduler 테스트")
class UserAgentRecoverySchedulerTest {

    @Mock
    private UserAgentPoolManager poolManager;

    @InjectMocks
    private UserAgentRecoveryScheduler scheduler;

    @Nested
    @DisplayName("recoverSuspendedUserAgents() 테스트")
    class RecoverSuspendedUserAgents {

        @Test
        @DisplayName("[성공] SUSPENDED UserAgent 복구 호출")
        void shouldRecoverSuspendedUserAgents() {
            // Given
            int recoveredCount = 5;
            given(poolManager.recoverSuspendedUserAgents()).willReturn(recoveredCount);

            // When
            scheduler.recoverSuspendedUserAgents();

            // Then
            verify(poolManager).recoverSuspendedUserAgents();
        }

        @Test
        @DisplayName("[성공] 복구 대상이 없는 경우에도 정상 처리")
        void shouldHandleZeroRecoveryCount() {
            // Given
            given(poolManager.recoverSuspendedUserAgents()).willReturn(0);

            // When
            scheduler.recoverSuspendedUserAgents();

            // Then
            verify(poolManager).recoverSuspendedUserAgents();
        }

        @Test
        @DisplayName("[실패] 예외 발생 → 로그만 기록 (예외 전파 안함)")
        void shouldHandleExceptionGracefully() {
            // Given
            doThrow(new RuntimeException("Recovery error"))
                    .when(poolManager).recoverSuspendedUserAgents();

            // When - 예외가 전파되지 않아야 함
            scheduler.recoverSuspendedUserAgents();

            // Then
            verify(poolManager).recoverSuspendedUserAgents();
        }
    }
}
