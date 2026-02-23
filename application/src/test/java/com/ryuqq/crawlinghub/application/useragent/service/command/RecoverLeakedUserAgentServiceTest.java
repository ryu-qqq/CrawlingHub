package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverLeakedUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RecoverLeakedUserAgentService 단위 테스트
 *
 * <p>Leak된 BORROWED UserAgent 강제 반납 서비스 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverLeakedUserAgentService 테스트")
class RecoverLeakedUserAgentServiceTest {

    @Mock private UserAgentPoolCacheCommandManager cacheCommandManager;

    @InjectMocks private RecoverLeakedUserAgentService service;

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] Leak 없으면 0 반환")
        void shouldReturnZeroWhenNoLeaks() {
            // Given
            RecoverLeakedUserAgentCommand command = new RecoverLeakedUserAgentCommand(30000L);
            given(cacheCommandManager.detectLeakedAgents(30000L)).willReturn(List.of());

            // When
            int result = service.execute(command);

            // Then
            assertThat(result).isZero();
            then(cacheCommandManager).should().detectLeakedAgents(30000L);
        }

        @Test
        @DisplayName("[성공] Leak된 UserAgent를 모두 강제 반납하고 복구 수 반환")
        void shouldReturnRecoveredCountWhenLeaksFound() {
            // Given
            RecoverLeakedUserAgentCommand command = new RecoverLeakedUserAgentCommand(30000L);
            given(cacheCommandManager.detectLeakedAgents(30000L)).willReturn(List.of(1L, 2L, 3L));
            given(cacheCommandManager.returnAgent(1L, false, 0, 0, null, 0)).willReturn(0);
            given(cacheCommandManager.returnAgent(2L, false, 0, 0, null, 0)).willReturn(0);
            given(cacheCommandManager.returnAgent(3L, false, 0, 0, null, 0)).willReturn(0);

            // When
            int result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(3);
        }

        @Test
        @DisplayName("[부분 실패] 일부 반납 실패 시 성공한 것만 카운트")
        void shouldCountOnlySuccessfulRecoveries() {
            // Given
            RecoverLeakedUserAgentCommand command = new RecoverLeakedUserAgentCommand(30000L);
            given(cacheCommandManager.detectLeakedAgents(30000L)).willReturn(List.of(1L, 2L));
            given(cacheCommandManager.returnAgent(1L, false, 0, 0, null, 0)).willReturn(0);
            willThrow(new RuntimeException("반납 실패"))
                    .given(cacheCommandManager)
                    .returnAgent(2L, false, 0, 0, null, 0);

            // When
            int result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(1); // 1개만 성공
        }
    }
}
