package com.ryuqq.crawlinghub.application.useragent.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.SessionTokenPort;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SessionTokenManager 단위 테스트
 *
 * <p>단일 SessionTokenPort 위임 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionTokenManager 테스트")
class SessionTokenManagerTest {

    @Mock private SessionTokenPort sessionTokenPort;

    @InjectMocks private SessionTokenManager manager;

    @Nested
    @DisplayName("issueSessionToken() 테스트")
    class IssueSessionToken {

        @Test
        @DisplayName("[성공] 세션 토큰 발급 성공 -> SessionToken 반환")
        void shouldReturnSessionTokenWhenIssuedSuccessfully() {
            // Given
            String userAgentValue = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
            SessionToken expectedToken =
                    new SessionToken("test-session-token-abc123", null, null, null);
            given(sessionTokenPort.issueSessionToken(userAgentValue))
                    .willReturn(Optional.of(expectedToken));

            // When
            Optional<SessionToken> result = manager.issueSessionToken(userAgentValue);

            // Then
            assertThat(result).isPresent().contains(expectedToken);
            then(sessionTokenPort).should().issueSessionToken(userAgentValue);
        }

        @Test
        @DisplayName("[실패] 세션 토큰 발급 실패 -> empty Optional 반환")
        void shouldReturnEmptyWhenTokenIssuanceFails() {
            // Given
            String userAgentValue = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0)";
            given(sessionTokenPort.issueSessionToken(userAgentValue)).willReturn(Optional.empty());

            // When
            Optional<SessionToken> result = manager.issueSessionToken(userAgentValue);

            // Then
            assertThat(result).isEmpty();
            then(sessionTokenPort).should().issueSessionToken(userAgentValue);
        }

        @Test
        @DisplayName("[성공] 빈 User-Agent 문자열로 발급 시도")
        void shouldDelegateEvenWithBlankUserAgent() {
            // Given
            String blankUserAgent = "";
            given(sessionTokenPort.issueSessionToken(blankUserAgent)).willReturn(Optional.empty());

            // When
            Optional<SessionToken> result = manager.issueSessionToken(blankUserAgent);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
