package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentStatusCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;
import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentMetadata;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentString;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UpdateUserAgentStatusService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserAgentStatusService 테스트")
class UpdateUserAgentStatusServiceTest {

    @Mock private UserAgentReadManager readManager;

    @Mock private UserAgentTransactionManager transactionManager;

    @Mock private UserAgentPoolCacheManager cacheManager;

    @Mock private ClockHolder clockHolder;

    @InjectMocks private UpdateUserAgentStatusService service;

    @Captor private ArgumentCaptor<List<UserAgent>> userAgentsCaptor;

    @Captor private ArgumentCaptor<List<CachedUserAgent>> cachedUserAgentsCaptor;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2025-12-28T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("execute() 상태 변경 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] SUSPENDED → READY 상태 변경")
        void shouldChangeStatusToAvailable() {
            // Given
            List<Long> ids = List.of(1L, 2L, 3L);
            UpdateUserAgentStatusCommand command =
                    new UpdateUserAgentStatusCommand(ids, UserAgentStatus.READY);

            List<UserAgent> userAgents =
                    List.of(
                            createUserAgent(1L, UserAgentStatus.SUSPENDED),
                            createUserAgent(2L, UserAgentStatus.SUSPENDED),
                            createUserAgent(3L, UserAgentStatus.SUSPENDED));

            given(readManager.findByIds(any())).willReturn(userAgents);
            given(clockHolder.getClock()).willReturn(fixedClock);

            // When
            int result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(3);
            then(transactionManager).should().persistAll(userAgentsCaptor.capture());
            then(cacheManager).should().warmUp(cachedUserAgentsCaptor.capture());
            then(cacheManager).should(never()).removeFromPool(any());

            List<UserAgent> savedAgents = userAgentsCaptor.getValue();
            assertThat(savedAgents).hasSize(3);
            savedAgents.forEach(ua -> assertThat(ua.getStatus()).isEqualTo(UserAgentStatus.READY));

            List<CachedUserAgent> cachedAgents = cachedUserAgentsCaptor.getValue();
            assertThat(cachedAgents).hasSize(3);
        }

        @Test
        @DisplayName("[성공] READY → SUSPENDED 상태 변경")
        void shouldChangeStatusToSuspended() {
            // Given
            List<Long> ids = List.of(1L, 2L);
            UpdateUserAgentStatusCommand command =
                    new UpdateUserAgentStatusCommand(ids, UserAgentStatus.SUSPENDED);

            List<UserAgent> userAgents =
                    List.of(
                            createUserAgent(1L, UserAgentStatus.READY),
                            createUserAgent(2L, UserAgentStatus.READY));

            given(readManager.findByIds(any())).willReturn(userAgents);
            given(clockHolder.getClock()).willReturn(fixedClock);

            // When
            int result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(2);
            then(transactionManager).should().persistAll(userAgentsCaptor.capture());
            then(cacheManager).should(never()).warmUp(any());
            then(cacheManager).should(times(2)).removeFromPool(any(UserAgentId.class));

            List<UserAgent> savedAgents = userAgentsCaptor.getValue();
            assertThat(savedAgents).hasSize(2);
            savedAgents.forEach(
                    ua -> assertThat(ua.getStatus()).isEqualTo(UserAgentStatus.SUSPENDED));
        }

        @Test
        @DisplayName("[성공] READY → BLOCKED 상태 변경")
        void shouldChangeStatusToBlocked() {
            // Given
            List<Long> ids = List.of(1L);
            UpdateUserAgentStatusCommand command =
                    new UpdateUserAgentStatusCommand(ids, UserAgentStatus.BLOCKED);

            List<UserAgent> userAgents = List.of(createUserAgent(1L, UserAgentStatus.READY));

            given(readManager.findByIds(any())).willReturn(userAgents);
            given(clockHolder.getClock()).willReturn(fixedClock);

            // When
            int result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(1);
            then(transactionManager).should().persistAll(userAgentsCaptor.capture());
            then(cacheManager).should(never()).warmUp(any());
            then(cacheManager).should().removeFromPool(any(UserAgentId.class));

            List<UserAgent> savedAgents = userAgentsCaptor.getValue();
            assertThat(savedAgents).hasSize(1);
            UserAgent savedAgent = savedAgents.get(0);
            assertThat(savedAgent.getStatus()).isEqualTo(UserAgentStatus.BLOCKED);
        }

        @Test
        @DisplayName("[성공] BLOCKED → READY 상태 변경 (관리자 해제)")
        void shouldUnblockToAvailable() {
            // Given
            List<Long> ids = List.of(1L);
            UpdateUserAgentStatusCommand command =
                    new UpdateUserAgentStatusCommand(ids, UserAgentStatus.READY);

            List<UserAgent> userAgents = List.of(createUserAgent(1L, UserAgentStatus.BLOCKED));

            given(readManager.findByIds(any())).willReturn(userAgents);
            given(clockHolder.getClock()).willReturn(fixedClock);

            // When
            int result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(1);
            then(transactionManager).should().persistAll(userAgentsCaptor.capture());
            then(cacheManager).should().warmUp(any());

            List<UserAgent> savedAgents = userAgentsCaptor.getValue();
            UserAgent savedAgent = savedAgents.get(0);
            assertThat(savedAgent.getStatus()).isEqualTo(UserAgentStatus.READY);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 UserAgent ID가 포함된 경우")
        void shouldThrowExceptionWhenUserAgentNotFound() {
            // Given
            List<Long> ids = List.of(1L, 2L, 999L);
            UpdateUserAgentStatusCommand command =
                    new UpdateUserAgentStatusCommand(ids, UserAgentStatus.SUSPENDED);

            // 1L, 2L만 존재하고 999L은 존재하지 않음
            List<UserAgent> userAgents =
                    List.of(
                            createUserAgent(1L, UserAgentStatus.READY),
                            createUserAgent(2L, UserAgentStatus.READY));

            given(readManager.findByIds(any())).willReturn(userAgents);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(UserAgentNotFoundException.class);

            then(transactionManager).should(never()).persistAll(any());
            then(cacheManager).should(never()).warmUp(any());
            then(cacheManager).should(never()).removeFromPool(any());
        }

        @Test
        @DisplayName("[검증] 단일 UserAgent 상태 변경")
        void shouldChangeSingleUserAgentStatus() {
            // Given
            List<Long> ids = List.of(1L);
            UpdateUserAgentStatusCommand command =
                    new UpdateUserAgentStatusCommand(ids, UserAgentStatus.SUSPENDED);

            List<UserAgent> userAgents = List.of(createUserAgent(1L, UserAgentStatus.READY));

            given(readManager.findByIds(any())).willReturn(userAgents);
            given(clockHolder.getClock()).willReturn(fixedClock);

            // When
            int result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(1);
            then(readManager).should().findByIds(any());
            then(transactionManager).should().persistAll(any());
        }

        @Test
        @DisplayName("[검증] 빈 결과 조회 시 예외 발생")
        void shouldThrowExceptionWhenNoUserAgentsFound() {
            // Given
            List<Long> ids = List.of(1L);
            UpdateUserAgentStatusCommand command =
                    new UpdateUserAgentStatusCommand(ids, UserAgentStatus.SUSPENDED);

            given(readManager.findByIds(any())).willReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(UserAgentNotFoundException.class);

            then(transactionManager).should(never()).persistAll(any());
        }
    }

    @Nested
    @DisplayName("Redis Pool 처리 테스트")
    class RedisPoolHandling {

        @Test
        @DisplayName("[검증] READY 상태 변경 시 warmUp 호출")
        void shouldCallWarmUpWhenChangingToAvailable() {
            // Given
            List<Long> ids = List.of(1L);
            UpdateUserAgentStatusCommand command =
                    new UpdateUserAgentStatusCommand(ids, UserAgentStatus.READY);

            List<UserAgent> userAgents = List.of(createUserAgent(1L, UserAgentStatus.SUSPENDED));

            given(readManager.findByIds(any())).willReturn(userAgents);
            given(clockHolder.getClock()).willReturn(fixedClock);

            // When
            service.execute(command);

            // Then
            then(cacheManager).should().warmUp(cachedUserAgentsCaptor.capture());
            then(cacheManager).should(never()).removeFromPool(any());

            List<CachedUserAgent> cachedAgents = cachedUserAgentsCaptor.getValue();
            assertThat(cachedAgents).hasSize(1);
        }

        @Test
        @DisplayName("[검증] 비-READY 상태 변경 시 removeFromPool 호출")
        void shouldCallRemoveFromPoolWhenChangingToNonAvailable() {
            // Given
            List<Long> ids = List.of(1L, 2L);
            UpdateUserAgentStatusCommand command =
                    new UpdateUserAgentStatusCommand(ids, UserAgentStatus.BLOCKED);

            List<UserAgent> userAgents =
                    List.of(
                            createUserAgent(1L, UserAgentStatus.READY),
                            createUserAgent(2L, UserAgentStatus.SUSPENDED));

            given(readManager.findByIds(any())).willReturn(userAgents);
            given(clockHolder.getClock()).willReturn(fixedClock);

            // When
            service.execute(command);

            // Then
            then(cacheManager).should(never()).warmUp(any());
            then(cacheManager).should(times(2)).removeFromPool(any(UserAgentId.class));
        }
    }

    /** 테스트용 UserAgent 생성 */
    private UserAgent createUserAgent(long id, UserAgentStatus status) {
        // Token은 최소 44자 이상 + 유효한 Base64 형식이어야 함
        String token = "dGVzdC10b2tlbi12YWx1ZS1mb3ItdGVzdGluZy1wdXJwb3NlLTEyMzQ1";
        return UserAgent.reconstitute(
                UserAgentId.of(id),
                Token.of(token),
                UserAgentString.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"),
                DeviceType.of(DeviceType.Type.DESKTOP),
                UserAgentMetadata.defaultMetadata(),
                status,
                HealthScore.initial(),
                Instant.parse("2025-12-27T10:00:00Z"),
                100,
                Instant.parse("2025-12-01T00:00:00Z"),
                Instant.parse("2025-12-27T10:00:00Z"));
    }
}
