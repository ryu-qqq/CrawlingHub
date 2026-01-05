package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ConsumeUserAgentService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeUserAgentService 테스트")
class ConsumeUserAgentServiceTest {

    @Mock private UserAgentPoolManager poolManager;

    @InjectMocks private ConsumeUserAgentService service;

    @Nested
    @DisplayName("execute() UserAgent 토큰 소비 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] READY 상태의 UserAgent 반환")
        void shouldReturnReadyUserAgent() {
            // Given
            CachedUserAgent expectedUserAgent = createReadyUserAgent();

            given(poolManager.consume()).willReturn(expectedUserAgent);

            // When
            CachedUserAgent result = service.execute();

            // Then
            assertThat(result).isEqualTo(expectedUserAgent);
            assertThat(result.status()).isEqualTo(UserAgentStatus.READY);
            then(poolManager).should().consume();
        }

        @Test
        @DisplayName("[성공] 반환된 UserAgent는 토큰을 보유")
        void shouldReturnUserAgentWithTokens() {
            // Given
            CachedUserAgent userAgent = createReadyUserAgent();

            given(poolManager.consume()).willReturn(userAgent);

            // When
            CachedUserAgent result = service.execute();

            // Then
            assertThat(result.hasTokens()).isTrue();
            assertThat(result.remainingTokens()).isEqualTo(80);
        }

        @Test
        @DisplayName("[성공] 반환된 UserAgent는 유효한 세션 보유")
        void shouldReturnUserAgentWithValidSession() {
            // Given
            CachedUserAgent userAgent = createReadyUserAgent();

            given(poolManager.consume()).willReturn(userAgent);

            // When
            CachedUserAgent result = service.execute();

            // Then
            assertThat(result.hasValidSession(Instant.now())).isTrue();
        }

        private CachedUserAgent createReadyUserAgent() {
            return new CachedUserAgent(
                    1L,
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                    "session-token-123",
                    null, // nid
                    null, // mustitUid
                    Instant.now().plusSeconds(3600),
                    80,
                    80,
                    Instant.now(),
                    Instant.now().plusSeconds(60),
                    100,
                    UserAgentStatus.READY,
                    null);
        }
    }
}
