package com.ryuqq.crawlinghub.application.useragent.service;

import com.ryuqq.crawlinghub.application.useragent.dto.query.GetUserAgentDetailQuery;
import com.ryuqq.crawlinghub.application.useragent.dto.query.GetUserAgentDetailQueryFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDtoFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
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
 * GetUserAgentDetailService 단위 테스트
 *
 * <p>UserAgent 상세 조회 UseCase의 비즈니스 로직을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserAgentDetailService 단위 테스트")
class GetUserAgentDetailServiceTest {

    @Mock
    private LoadUserAgentPort loadUserAgentPort;

    @InjectMocks
    private GetUserAgentDetailService sut;

    @Nested
    @DisplayName("execute 메서드는")
    class Describe_execute {

        @Nested
        @DisplayName("존재하는 UserAgent ID가 주어지면")
        class Context_with_existing_user_agent_id {

            private GetUserAgentDetailQuery query;
            private UserAgentQueryDto queryDto;

            @BeforeEach
            void setUp() {
                // Given: UserAgent 상세 조회 Query
                query = GetUserAgentDetailQueryFixture.create();
                queryDto = UserAgentQueryDtoFixture.create();

                // And: UserAgent 조회 성공
                given(loadUserAgentPort.findById(any(UserAgentId.class)))
                    .willReturn(Optional.of(queryDto));
            }

            @Test
            @DisplayName("UserAgent 상세 정보를 반환한다")
            void it_returns_user_agent_detail() {
                // When: UserAgent 상세 조회 실행
                UserAgentResponse response = sut.execute(query);

                // Then: UserAgent 조회가 수행됨
                then(loadUserAgentPort).should().findById(UserAgentId.of(query.userAgentId()));

                // And: 응답이 반환됨
                assertThat(response).isNotNull();
                assertThat(response.userAgentId()).isEqualTo(queryDto.id());
                assertThat(response.userAgentString()).isEqualTo(queryDto.userAgentString());
                assertThat(response.tokenStatus()).isEqualTo(queryDto.tokenStatus());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 UserAgent ID가 주어지면")
        class Context_with_non_existent_user_agent_id {

            private GetUserAgentDetailQuery query;

            @BeforeEach
            void setUp() {
                // Given: 존재하지 않는 UserAgent ID
                query = GetUserAgentDetailQueryFixture.create();

                // And: UserAgent 조회 실패
                given(loadUserAgentPort.findById(any(UserAgentId.class)))
                    .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NoAvailableUserAgentException을 발생시킨다")
            void it_throws_no_available_user_agent_exception() {
                // When & Then: UserAgent 상세 조회 시도 시 예외 발생
                assertThatThrownBy(() -> sut.execute(query))
                    .isInstanceOf(NoAvailableUserAgentException.class);

                // And: UserAgent 조회는 수행됨
                then(loadUserAgentPort).should().findById(UserAgentId.of(query.userAgentId()));
            }
        }
    }
}



