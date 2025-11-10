package com.ryuqq.crawlinghub.adapter.in.rest.useragent;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller.UserAgentController;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.IssueTokenApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.UserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueTokenCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;
import com.ryuqq.crawlinghub.application.useragent.port.in.DisableUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.GetUserAgentDetailUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.IssueTokenUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.RecoverRateLimitUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserAgentController 단위 테스트
 *
 * <p>Mockito를 사용하여 Controller Layer만 격리 테스트합니다.
 * UseCase는 Mock으로 대체하여 Controller 로직만 검증합니다.
 *
 * <p>Spring REST Docs를 사용하여 API 문서를 자동 생성합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
@DisplayName("UserAgentController 단위 테스트")
class UserAgentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private IssueTokenUseCase issueTokenUseCase;

    @Mock
    private RecoverRateLimitUseCase recoverRateLimitUseCase;

    @Mock
    private DisableUserAgentUseCase disableUserAgentUseCase;

    @Mock
    private GetUserAgentDetailUseCase getUserAgentDetailUseCase;

    @Mock
    private UserAgentApiMapper userAgentApiMapper;

    @InjectMocks
    private UserAgentController userAgentController;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders
            .standaloneSetup(userAgentController)
            .apply(documentationConfiguration(restDocumentation))
            .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("GET /api/v1/user-agents/{userAgentId}")
    class Describe_getUserAgentDetail {

        @Test
        @DisplayName("존재하는 UserAgent ID로 조회하면 상세 정보를 반환한다")
        void it_returns_user_agent_detail() throws Exception {
            // Given
            Long userAgentId = 1L;
            UserAgentResponse response = createUserAgentResponse();
            UserAgentApiResponse apiResponse = createUserAgentApiResponse();

            given(getUserAgentDetailUseCase.execute(any()))
                .willReturn(response);
            given(userAgentApiMapper.toResponse(response))
                .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/user-agents/{userAgentId}", userAgentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userAgentId").value(userAgentId))
                .andExpect(jsonPath("$.data.userAgentString").value("Mozilla/5.0"))
                .andExpect(jsonPath("$.data.tokenStatus").value("IDLE"))
                .andDo(document("user-agent-get-detail",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("userAgentId").description("UserAgent ID")
                    ),
                    responseFields(
                        fieldWithPath("success").description("성공 여부"),
                        fieldWithPath("data").description("응답 데이터"),
                        fieldWithPath("data.userAgentId").description("UserAgent ID"),
                        fieldWithPath("data.userAgentString").description("UserAgent 문자열"),
                        fieldWithPath("data.tokenStatus").description("토큰 상태 (IDLE, IN_USE, RATE_LIMITED, RECOVERED)"),
                        fieldWithPath("data.remainingRequests").description("남은 요청 수"),
                        fieldWithPath("data.tokenIssuedAt").description("토큰 발급 시각"),
                        fieldWithPath("data.rateLimitResetAt").description("Rate Limit 리셋 시각").optional(),
                        fieldWithPath("data.createdAt").description("생성 시각"),
                        fieldWithPath("data.updatedAt").description("수정 시각"),
                        fieldWithPath("error").description("에러 정보").optional(),
                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                        fieldWithPath("requestId").description("요청 ID")
                    )
                ));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/user-agents/{userAgentId}/tokens")
    class Describe_issueToken {

        @Test
        @DisplayName("유효한 토큰으로 발급하면 201 Created를 반환한다")
        void it_issues_token_and_returns_201() throws Exception {
            // Given
            Long userAgentId = 1L;
            IssueTokenApiRequest request = new IssueTokenApiRequest("new-token-12345");
            String requestBody = objectMapper.writeValueAsString(request);

            UserAgentResponse response = createUserAgentResponse();
            UserAgentApiResponse apiResponse = createUserAgentApiResponse();

            given(userAgentApiMapper.toCommand(any(Long.class), any(IssueTokenApiRequest.class)))
                .willReturn(new IssueTokenCommand(userAgentId, "new-token-12345"));
            given(issueTokenUseCase.execute(any(IssueTokenCommand.class)))
                .willReturn(response);
            given(userAgentApiMapper.toResponse(response))
                .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/user-agents/{userAgentId}/tokens", userAgentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userAgentId").value(userAgentId))
                .andDo(document("user-agent-issue-token",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("userAgentId").description("UserAgent ID")
                    ),
                    requestFields(
                        fieldWithPath("token").description("발급할 토큰 값")
                    ),
                    responseFields(
                        fieldWithPath("success").description("성공 여부"),
                        fieldWithPath("data").description("응답 데이터"),
                        fieldWithPath("data.userAgentId").description("UserAgent ID"),
                        fieldWithPath("data.userAgentString").description("UserAgent 문자열"),
                        fieldWithPath("data.tokenStatus").description("토큰 상태"),
                        fieldWithPath("data.remainingRequests").description("남은 요청 수"),
                        fieldWithPath("data.tokenIssuedAt").description("토큰 발급 시각"),
                        fieldWithPath("data.rateLimitResetAt").description("Rate Limit 리셋 시각").optional(),
                        fieldWithPath("data.createdAt").description("생성 시각"),
                        fieldWithPath("data.updatedAt").description("수정 시각"),
                        fieldWithPath("error").description("에러 정보").optional(),
                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                        fieldWithPath("requestId").description("요청 ID")
                    )
                ));
        }

        @Test
        @DisplayName("토큰이 빈 문자열이면 400 Bad Request를 반환한다")
        void it_returns_400_when_token_is_blank() throws Exception {
            // Given
            Long userAgentId = 1L;
            IssueTokenApiRequest request = new IssueTokenApiRequest("   ");
            String requestBody = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/v1/user-agents/{userAgentId}/tokens", userAgentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/user-agents/{userAgentId}/recover")
    class Describe_recoverRateLimit {

        @Test
        @DisplayName("Rate Limit 복구 요청하면 200 OK를 반환한다")
        void it_recovers_rate_limit_and_returns_200() throws Exception {
            // Given
            Long userAgentId = 1L;
            UserAgentResponse response = createUserAgentResponse();
            UserAgentApiResponse apiResponse = createUserAgentApiResponse();

            given(recoverRateLimitUseCase.execute(any()))
                .willReturn(response);
            given(userAgentApiMapper.toResponse(response))
                .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(put("/api/v1/user-agents/{userAgentId}/recover", userAgentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userAgentId").value(userAgentId))
                .andDo(document("user-agent-recover-rate-limit",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("userAgentId").description("UserAgent ID")
                    ),
                    responseFields(
                        fieldWithPath("success").description("성공 여부"),
                        fieldWithPath("data").description("응답 데이터"),
                        fieldWithPath("data.userAgentId").description("UserAgent ID"),
                        fieldWithPath("data.userAgentString").description("UserAgent 문자열"),
                        fieldWithPath("data.tokenStatus").description("토큰 상태 (RECOVERED)"),
                        fieldWithPath("data.remainingRequests").description("남은 요청 수 (복구 후 초기화)"),
                        fieldWithPath("data.tokenIssuedAt").description("토큰 발급 시각"),
                        fieldWithPath("data.rateLimitResetAt").description("Rate Limit 리셋 시각 (null)").optional(),
                        fieldWithPath("data.createdAt").description("생성 시각"),
                        fieldWithPath("data.updatedAt").description("수정 시각"),
                        fieldWithPath("error").description("에러 정보").optional(),
                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                        fieldWithPath("requestId").description("요청 ID")
                    )
                ));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/user-agents/{userAgentId}")
    class Describe_disableUserAgent {

        @Test
        @DisplayName("UserAgent 비활성화 요청하면 200 OK를 반환한다")
        void it_disables_user_agent_and_returns_200() throws Exception {
            // Given
            Long userAgentId = 1L;
            UserAgentResponse response = createUserAgentResponse();
            UserAgentApiResponse apiResponse = createUserAgentApiResponse();

            given(disableUserAgentUseCase.execute(any()))
                .willReturn(response);
            given(userAgentApiMapper.toResponse(response))
                .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(delete("/api/v1/user-agents/{userAgentId}", userAgentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userAgentId").value(userAgentId))
                .andDo(document("user-agent-disable",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("userAgentId").description("UserAgent ID")
                    ),
                    responseFields(
                        fieldWithPath("success").description("성공 여부"),
                        fieldWithPath("data").description("응답 데이터"),
                        fieldWithPath("data.userAgentId").description("UserAgent ID"),
                        fieldWithPath("data.userAgentString").description("UserAgent 문자열"),
                        fieldWithPath("data.tokenStatus").description("토큰 상태"),
                        fieldWithPath("data.remainingRequests").description("남은 요청 수"),
                        fieldWithPath("data.tokenIssuedAt").description("토큰 발급 시각"),
                        fieldWithPath("data.rateLimitResetAt").description("Rate Limit 리셋 시각").optional(),
                        fieldWithPath("data.createdAt").description("생성 시각"),
                        fieldWithPath("data.updatedAt").description("수정 시각"),
                        fieldWithPath("error").description("에러 정보").optional(),
                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                        fieldWithPath("requestId").description("요청 ID")
                    )
                ));
        }
    }

    // Helper Methods
    private UserAgentResponse createUserAgentResponse() {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgentResponse(
            1L,
            "Mozilla/5.0",
            com.ryuqq.crawlinghub.domain.useragent.TokenStatus.IDLE,
            80,
            now,
            null,
            now,
            now
        );
    }

    private UserAgentApiResponse createUserAgentApiResponse() {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgentApiResponse(
            1L,
            "Mozilla/5.0",
            "IDLE",
            80,
            now,
            null,
            now,
            now
        );
    }
}



