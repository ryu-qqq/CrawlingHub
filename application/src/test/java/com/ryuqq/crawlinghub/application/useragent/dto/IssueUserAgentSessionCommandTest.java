package com.ryuqq.crawlinghub.application.useragent.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand.SessionIssueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * IssueUserAgentSessionCommand 단위 테스트
 *
 * <p>세션 발급 커맨드 팩토리 메서드 및 유효성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("IssueUserAgentSessionCommand 테스트")
class IssueUserAgentSessionCommandTest {

    @Nested
    @DisplayName("ofRenew() 팩토리 메서드 테스트")
    class OfRenew {

        @Test
        @DisplayName("[성공] RENEW 타입으로 커맨드 생성")
        void shouldCreateRenewCommand() {
            // When
            IssueUserAgentSessionCommand command =
                    IssueUserAgentSessionCommand.ofRenew(10, 50, 500L);

            // Then
            assertThat(command.issueType()).isEqualTo(SessionIssueType.RENEW);
            assertThat(command.renewalBufferMinutes()).isEqualTo(10);
            assertThat(command.maxBatchSize()).isEqualTo(50);
            assertThat(command.sessionDelayMillis()).isEqualTo(500L);
        }
    }

    @Nested
    @DisplayName("ofNew() 팩토리 메서드 테스트")
    class OfNew {

        @Test
        @DisplayName("[성공] NEW 타입으로 커맨드 생성")
        void shouldCreateNewCommand() {
            // When
            IssueUserAgentSessionCommand command = IssueUserAgentSessionCommand.ofNew(100, 1000L);

            // Then
            assertThat(command.issueType()).isEqualTo(SessionIssueType.NEW);
            assertThat(command.renewalBufferMinutes()).isZero();
            assertThat(command.maxBatchSize()).isEqualTo(100);
            assertThat(command.sessionDelayMillis()).isEqualTo(1000L);
        }
    }

    @Nested
    @DisplayName("직접 생성자 테스트")
    class DirectConstructor {

        @Test
        @DisplayName("[성공] record 생성자로 RENEW 커맨드 생성")
        void shouldCreateDirectlyWithRenewType() {
            // When
            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(SessionIssueType.RENEW, 5, 30, 200L);

            // Then
            assertThat(command.issueType()).isEqualTo(SessionIssueType.RENEW);
            assertThat(command.renewalBufferMinutes()).isEqualTo(5);
            assertThat(command.maxBatchSize()).isEqualTo(30);
            assertThat(command.sessionDelayMillis()).isEqualTo(200L);
        }

        @Test
        @DisplayName("[성공] record 생성자로 NEW 커맨드 생성")
        void shouldCreateDirectlyWithNewType() {
            // When
            IssueUserAgentSessionCommand command =
                    new IssueUserAgentSessionCommand(SessionIssueType.NEW, 0, 20, 300L);

            // Then
            assertThat(command.issueType()).isEqualTo(SessionIssueType.NEW);
        }
    }

    @Nested
    @DisplayName("SessionIssueType enum 테스트")
    class SessionIssueTypeEnum {

        @Test
        @DisplayName("[성공] RENEW와 NEW 타입 모두 존재")
        void shouldHaveBothTypes() {
            // Then
            assertThat(SessionIssueType.values())
                    .containsExactlyInAnyOrder(SessionIssueType.RENEW, SessionIssueType.NEW);
        }
    }
}
