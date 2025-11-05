package com.ryuqq.crawlinghub.application.useragent.service;

import com.ryuqq.crawlinghub.application.useragent.assembler.UserAgentAssembler;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueTokenCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueTokenCommandFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDtoFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
import com.ryuqq.crawlinghub.application.useragent.port.out.SaveUserAgentPort;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * IssueTokenService 단위 테스트
 *
 * <p>토큰 발급 UseCase의 비즈니스 로직을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IssueTokenService 단위 테스트")
class IssueTokenServiceTest {

    @Mock
    private LoadUserAgentPort loadUserAgentPort;

    @Mock
    private SaveUserAgentPort saveUserAgentPort;

    @Mock
    private UserAgentAssembler assembler;

    @InjectMocks
    private IssueTokenService sut;

    @Nested
    @DisplayName("execute 메서드는")
    class Describe_execute {

        @Nested
        @DisplayName("유효한 UserAgent ID와 토큰이 주어지면")
        class Context_with_valid_user_agent_and_token {

            private IssueTokenCommand command;
            private UserAgentQueryDto queryDto;
            private UserAgent userAgent;
            private UserAgent savedUserAgent;

            @BeforeEach
            void setUp() {
                // Given: 유효한 토큰 발급 Command
                command = IssueTokenCommandFixture.create();
                queryDto = UserAgentQueryDtoFixture.create();
                userAgent = UserAgentFixture.createWithId(command.userAgentId());
                savedUserAgent = UserAgentFixture.createWithId(command.userAgentId());

                // And: UserAgent 조회 성공
                given(loadUserAgentPort.findById(any(UserAgentId.class)))
                    .willReturn(Optional.of(queryDto));

                // And: DTO → Domain 변환
                given(assembler.toDomain(queryDto))
                    .willReturn(userAgent);

                // And: UserAgent 저장 성공
                given(saveUserAgentPort.save(any(UserAgent.class)))
                    .willReturn(savedUserAgent);
            }

            @Test
            @DisplayName("토큰을 발급하고 응답을 반환한다")
            void it_issues_token_and_returns_response() {
                // When: 토큰 발급 실행
                UserAgentResponse response = sut.execute(command);

                // Then: UserAgent 조회가 수행됨
                then(loadUserAgentPort).should().findById(UserAgentId.of(command.userAgentId()));

                // And: DTO → Domain 변환이 수행됨
                then(assembler).should().toDomain(queryDto);

                // And: 토큰 발급이 수행됨 (Domain 메서드 호출)
                assertThat(userAgent.getCurrentToken()).isEqualTo(command.token());
                assertThat(userAgent.getTokenStatus().name()).isEqualTo("IDLE");

                // And: UserAgent가 저장됨
                then(saveUserAgentPort).should().save(userAgent);

                // And: 응답이 반환됨
                assertThat(response).isNotNull();
            }
        }

        @Nested
        @DisplayName("존재하지 않는 UserAgent ID가 주어지면")
        class Context_with_non_existent_user_agent_id {

            private IssueTokenCommand command;

            @BeforeEach
            void setUp() {
                // Given: 존재하지 않는 UserAgent ID
                command = IssueTokenCommandFixture.create();

                // And: UserAgent 조회 실패
                given(loadUserAgentPort.findById(any(UserAgentId.class)))
                    .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NoAvailableUserAgentException을 발생시킨다")
            void it_throws_no_available_user_agent_exception() {
                // When & Then: 토큰 발급 시도 시 예외 발생
                assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(NoAvailableUserAgentException.class);

                // And: UserAgent 조회는 수행됨
                then(loadUserAgentPort).should().findById(UserAgentId.of(command.userAgentId()));

                // And: 저장은 수행되지 않음
                then(saveUserAgentPort).shouldHaveNoInteractions();
            }
        }
    }
}

