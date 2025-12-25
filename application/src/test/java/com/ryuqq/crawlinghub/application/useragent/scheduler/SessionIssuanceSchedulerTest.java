package com.ryuqq.crawlinghub.application.useragent.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.useragent.config.SessionSchedulerProperties;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CacheStatus;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import com.ryuqq.crawlinghub.application.useragent.port.out.cache.UserAgentPoolCachePort;
import com.ryuqq.crawlinghub.application.useragent.port.out.session.SessionTokenPort;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SessionIssuanceScheduler 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port, Cache Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionIssuanceScheduler 테스트")
class SessionIssuanceSchedulerTest {

    @Mock private SessionTokenPort sessionTokenPort;

    @Mock private UserAgentPoolCachePort cachePort;

    @Mock private SessionSchedulerProperties properties;

    private SessionIssuanceScheduler scheduler;

    @BeforeEach
    void setUp() {
        given(properties.getRenewalBufferMinutes()).willReturn(5);
        scheduler = new SessionIssuanceScheduler(sessionTokenPort, cachePort, properties);
    }

    @Nested
    @DisplayName("issueSessionTokens() 테스트")
    class IssueSessionTokens {

        @Test
        @DisplayName("[성공] SESSION_REQUIRED UserAgent에 세션 발급")
        void shouldIssueSessionTokensToRequiredUserAgents() {
            // Given
            UserAgentId userAgentId = new UserAgentId(1L);
            List<UserAgentId> sessionRequiredIds = List.of(userAgentId);

            CachedUserAgent cachedUserAgent =
                    new CachedUserAgent(
                            userAgentId.value(),
                            "Mozilla/5.0 Test Agent",
                            null, // sessionToken
                            null, // nid
                            null, // mustitUid
                            null, // sessionExpiresAt
                            80, // remainingTokens
                            80, // maxTokens
                            null, // windowStart
                            null, // windowEnd
                            100, // healthScore
                            CacheStatus.SESSION_REQUIRED,
                            null); // suspendedAt

            SessionToken sessionToken =
                    new SessionToken("token-abc123", Instant.now().plusSeconds(1800));

            given(cachePort.getSessionRequiredUserAgents()).willReturn(sessionRequiredIds);
            given(cachePort.findById(userAgentId)).willReturn(Optional.of(cachedUserAgent));
            given(sessionTokenPort.issueSessionToken("Mozilla/5.0 Test Agent"))
                    .willReturn(Optional.of(sessionToken));

            // When
            scheduler.issueSessionTokens();

            // Then
            verify(cachePort).getSessionRequiredUserAgents();
            verify(cachePort).findById(userAgentId);
            verify(sessionTokenPort).issueSessionToken("Mozilla/5.0 Test Agent");
            verify(cachePort).updateSession(any(), anyString(), any(Instant.class));
        }

        @Test
        @DisplayName("[성공] 세션 발급 대상이 없는 경우 → 아무 작업 안함")
        void shouldDoNothingWhenNoUserAgentsNeedSession() {
            // Given
            given(cachePort.getSessionRequiredUserAgents()).willReturn(Collections.emptyList());

            // When
            scheduler.issueSessionTokens();

            // Then
            verify(cachePort).getSessionRequiredUserAgents();
            verify(sessionTokenPort, never()).issueSessionToken(anyString());
            verify(cachePort, never()).updateSession(any(), anyString(), any());
        }

        @Test
        @DisplayName("[실패] 캐시에서 UserAgent를 찾을 수 없는 경우 → 스킵")
        void shouldSkipWhenCachedUserAgentNotFound() {
            // Given
            UserAgentId userAgentId = new UserAgentId(1L);
            List<UserAgentId> sessionRequiredIds = List.of(userAgentId);

            given(cachePort.getSessionRequiredUserAgents()).willReturn(sessionRequiredIds);
            given(cachePort.findById(userAgentId)).willReturn(Optional.empty());

            // When
            scheduler.issueSessionTokens();

            // Then
            verify(cachePort).getSessionRequiredUserAgents();
            verify(cachePort).findById(userAgentId);
            verify(sessionTokenPort, never()).issueSessionToken(anyString());
            verify(cachePort, never()).updateSession(any(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("renewExpiringSessionTokens() 테스트")
    class RenewExpiringSessionTokens {

        @Test
        @DisplayName("[성공] 만료 임박 세션 선제적 갱신")
        void shouldRenewExpiringSessionTokens() {
            // Given
            UserAgentId userAgentId = new UserAgentId(1L);
            List<UserAgentId> expiringIds = List.of(userAgentId);

            CachedUserAgent cachedUserAgent =
                    new CachedUserAgent(
                            userAgentId.value(),
                            "Mozilla/5.0 Test Agent",
                            "old-token", // sessionToken
                            null, // nid
                            null, // mustitUid
                            Instant.now().plusSeconds(120), // sessionExpiresAt (만료 임박)
                            80, // remainingTokens
                            80, // maxTokens
                            null, // windowStart
                            null, // windowEnd
                            100, // healthScore
                            CacheStatus.READY,
                            null); // suspendedAt

            SessionToken newSessionToken =
                    new SessionToken("new-token-xyz789", Instant.now().plusSeconds(1800));

            given(cachePort.getSessionExpiringUserAgents(5)).willReturn(expiringIds);
            given(cachePort.findById(userAgentId)).willReturn(Optional.of(cachedUserAgent));
            given(sessionTokenPort.issueSessionToken("Mozilla/5.0 Test Agent"))
                    .willReturn(Optional.of(newSessionToken));

            // When
            scheduler.renewExpiringSessionTokens();

            // Then
            verify(cachePort).getSessionExpiringUserAgents(5);
            verify(cachePort).findById(userAgentId);
            verify(sessionTokenPort).issueSessionToken("Mozilla/5.0 Test Agent");
            verify(cachePort).updateSession(any(), anyString(), any(Instant.class));
        }

        @Test
        @DisplayName("[성공] 갱신 대상이 없는 경우 → 아무 작업 안함")
        void shouldDoNothingWhenNoExpiringUserAgents() {
            // Given
            given(cachePort.getSessionExpiringUserAgents(5)).willReturn(Collections.emptyList());

            // When
            scheduler.renewExpiringSessionTokens();

            // Then
            verify(cachePort).getSessionExpiringUserAgents(5);
            verify(sessionTokenPort, never()).issueSessionToken(anyString());
            verify(cachePort, never()).updateSession(any(), anyString(), any());
        }
    }
}
