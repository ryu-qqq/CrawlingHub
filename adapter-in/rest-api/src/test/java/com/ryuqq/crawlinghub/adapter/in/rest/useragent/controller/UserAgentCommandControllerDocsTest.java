package com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.RecoverUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverUserAgentUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;

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

    @MockBean
    private RecoverUserAgentUseCase recoverUserAgentUseCase;

    @MockBean
    private UserAgentApiMapper userAgentApiMapper;

    @Test
    @DisplayName("POST /api/v1/user-agents/recover - 정지된 UserAgent 복구 API 문서")
    void recoverUserAgents() throws Exception {
        // given
        int recoveredCount = 5;
        RecoverUserAgentApiResponse apiResponse =
                new RecoverUserAgentApiResponse(5, "5 user agents recovered successfully");

        given(recoverUserAgentUseCase.recoverAll()).willReturn(recoveredCount);
        given(userAgentApiMapper.toRecoverApiResponse(recoveredCount)).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/user-agents/recover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recoveredCount").value(5))
                .andExpect(jsonPath("$.data.message").value("5 user agents recovered successfully"))
                .andDo(
                        document(
                                "useragent-command/recover",
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.recoveredCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("복구된 UserAgent 수"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("결과 메시지"),
                                        fieldWithPath("error")
                                                .type(JsonFieldType.NULL)
                                                .description("에러 정보")
                                                .optional(),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("POST /api/v1/user-agents/recover - 복구할 UserAgent가 없는 경우 API 문서")
    void recoverUserAgents_noAgentsToRecover() throws Exception {
        // given
        int recoveredCount = 0;
        RecoverUserAgentApiResponse apiResponse =
                new RecoverUserAgentApiResponse(0, "No user agents to recover");

        given(recoverUserAgentUseCase.recoverAll()).willReturn(recoveredCount);
        given(userAgentApiMapper.toRecoverApiResponse(recoveredCount)).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/user-agents/recover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recoveredCount").value(0))
                .andExpect(jsonPath("$.data.message").value("No user agents to recover"))
                .andDo(
                        document(
                                "useragent-command/recover-no-agents",
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.recoveredCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("복구된 UserAgent 수 (0)"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("복구할 UserAgent가 없음을 나타내는 메시지"),
                                        fieldWithPath("error")
                                                .type(JsonFieldType.NULL)
                                                .description("에러 정보")
                                                .optional(),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }
}
