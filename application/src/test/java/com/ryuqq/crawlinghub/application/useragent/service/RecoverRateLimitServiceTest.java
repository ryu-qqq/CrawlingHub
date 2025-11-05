package com.ryuqq.crawlinghub.application.useragent.service;

import com.ryuqq.crawlinghub.application.useragent.assembler.UserAgentAssembler;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverRateLimitCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverRateLimitCommandFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDtoFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
import com.ryuqq.crawlinghub.application.useragent.port.out.SaveUserAgentPort;
import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;
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
 * RecoverRateLimitService 단위 테스트
 *
 * <p>Rate Limit 복구 UseCase의 비즈니스 로직을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverRateLimitService 단위 테스트")
class RecoverRateLimitServiceTest {

    @Mock
    private LoadUserAgentPort loadUserAgentPort;

    @Mock
    private SaveUserAgentPort saveUserAgentPort;

    @Mock
    private UserAgentAssembler assembler;

    @InjectMocks
    private RecoverRateLimitService sut;

    @Nested
    @DisplayName("execute 메서드는")
    class Describe_execute {

        @Nested
        @DisplayName("RATE_LIMITED 상태의 UserAgent가 주어지면")
        class Context_with_rate_limited_user_agent {

            private RecoverRateLimitCommand command;
            private UserAgentQueryDto queryDto;
            private UserAgent userAgent;
            private UserAgent savedUserAgent;

            @BeforeEach
            void setUp() {
                // Given: Rate Limit 복구 Command
                command = RecoverRateLimitCommandFixture.create();
                queryDto = UserAgentQueryDtoFixture.createWithStatus(TokenStatus.RATE_LIMITED);
                userAgent = UserAgentFixture.createRateLimited();
                savedUserAgent = UserAgentFixture.createRecovered();

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
            @DisplayName("Rate Limit을 복구하고 응답을 반환한다")
            void it_recovers_rate_limit_and_returns_response() {
                // When: Rate Limit 복구 실행
                UserAgentResponse response = sut.execute(command);

                // Then: UserAgent 조회가 수행됨
                then(loadUserAgentPort).should().findById(UserAgentId.of(command.userAgentId()));

                // And: DTO → Domain 변환이 수행됨
                then(assembler).should().toDomain(queryDto);

                // And: Rate Limit 복구가 수행됨 (Domain 메서드 호출)
                assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.RECOVERED);
                assertThat(userAgent.getRemainingRequests()).isEqualTo(80);

                // And: UserAgent가 저장됨
                then(saveUserAgentPort).should().save(userAgent);

                // And: 응답이 반환됨
                assertThat(response).isNotNull();
            }
        }

        @Nested
        @DisplayName("존재하지 않는 UserAgent ID가 주어지면")
        class Context_with_non_existent_user_agent_id {

            private RecoverRateLimitCommand command;

            @BeforeEach
            void setUp() {
                // Given: 존재하지 않는 UserAgent ID
                command = RecoverRateLimitCommandFixture.create();

                // And: UserAgent 조회 실패
                given(loadUserAgentPort.findById(any(UserAgentId.class)))
                    .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NoAvailableUserAgentException을 발생시킨다")
            void it_throws_no_available_user_agent_exception() {
                // When & Then: Rate Limit 복구 시도 시 예외 발생
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

