package com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command.UpdateUserAgentStatusApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.RecoverUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UpdateUserAgentStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.WarmUpUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentStatusCommand;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RegisterUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.UpdateUserAgentMetadataUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.UpdateUserAgentStatusUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.WarmUpUserAgentUseCase;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * UserAgentCommandController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(UserAgentCommandController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("UserAgentCommandController REST Docs")
class UserAgentCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private RegisterUserAgentUseCase registerUserAgentUseCase;

    @MockitoBean private UpdateUserAgentMetadataUseCase updateUserAgentMetadataUseCase;

    @MockitoBean private RecoverUserAgentUseCase recoverUserAgentUseCase;

    @MockitoBean private UpdateUserAgentStatusUseCase updateUserAgentStatusUseCase;

    @MockitoBean private WarmUpUserAgentUseCase warmUpUserAgentUseCase;

    @MockitoBean private UserAgentApiMapper userAgentApiMapper;

    @Test
    @DisplayName("POST /api/v1/crawling/user-agents/recover - 정지된 UserAgent 복구 API 문서")
    void recoverUserAgents() throws Exception {
        // given
        int recoveredCount = 5;
        RecoverUserAgentApiResponse apiResponse =
                new RecoverUserAgentApiResponse(5, "5 user agents recovered successfully");

        given(recoverUserAgentUseCase.recoverAll()).willReturn(recoveredCount);
        given(userAgentApiMapper.toRecoverApiResponse(recoveredCount)).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/user-agents/recover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recoveredCount").value(5))
                .andExpect(jsonPath("$.data.message").value("5 user agents recovered successfully"))
                .andDo(
                        document(
                                "useragent-command/recover",
                                RestDocsSecuritySnippets.authorization("useragent:manage"),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.recoveredCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("복구된 UserAgent 수"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("결과 메시지"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("POST /api/v1/crawling/user-agents/recover - 복구할 UserAgent가 없는 경우 API 문서")
    void recoverUserAgents_noAgentsToRecover() throws Exception {
        // given
        int recoveredCount = 0;
        RecoverUserAgentApiResponse apiResponse =
                new RecoverUserAgentApiResponse(0, "No user agents to recover");

        given(recoverUserAgentUseCase.recoverAll()).willReturn(recoveredCount);
        given(userAgentApiMapper.toRecoverApiResponse(recoveredCount)).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/user-agents/recover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recoveredCount").value(0))
                .andExpect(jsonPath("$.data.message").value("No user agents to recover"))
                .andDo(
                        document(
                                "useragent-command/recover-no-agents",
                                RestDocsSecuritySnippets.authorization("useragent:manage"),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.recoveredCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("복구된 UserAgent 수 (0)"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("복구할 UserAgent가 없음을 나타내는 메시지"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("PATCH /api/v1/crawling/user-agents/status - UserAgent 상태 일괄 변경 API 문서")
    void updateUserAgentStatus() throws Exception {
        // given
        UpdateUserAgentStatusApiRequest apiRequest =
                new UpdateUserAgentStatusApiRequest(List.of(1L, 2L, 3L), UserAgentStatus.SUSPENDED);
        UpdateUserAgentStatusCommand command =
                new UpdateUserAgentStatusCommand(List.of(1L, 2L, 3L), UserAgentStatus.SUSPENDED);
        UpdateUserAgentStatusApiResponse apiResponse =
                UpdateUserAgentStatusApiResponse.of(3, "SUSPENDED");

        given(userAgentApiMapper.toCommand(any(UpdateUserAgentStatusApiRequest.class)))
                .willReturn(command);
        given(updateUserAgentStatusUseCase.execute(any(UpdateUserAgentStatusCommand.class)))
                .willReturn(3);
        given(userAgentApiMapper.toStatusUpdateApiResponse(3, "SUSPENDED")).willReturn(apiResponse);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/crawling/user-agents/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(apiRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedCount").value(3))
                .andExpect(
                        jsonPath("$.data.message")
                                .value("3 user agent(s) status updated to SUSPENDED"))
                .andDo(
                        document(
                                "useragent-command/update-status",
                                RestDocsSecuritySnippets.authorization("useragent:manage"),
                                requestFields(
                                        fieldWithPath("userAgentIds")
                                                .type(JsonFieldType.ARRAY)
                                                .description("변경할 UserAgent ID 목록"),
                                        fieldWithPath("status")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "변경할 상태 (AVAILABLE, SUSPENDED, BLOCKED)")),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.updatedCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("상태가 변경된 UserAgent 수"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("결과 메시지"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("PATCH /api/v1/crawling/user-agents/status - 단일 UserAgent 상태 변경 API 문서")
    void updateUserAgentStatus_single() throws Exception {
        // given
        UpdateUserAgentStatusApiRequest apiRequest =
                new UpdateUserAgentStatusApiRequest(List.of(1L), UserAgentStatus.BLOCKED);
        UpdateUserAgentStatusCommand command =
                new UpdateUserAgentStatusCommand(List.of(1L), UserAgentStatus.BLOCKED);
        UpdateUserAgentStatusApiResponse apiResponse =
                UpdateUserAgentStatusApiResponse.of(1, "BLOCKED");

        given(userAgentApiMapper.toCommand(any(UpdateUserAgentStatusApiRequest.class)))
                .willReturn(command);
        given(updateUserAgentStatusUseCase.execute(any(UpdateUserAgentStatusCommand.class)))
                .willReturn(1);
        given(userAgentApiMapper.toStatusUpdateApiResponse(1, "BLOCKED")).willReturn(apiResponse);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/crawling/user-agents/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(apiRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedCount").value(1))
                .andExpect(
                        jsonPath("$.data.message")
                                .value("1 user agent(s) status updated to BLOCKED"))
                .andDo(
                        document(
                                "useragent-command/update-status-single",
                                RestDocsSecuritySnippets.authorization("useragent:manage"),
                                requestFields(
                                        fieldWithPath("userAgentIds")
                                                .type(JsonFieldType.ARRAY)
                                                .description("변경할 UserAgent ID 목록 (단일)"),
                                        fieldWithPath("status")
                                                .type(JsonFieldType.STRING)
                                                .description("변경할 상태")),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.updatedCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("상태가 변경된 UserAgent 수"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("결과 메시지"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("POST /api/v1/crawling/user-agents/warmup - UserAgent Pool Warm-up API 문서")
    void warmUpUserAgents() throws Exception {
        // given
        int addedCount = 10;
        WarmUpUserAgentApiResponse apiResponse =
                new WarmUpUserAgentApiResponse(
                        10, "10 user agents added to pool (Lazy session issuance)");

        given(warmUpUserAgentUseCase.warmUp()).willReturn(addedCount);
        given(userAgentApiMapper.toWarmUpApiResponse(addedCount)).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/user-agents/warmup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.addedCount").value(10))
                .andExpect(
                        jsonPath("$.data.message")
                                .value("10 user agents added to pool (Lazy session issuance)"))
                .andDo(
                        document(
                                "useragent-command/warmup",
                                RestDocsSecuritySnippets.authorization("useragent:manage"),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.addedCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Pool에 추가된 UserAgent 수"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("결과 메시지"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("POST /api/v1/crawling/user-agents/warmup - Warm-up 대상이 없는 경우 API 문서")
    void warmUpUserAgents_noAgentsToWarmUp() throws Exception {
        // given
        int addedCount = 0;
        WarmUpUserAgentApiResponse apiResponse =
                new WarmUpUserAgentApiResponse(0, "No user agents to warm up");

        given(warmUpUserAgentUseCase.warmUp()).willReturn(addedCount);
        given(userAgentApiMapper.toWarmUpApiResponse(addedCount)).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/user-agents/warmup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.addedCount").value(0))
                .andExpect(jsonPath("$.data.message").value("No user agents to warm up"))
                .andDo(
                        document(
                                "useragent-command/warmup-no-agents",
                                RestDocsSecuritySnippets.authorization("useragent:manage"),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.addedCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Pool에 추가된 UserAgent 수 (0)"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("Warm-up 대상이 없음을 나타내는 메시지"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }
}
