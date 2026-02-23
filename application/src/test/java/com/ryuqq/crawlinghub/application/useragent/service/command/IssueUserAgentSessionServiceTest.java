package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand.SessionIssueType;
import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import com.ryuqq.crawlinghub.application.useragent.manager.SessionDbStatusManager;
import com.ryuqq.crawlinghub.application.useragent.manager.SessionTokenManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheQueryManager;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * IssueUserAgentSessionService 단위 테스트
 *
 * <p>세션 발급(NEW/RENEW) 서비스 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IssueUserAgentSessionService 테스트")
class IssueUserAgentSessionServiceTest {

    @Mock private UserAgentPoolCacheQueryManager cacheQueryManager;
    @Mock private UserAgentPoolCacheCommandManager cacheCommandManager;
    @Mock private SessionTokenManager sessionTokenManager;
    @Mock private SessionDbStatusManager dbStatusManager;

    @InjectMocks private IssueUserAgentSessionService sut;

    @Nested
    @DisplayName("execute() NEW 세션 발급 테스트")
    class ExecuteNew {

        @Test
        @DisplayName("[성공] SESSION_REQUIRED 대상에 세션 발급 성공")
        void shouldIssueNewSessionSuccessfully() {
            // Given
            UserAgentId id = UserAgentId.of(1L);
            CachedUserAgent cached =
                    buildCachedUserAgent(1L, "Mozilla/5.0", UserAgentStatus.SESSION_REQUIRED);
            SessionToken sessionToken =
                    new SessionToken(
                            "token-abc", "nid-val", "uid-val", Instant.now().plusSeconds(3600));

            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(SessionIssueType.NEW, 0, 10, 0);

            given(cacheQueryManager.getSessionRequiredUserAgents()).willReturn(List.of(id));
            given(cacheQueryManager.findById(id)).willReturn(Optional.of(cached));
            given(sessionTokenManager.issueSessionToken("Mozilla/5.0"))
                    .willReturn(Optional.of(sessionToken));

            // When
            int result = sut.execute(command);

            // Then
            assertThat(result).isEqualTo(1);
            then(cacheCommandManager).should().updateSession(any(), any(), any(), any(), any());
            then(dbStatusManager).should().updateStatusToIdle(List.of(id));
        }

        @Test
        @DisplayName("[성공] 대상 없으면 0 반환")
        void shouldReturnZeroWhenNoTargets() {
            // Given
            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(SessionIssueType.NEW, 0, 10, 0);
            given(cacheQueryManager.getSessionRequiredUserAgents()).willReturn(List.of());

            // When
            int result = sut.execute(command);

            // Then
            assertThat(result).isZero();
            then(sessionTokenManager).should(never()).issueSessionToken(any());
        }

        @Test
        @DisplayName("[성공] 캐시에서 UserAgent를 찾을 수 없으면 건너뜀")
        void shouldSkipWhenCachedUserAgentNotFound() {
            // Given
            UserAgentId id = UserAgentId.of(1L);
            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(SessionIssueType.NEW, 0, 10, 0);

            given(cacheQueryManager.getSessionRequiredUserAgents()).willReturn(List.of(id));
            given(cacheQueryManager.findById(id)).willReturn(Optional.empty());

            // When
            int result = sut.execute(command);

            // Then
            assertThat(result).isZero();
            then(sessionTokenManager).should(never()).issueSessionToken(any());
        }

        @Test
        @DisplayName("[성공] 세션 토큰 발급 실패 시 건너뜀")
        void shouldSkipWhenSessionTokenIssuanceFails() {
            // Given
            UserAgentId id = UserAgentId.of(1L);
            CachedUserAgent cached =
                    buildCachedUserAgent(1L, "Mozilla/5.0", UserAgentStatus.SESSION_REQUIRED);
            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(SessionIssueType.NEW, 0, 10, 0);

            given(cacheQueryManager.getSessionRequiredUserAgents()).willReturn(List.of(id));
            given(cacheQueryManager.findById(id)).willReturn(Optional.of(cached));
            given(sessionTokenManager.issueSessionToken("Mozilla/5.0"))
                    .willReturn(Optional.empty());

            // When
            int result = sut.execute(command);

            // Then
            assertThat(result).isZero();
            then(dbStatusManager).should(never()).updateStatusToIdle(any());
        }

        @Test
        @DisplayName("[성공] maxBatchSize 초과 시 처리 중단")
        void shouldStopWhenMaxBatchSizeReached() {
            // Given
            UserAgentId id1 = UserAgentId.of(1L);
            UserAgentId id2 = UserAgentId.of(2L);
            CachedUserAgent cached1 =
                    buildCachedUserAgent(1L, "Mozilla/5.0", UserAgentStatus.SESSION_REQUIRED);
            SessionToken sessionToken =
                    new SessionToken("token", "nid", "uid", Instant.now().plusSeconds(3600));

            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(
                            SessionIssueType.NEW, 0, 1, 0); // maxBatchSize = 1

            given(cacheQueryManager.getSessionRequiredUserAgents()).willReturn(List.of(id1, id2));
            given(cacheQueryManager.findById(id1)).willReturn(Optional.of(cached1));
            given(sessionTokenManager.issueSessionToken("Mozilla/5.0"))
                    .willReturn(Optional.of(sessionToken));

            // When
            int result = sut.execute(command);

            // Then
            assertThat(result).isEqualTo(1); // 1개만 처리
            then(cacheQueryManager).should(never()).findById(id2);
        }
    }

    @Nested
    @DisplayName("execute() RENEW 세션 갱신 테스트")
    class ExecuteRenew {

        @Test
        @DisplayName("[성공] RENEW 타입이면 getSessionExpiringUserAgents 호출")
        void shouldQueryExpiringUserAgentsForRenew() {
            // Given
            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(SessionIssueType.RENEW, 5, 10, 0);

            given(cacheQueryManager.getSessionExpiringUserAgents(5)).willReturn(List.of());

            // When
            int result = sut.execute(command);

            // Then
            assertThat(result).isZero();
            then(cacheQueryManager).should().getSessionExpiringUserAgents(5);
            then(cacheQueryManager).should(never()).getSessionRequiredUserAgents();
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionHandling {

        @Test
        @DisplayName("[성공] 세션 발급 중 예외 발생 시 해당 항목 건너뜀")
        void shouldSkipWhenExceptionDuringIssuance() {
            // Given
            UserAgentId id = UserAgentId.of(1L);
            CachedUserAgent cached =
                    buildCachedUserAgent(1L, "Mozilla/5.0", UserAgentStatus.SESSION_REQUIRED);
            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(SessionIssueType.NEW, 0, 10, 0);

            given(cacheQueryManager.getSessionRequiredUserAgents()).willReturn(List.of(id));
            given(cacheQueryManager.findById(id)).willReturn(Optional.of(cached));
            given(sessionTokenManager.issueSessionToken(any()))
                    .willThrow(new RuntimeException("외부 API 오류"));

            // When
            int result = sut.execute(command);

            // Then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("[성공] DB 동기화 실패 시 성공 카운트는 유지")
        void shouldReturnSuccessCountEvenWhenDbSyncFails() {
            // Given
            UserAgentId id = UserAgentId.of(1L);
            CachedUserAgent cached =
                    buildCachedUserAgent(1L, "Mozilla/5.0", UserAgentStatus.SESSION_REQUIRED);
            SessionToken sessionToken =
                    new SessionToken("token", "nid", "uid", Instant.now().plusSeconds(3600));
            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(SessionIssueType.NEW, 0, 10, 0);

            given(cacheQueryManager.getSessionRequiredUserAgents()).willReturn(List.of(id));
            given(cacheQueryManager.findById(id)).willReturn(Optional.of(cached));
            given(sessionTokenManager.issueSessionToken("Mozilla/5.0"))
                    .willReturn(Optional.of(sessionToken));
            given(dbStatusManager.updateStatusToIdle(any()))
                    .willThrow(new RuntimeException("DB 오류"));

            // When
            int result = sut.execute(command);

            // Then
            assertThat(result).isEqualTo(1);
        }
    }

    private CachedUserAgent buildCachedUserAgent(
            Long id, String userAgentValue, UserAgentStatus status) {
        return new CachedUserAgent(
                id,
                userAgentValue,
                null,
                null,
                null,
                null,
                80,
                80,
                null,
                null,
                100,
                status,
                null,
                null,
                null,
                0);
    }
}
