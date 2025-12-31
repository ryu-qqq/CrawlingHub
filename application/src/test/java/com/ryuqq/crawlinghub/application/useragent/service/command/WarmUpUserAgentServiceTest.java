package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * WarmUpUserAgentService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager 의존성 Mocking
 *
 * <p><strong>테스트 시나리오:</strong>
 *
 * <ul>
 *   <li>AVAILABLE 상태 UserAgent가 없는 경우
 *   <li>UserAgent가 Pool에 추가되는 경우
 *   <li>단일 UserAgent가 Pool에 추가되는 경우
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("WarmUpUserAgentService 테스트")
class WarmUpUserAgentServiceTest {

    @Mock private UserAgentReadManager readManager;

    @Mock private UserAgentPoolCacheManager cacheManager;

    @InjectMocks private WarmUpUserAgentService service;

    @Nested
    @DisplayName("warmUp() Pool Warm-up 테스트")
    class WarmUp {

        @Test
        @DisplayName("[성공] AVAILABLE 상태 UserAgent가 없으면 0 반환")
        void shouldReturnZeroWhenNoAvailableUserAgents() {
            // Given
            given(readManager.findAllAvailable()).willReturn(List.of());

            // When
            int result = service.warmUp();

            // Then
            assertThat(result).isZero();
            then(cacheManager).should(never()).warmUp(anyList());
        }

        @Test
        @DisplayName("[성공] AVAILABLE 상태 UserAgent가 Pool에 추가되는 경우")
        void shouldAddAllUserAgentsToPool() {
            // Given
            UserAgent userAgent1 = UserAgentFixture.anAvailableUserAgent();
            UserAgent userAgent2 = UserAgentFixture.anUserAgentWithId(2L);
            List<UserAgent> availableUserAgents = List.of(userAgent1, userAgent2);

            given(readManager.findAllAvailable()).willReturn(availableUserAgents);
            given(cacheManager.warmUp(anyList())).willReturn(2);

            // When
            int result = service.warmUp();

            // Then
            assertThat(result).isEqualTo(2);
            then(cacheManager).should().warmUp(anyList());
        }

        @Test
        @DisplayName("[성공] 단일 UserAgent가 Pool에 추가되는 경우")
        void shouldAddSingleUserAgentToPool() {
            // Given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            List<UserAgent> availableUserAgents = List.of(userAgent);

            given(readManager.findAllAvailable()).willReturn(availableUserAgents);
            given(cacheManager.warmUp(anyList())).willReturn(1);

            // When
            int result = service.warmUp();

            // Then
            assertThat(result).isEqualTo(1);
            then(cacheManager).should().warmUp(anyList());
        }

        @Test
        @DisplayName("[성공] 여러 UserAgent가 Pool에 추가되는 경우")
        void shouldAddMultipleUserAgentsToPool() {
            // Given
            UserAgent userAgent1 = UserAgentFixture.anAvailableUserAgent();
            UserAgent userAgent2 = UserAgentFixture.anUserAgentWithId(2L);
            UserAgent userAgent3 = UserAgentFixture.anUserAgentWithId(3L);
            List<UserAgent> availableUserAgents = List.of(userAgent1, userAgent2, userAgent3);

            given(readManager.findAllAvailable()).willReturn(availableUserAgents);
            given(cacheManager.warmUp(anyList())).willReturn(3);

            // When
            int result = service.warmUp();

            // Then
            assertThat(result).isEqualTo(3);
            then(cacheManager).should().warmUp(anyList());
        }
    }
}
