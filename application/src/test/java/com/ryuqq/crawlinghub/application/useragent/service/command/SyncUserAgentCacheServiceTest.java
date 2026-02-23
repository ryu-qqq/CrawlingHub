package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheQueryManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentReadManager;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncUserAgentCacheService 테스트")
class SyncUserAgentCacheServiceTest {

    @Mock private UserAgentPoolCacheQueryManager cacheQueryManager;
    @Mock private UserAgentReadManager readManager;
    @Mock private UserAgentCommandManager commandManager;
    @InjectMocks private SyncUserAgentCacheService service;

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 캐시와 DB 상태가 다르면 동기화")
        void shouldSyncWhenStateDiffers() {
            UserAgentId id = UserAgentId.of(1L);
            UserAgent dbUserAgent = UserAgentFixture.anAvailableUserAgent();
            CachedUserAgent cached =
                    new CachedUserAgent(
                            1L,
                            "Mozilla/5.0",
                            null,
                            null,
                            null,
                            null,
                            80,
                            80,
                            null,
                            null,
                            50,
                            UserAgentStatus.IDLE,
                            null,
                            null,
                            null,
                            0);

            given(cacheQueryManager.getAllUserAgentIds()).willReturn(List.of(id));
            given(cacheQueryManager.findById(id)).willReturn(Optional.of(cached));
            given(readManager.findByIds(List.of(id))).willReturn(List.of(dbUserAgent));

            int result = service.execute();

            assertThat(result).isEqualTo(1);
            verify(commandManager).persistAll(any());
        }

        @Test
        @DisplayName("[성공] 캐시와 DB 상태가 같으면 동기화 건너뜀")
        void shouldSkipWhenStateMatches() {
            UserAgentId id = UserAgentId.of(1L);
            UserAgent dbUserAgent = UserAgentFixture.anAvailableUserAgent();
            CachedUserAgent cached = CachedUserAgent.forDbFallback(dbUserAgent);

            given(cacheQueryManager.getAllUserAgentIds()).willReturn(List.of(id));
            given(cacheQueryManager.findById(id)).willReturn(Optional.of(cached));
            given(readManager.findByIds(List.of(id))).willReturn(List.of(dbUserAgent));

            int result = service.execute();

            assertThat(result).isEqualTo(0);
            verify(commandManager, never()).persistAll(any());
        }

        @Test
        @DisplayName("[성공] 캐시가 비어있으면 0 반환")
        void shouldReturnZeroWhenCacheEmpty() {
            given(cacheQueryManager.getAllUserAgentIds()).willReturn(List.of());

            int result = service.execute();

            assertThat(result).isEqualTo(0);
            verify(commandManager, never()).persistAll(any());
        }

        @Test
        @DisplayName("[성공] DB에 없는 UserAgent는 건너뜀")
        void shouldSkipWhenNotFoundInDb() {
            UserAgentId id = UserAgentId.of(99L);
            CachedUserAgent cached =
                    new CachedUserAgent(
                            99L,
                            "Mozilla/5.0",
                            null,
                            null,
                            null,
                            null,
                            80,
                            80,
                            null,
                            null,
                            50,
                            UserAgentStatus.IDLE,
                            null,
                            null,
                            null,
                            0);

            given(cacheQueryManager.getAllUserAgentIds()).willReturn(List.of(id));
            given(cacheQueryManager.findById(id)).willReturn(Optional.of(cached));
            given(readManager.findByIds(List.of(id))).willReturn(List.of());

            int result = service.execute();

            assertThat(result).isEqualTo(0);
            verify(commandManager, never()).persistAll(any());
        }
    }
}
