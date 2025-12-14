package com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse.HealthScoreStatsApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse.HealthScoreStats;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentPoolStatusUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * UserAgentQueryController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(UserAgentQueryController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("UserAgentQueryController REST Docs")
class UserAgentQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private GetUserAgentPoolStatusUseCase getUserAgentPoolStatusUseCase;

    @MockitoBean private UserAgentApiMapper userAgentApiMapper;

    @Test
    @DisplayName("GET /api/v1/crawling/user-agents/pool-status - UserAgent Pool 상태 조회 API 문서")
    void getPoolStatus() throws Exception {
        // given
        UserAgentPoolStatusResponse useCaseResponse =
                new UserAgentPoolStatusResponse(
                        100L, 85L, 15L, 85.0, new HealthScoreStats(75.5, 30, 100));

        UserAgentPoolStatusApiResponse apiResponse =
                new UserAgentPoolStatusApiResponse(
                        100L,
                        85L,
                        15L,
                        85.0,
                        new HealthScoreStatsApiResponse(75.5, 30, 100),
                        false,
                        true);

        given(getUserAgentPoolStatusUseCase.execute()).willReturn(useCaseResponse);
        given(userAgentApiMapper.toApiResponse(useCaseResponse)).willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/crawling/user-agents/pool-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAgents").value(100))
                .andExpect(jsonPath("$.data.availableAgents").value(85))
                .andExpect(jsonPath("$.data.isHealthy").value(true))
                .andDo(
                        document(
                                "useragent-query/pool-status",
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.totalAgents")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 UserAgent 수"),
                                        fieldWithPath("data.availableAgents")
                                                .type(JsonFieldType.NUMBER)
                                                .description("사용 가능한 UserAgent 수"),
                                        fieldWithPath("data.suspendedAgents")
                                                .type(JsonFieldType.NUMBER)
                                                .description("정지된 UserAgent 수"),
                                        fieldWithPath("data.availableRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("가용률 (%)"),
                                        fieldWithPath("data.healthScoreStats")
                                                .type(JsonFieldType.OBJECT)
                                                .description("Health Score 통계"),
                                        fieldWithPath("data.healthScoreStats.avg")
                                                .type(JsonFieldType.NUMBER)
                                                .description("평균 Health Score"),
                                        fieldWithPath("data.healthScoreStats.min")
                                                .type(JsonFieldType.NUMBER)
                                                .description("최소 Health Score"),
                                        fieldWithPath("data.healthScoreStats.max")
                                                .type(JsonFieldType.NUMBER)
                                                .description("최대 Health Score"),
                                        fieldWithPath("data.isCircuitBreakerOpen")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description(
                                                        "Circuit Breaker 열림 여부 (가용률 < 20%일 때"
                                                                + " true)"),
                                        fieldWithPath("data.isHealthy")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("Pool 상태 건강 여부 (가용률 >= 50%일 때 true)"),
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
