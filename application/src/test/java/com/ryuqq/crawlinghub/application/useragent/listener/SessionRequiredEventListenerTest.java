package com.ryuqq.crawlinghub.application.useragent.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import com.ryuqq.crawlinghub.application.useragent.port.out.cache.UserAgentPoolCachePort;
import com.ryuqq.crawlinghub.application.useragent.port.out.session.SessionTokenPort;
import com.ryuqq.crawlinghub.domain.useragent.event.SessionRequiredEvent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SessionRequiredEventListener 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionRequiredEventListener 테스트")
class SessionRequiredEventListenerTest {

    @Mock private SessionTokenPort sessionTokenPort;

    @Mock private UserAgentPoolCachePort cachePort;

    @InjectMocks private SessionRequiredEventListener listener;

    @Nested
    @DisplayName("handleSessionRequired() 테스트")
    class HandleSessionRequired {

        @Test
        @DisplayName("[성공] 세션 토큰 발급 → 캐시 업데이트")
        void shouldIssueSessionAndUpdateCache() {
            // Given
            UserAgentId userAgentId = new UserAgentId(1L);
            String userAgentValue = "Mozilla/5.0 Test Agent";
            Instant occurredAt = Instant.parse("2025-01-15T10:00:00Z");
            SessionRequiredEvent event =
                    new SessionRequiredEvent(userAgentId, userAgentValue, occurredAt);

            String token = "session-token-abc123";
            String nid = "nid-test";
            String mustitUid = "uid-test";
            Instant expiresAt = Instant.now().plusSeconds(3600);
            SessionToken sessionToken = new SessionToken(token, nid, mustitUid, expiresAt);

            given(sessionTokenPort.issueSessionToken(userAgentValue))
                    .willReturn(Optional.of(sessionToken));

            // When
            listener.handleSessionRequired(event);

            // Then
            verify(sessionTokenPort).issueSessionToken(userAgentValue);
            verify(cachePort)
                    .updateSession(
                            eq(userAgentId), eq(token), eq(nid), eq(mustitUid), eq(expiresAt));
        }

        @Test
        @DisplayName("[실패] 세션 토큰 발급 실패 → 캐시 업데이트 안함")
        void shouldNotUpdateCacheWhenSessionIssueFails() {
            // Given
            UserAgentId userAgentId = new UserAgentId(1L);
            String userAgentValue = "Mozilla/5.0 Test Agent";
            Instant occurredAt = Instant.parse("2025-01-15T10:00:00Z");
            SessionRequiredEvent event =
                    new SessionRequiredEvent(userAgentId, userAgentValue, occurredAt);

            given(sessionTokenPort.issueSessionToken(userAgentValue)).willReturn(Optional.empty());

            // When
            listener.handleSessionRequired(event);

            // Then
            verify(sessionTokenPort).issueSessionToken(userAgentValue);
            verify(cachePort, never()).updateSession(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("[실패] 예외 발생 → 로그만 기록 (예외 전파 안함)")
        void shouldHandleExceptionGracefully() {
            // Given
            UserAgentId userAgentId = new UserAgentId(1L);
            String userAgentValue = "Mozilla/5.0 Test Agent";
            Instant occurredAt = Instant.parse("2025-01-15T10:00:00Z");
            SessionRequiredEvent event =
                    new SessionRequiredEvent(userAgentId, userAgentValue, occurredAt);

            given(sessionTokenPort.issueSessionToken(userAgentValue))
                    .willThrow(new RuntimeException("External service error"));

            // When - 예외가 전파되지 않아야 함
            listener.handleSessionRequired(event);

            // Then
            verify(sessionTokenPort).issueSessionToken(userAgentValue);
            verify(cachePort, never()).updateSession(any(), any(), any(), any(), any());
        }
    }
}
