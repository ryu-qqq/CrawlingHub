package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentReadManager;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * WarmUpUserAgentPoolService 단위 테스트
 *
 * <p>Redis Pool WarmUp 서비스 분기 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WarmUpUserAgentPoolService 테스트")
class WarmUpUserAgentPoolServiceTest {

    @Mock private UserAgentReadManager readManager;
    @Mock private UserAgentPoolCacheCommandManager cacheCommandManager;

    @InjectMocks private WarmUpUserAgentPoolService sut;

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[스킵] 이미 초기화된 경우 0 반환하고 이후 작업 생략")
        void shouldSkipWhenAlreadyInitialized() {
            // Given
            given(cacheCommandManager.isPoolInitialized()).willReturn(true);

            // When
            int result = sut.execute();

            // Then
            assertThat(result).isZero();
            then(cacheCommandManager).should().isPoolInitialized();
            then(cacheCommandManager).should(never()).tryAcquireWarmUpLock();
            then(readManager).should(never()).findAllAvailable();
        }

        @Test
        @DisplayName("[스킵] 분산 락 획득 실패 시 0 반환")
        void shouldSkipWhenLockNotAcquired() {
            // Given
            given(cacheCommandManager.isPoolInitialized()).willReturn(false);
            given(cacheCommandManager.tryAcquireWarmUpLock()).willReturn(false);

            // When
            int result = sut.execute();

            // Then
            assertThat(result).isZero();
            then(readManager).should(never()).findAllAvailable();
        }

        @Test
        @DisplayName("[스킵] DB에 AVAILABLE UserAgent가 없으면 0 반환하고 초기화 플래그 설정")
        void shouldMarkInitializedWhenNoAvailableAgents() {
            // Given
            given(cacheCommandManager.isPoolInitialized()).willReturn(false);
            given(cacheCommandManager.tryAcquireWarmUpLock()).willReturn(true);
            given(readManager.findAllAvailable()).willReturn(List.of());

            // When
            int result = sut.execute();

            // Then
            assertThat(result).isZero();
            then(cacheCommandManager).should().markPoolInitialized();
            then(cacheCommandManager).should(never()).warmUp(anyList());
        }

        @Test
        @DisplayName("[성공] DB에서 조회한 UserAgent를 Redis Pool에 등록하고 수 반환")
        void shouldWarmUpWithAvailableAgents() {
            // Given
            UserAgent agent1 = UserAgentFixture.anAvailableUserAgent();
            UserAgent agent2 = UserAgentFixture.anAvailableUserAgent();
            List<UserAgent> availableAgents = List.of(agent1, agent2);

            given(cacheCommandManager.isPoolInitialized()).willReturn(false);
            given(cacheCommandManager.tryAcquireWarmUpLock()).willReturn(true);
            given(readManager.findAllAvailable()).willReturn(availableAgents);
            given(cacheCommandManager.warmUp(anyList())).willReturn(2);

            // When
            int result = sut.execute();

            // Then
            assertThat(result).isEqualTo(2);
            then(cacheCommandManager).should().warmUp(anyList());
            then(cacheCommandManager).should().markPoolInitialized();
        }

        @Test
        @DisplayName("[실패] warmUp 중 예외 발생 시 0 반환")
        void shouldReturnZeroWhenExceptionOccurs() {
            // Given
            UserAgent agent = UserAgentFixture.anAvailableUserAgent();
            given(cacheCommandManager.isPoolInitialized()).willReturn(false);
            given(cacheCommandManager.tryAcquireWarmUpLock()).willReturn(true);
            given(readManager.findAllAvailable()).willReturn(List.of(agent));
            given(cacheCommandManager.warmUp(anyList()))
                    .willThrow(new RuntimeException("Redis 오류"));

            // When
            int result = sut.execute();

            // Then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("CachedUserAgent.forNew() 변환 검증")
    class ForNewConversion {

        @Test
        @DisplayName("[성공] UserAgent가 SESSION_REQUIRED 상태의 CachedUserAgent로 변환됨")
        void shouldConvertToCachedUserAgentWithSessionRequired() {
            // Given
            UserAgent agent = UserAgentFixture.anAvailableUserAgent();
            CachedUserAgent cached = CachedUserAgent.forNew(agent);

            // Then
            assertThat(cached.userAgentId()).isEqualTo(agent.getIdValue());
            assertThat(cached.userAgentValue()).isEqualTo(agent.getUserAgentStringValue());
            assertThat(cached.sessionToken()).isNull();
            assertThat(cached.needsSession()).isTrue();
        }
    }
}
