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
 * RecoverUserAgentService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverUserAgentService 테스트")
class RecoverUserAgentServiceTest {

    @Mock
    private UserAgentPoolManager poolManager;

    @InjectMocks
    private RecoverUserAgentService service;

    @Nested
    @DisplayName("recoverAll() 전체 복구 테스트")
    class RecoverAll {

        @Test
        @DisplayName("[성공] 복구 가능한 UserAgent 복구 시 복구된 개수 반환")
        void shouldReturnRecoveredCount() {
            // Given
            int expectedRecoveredCount = 5;

            given(poolManager.recoverSuspendedUserAgents()).willReturn(expectedRecoveredCount);

            // When
            int result = service.recoverAll();

            // Then
            assertThat(result).isEqualTo(expectedRecoveredCount);
            then(poolManager).should().recoverSuspendedUserAgents();
        }

        @Test
        @DisplayName("[성공] 복구할 UserAgent가 없으면 0 반환")
        void shouldReturnZeroWhenNoAgentsToRecover() {
            // Given
            given(poolManager.recoverSuspendedUserAgents()).willReturn(0);

            // When
            int result = service.recoverAll();

            // Then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("[성공] 일부 UserAgent만 복구 가능한 경우")
        void shouldReturnPartialRecoveredCount() {
            // Given
            int expectedRecoveredCount = 3;

            given(poolManager.recoverSuspendedUserAgents()).willReturn(expectedRecoveredCount);

            // When
            int result = service.recoverAll();

            // Then
            assertThat(result).isEqualTo(3);
        }
    }
}
