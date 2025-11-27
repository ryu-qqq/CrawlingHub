package com.ryuqq.crawlinghub.application.useragent.service.command;

import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RecordUserAgentResultCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RecordUserAgentResultService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecordUserAgentResultService 테스트")
class RecordUserAgentResultServiceTest {

    @Mock private UserAgentPoolManager poolManager;

    @InjectMocks private RecordUserAgentResultService service;

    @Nested
    @DisplayName("execute() 결과 기록 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 성공 결과 기록")
        void shouldRecordSuccessResult() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.success(1L);

            // When
            service.execute(command);

            // Then
            then(poolManager).should().recordResult(command);
        }

        @Test
        @DisplayName("[성공] 실패 결과 (500 에러) 기록")
        void shouldRecordFailureResult() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.failure(1L, 500);

            // When
            service.execute(command);

            // Then
            then(poolManager).should().recordResult(command);
        }

        @Test
        @DisplayName("[성공] Rate Limit (429) 결과 기록")
        void shouldRecordRateLimitResult() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.failure(1L, 429);

            // When
            service.execute(command);

            // Then
            then(poolManager).should().recordResult(command);
        }

        @Test
        @DisplayName("[성공] 클라이언트 에러 (4xx) 결과 기록")
        void shouldRecordClientErrorResult() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.failure(1L, 404);

            // When
            service.execute(command);

            // Then
            then(poolManager).should().recordResult(command);
        }
    }
}
