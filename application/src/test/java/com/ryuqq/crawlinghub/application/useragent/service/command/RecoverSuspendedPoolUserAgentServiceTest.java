package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RecoverSuspendedPoolUserAgentService 단위 테스트
 *
 * <p>SUSPENDED UserAgent 복구 서비스 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverSuspendedPoolUserAgentService 테스트")
class RecoverSuspendedPoolUserAgentServiceTest {

    @Mock private UserAgentPoolManager poolManager;

    @InjectMocks private RecoverSuspendedPoolUserAgentService service;

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 복구된 UserAgent 수 반환")
        void shouldReturnRecoveredCount() {
            // Given
            given(poolManager.recoverSuspendedUserAgents()).willReturn(2);

            // When
            int result = service.execute();

            // Then
            assertThat(result).isEqualTo(2);
            then(poolManager).should().recoverSuspendedUserAgents();
        }

        @Test
        @DisplayName("[성공] 복구 대상 없으면 0 반환")
        void shouldReturnZeroWhenNothingToRecover() {
            // Given
            given(poolManager.recoverSuspendedUserAgents()).willReturn(0);

            // When
            int result = service.execute();

            // Then
            assertThat(result).isZero();
        }
    }
}
