package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RecoverCooldownUserAgentService 단위 테스트
 *
 * <p>COOLDOWN 만료 UserAgent 복구 서비스 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverCooldownUserAgentService 테스트")
class RecoverCooldownUserAgentServiceTest {

    @Mock private UserAgentPoolCacheCommandManager cacheCommandManager;

    @InjectMocks private RecoverCooldownUserAgentService service;

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 복구된 UserAgent 수 반환")
        void shouldReturnRecoveredCount() {
            // Given
            given(cacheCommandManager.recoverExpiredCooldowns()).willReturn(3);

            // When
            int result = service.execute();

            // Then
            assertThat(result).isEqualTo(3);
            then(cacheCommandManager).should().recoverExpiredCooldowns();
        }

        @Test
        @DisplayName("[성공] 복구할 UserAgent 없으면 0 반환")
        void shouldReturnZeroWhenNothingToRecover() {
            // Given
            given(cacheCommandManager.recoverExpiredCooldowns()).willReturn(0);

            // When
            int result = service.execute();

            // Then
            assertThat(result).isZero();
        }
    }
}
