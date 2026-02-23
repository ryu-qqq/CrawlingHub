package com.ryuqq.crawlinghub.application.useragent.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.port.out.query.UserAgentPoolCacheQueryPort;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentPoolCacheQueryManager 단위 테스트
 *
 * <p>Redis Pool 캐시 조회 Manager 위임 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolCacheQueryManager 테스트")
class UserAgentPoolCacheQueryManagerTest {

    @Mock private UserAgentPoolCacheQueryPort queryPort;

    @InjectMocks private UserAgentPoolCacheQueryManager manager;

    @Nested
    @DisplayName("getPoolStats() 테스트")
    class GetPoolStats {

        @Test
        @DisplayName("[성공] Pool 통계 조회")
        void shouldReturnPoolStats() {
            // Given
            PoolStats stats = new PoolStats(10, 5, 3, 1, 1, 80.0, 70, 90);
            given(queryPort.getPoolStats()).willReturn(stats);

            // When
            PoolStats result = manager.getPoolStats();

            // Then
            assertThat(result).isEqualTo(stats);
            then(queryPort).should().getPoolStats();
        }
    }

    @Nested
    @DisplayName("getRecoverableUserAgents() 테스트")
    class GetRecoverableUserAgents {

        @Test
        @DisplayName("[성공] 복구 가능한 UserAgent ID 목록 반환")
        void shouldReturnRecoverableUserAgents() {
            // Given
            UserAgentId id1 = UserAgentId.of(1L);
            UserAgentId id2 = UserAgentId.of(2L);
            given(queryPort.getRecoverableUserAgents()).willReturn(List.of(id1, id2));

            // When
            List<UserAgentId> result = manager.getRecoverableUserAgents();

            // Then
            assertThat(result).hasSize(2);
            then(queryPort).should().getRecoverableUserAgents();
        }
    }

    @Nested
    @DisplayName("getAllUserAgentIds() 테스트")
    class GetAllUserAgentIds {

        @Test
        @DisplayName("[성공] 전체 UserAgent ID 목록 반환")
        void shouldReturnAllUserAgentIds() {
            // Given
            given(queryPort.getAllUserAgentIds()).willReturn(List.of(UserAgentId.of(1L)));

            // When
            List<UserAgentId> result = manager.getAllUserAgentIds();

            // Then
            assertThat(result).hasSize(1);
            then(queryPort).should().getAllUserAgentIds();
        }
    }

    @Nested
    @DisplayName("getSessionExpiringUserAgents() 테스트")
    class GetSessionExpiringUserAgents {

        @Test
        @DisplayName("[성공] 세션 만료 임박 UserAgent ID 목록 반환")
        void shouldReturnSessionExpiringUserAgents() {
            // Given
            int bufferMinutes = 10;
            given(queryPort.getSessionExpiringUserAgents(bufferMinutes))
                    .willReturn(List.of(UserAgentId.of(1L)));

            // When
            List<UserAgentId> result = manager.getSessionExpiringUserAgents(bufferMinutes);

            // Then
            assertThat(result).hasSize(1);
            then(queryPort).should().getSessionExpiringUserAgents(bufferMinutes);
        }
    }

    @Nested
    @DisplayName("getSessionRequiredUserAgents() 테스트")
    class GetSessionRequiredUserAgents {

        @Test
        @DisplayName("[성공] 세션 발급 필요한 UserAgent ID 목록 반환")
        void shouldReturnSessionRequiredUserAgents() {
            // Given
            given(queryPort.getSessionRequiredUserAgents()).willReturn(List.of());

            // When
            List<UserAgentId> result = manager.getSessionRequiredUserAgents();

            // Then
            assertThat(result).isEmpty();
            then(queryPort).should().getSessionRequiredUserAgents();
        }
    }
}
