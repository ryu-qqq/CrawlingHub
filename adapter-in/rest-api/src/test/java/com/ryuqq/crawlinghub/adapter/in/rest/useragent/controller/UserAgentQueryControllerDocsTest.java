package com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentDetailApiResponse.PoolInfoApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse.HealthScoreStatsApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentDetailResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentDetailResponse.PoolInfo;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse.HealthScoreStats;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentSummaryResponse;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentByIdUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentPoolStatusUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentsUseCase;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.List;
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

    @MockitoBean private GetUserAgentByIdUseCase getUserAgentByIdUseCase;

    @MockitoBean private GetUserAgentsUseCase getUserAgentsUseCase;

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
                .andExpect(jsonPath("$.data.totalAgents").value(100))
                .andExpect(jsonPath("$.data.availableAgents").value(85))
                .andExpect(jsonPath("$.data.isHealthy").value(true))
                .andDo(
                        document(
                                "useragent-query/pool-status",
                                RestDocsSecuritySnippets.authorization("useragent:read"),
                                responseFields(
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
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/crawling/user-agents - UserAgent 목록 조회 API 문서")
    void getUserAgents() throws Exception {
        // given
        Instant now = Instant.parse("2025-11-20T10:30:00Z");

        List<UserAgentSummaryResponse> content =
                List.of(
                        UserAgentSummaryResponse.of(
                                1L,
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                                DeviceType.of("DESKTOP"),
                                UserAgentStatus.AVAILABLE,
                                95,
                                150,
                                now,
                                now,
                                now),
                        UserAgentSummaryResponse.of(
                                2L,
                                "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0)",
                                DeviceType.of("MOBILE"),
                                UserAgentStatus.SUSPENDED,
                                60,
                                80,
                                now,
                                now,
                                now));

        PageResponse<UserAgentSummaryResponse> useCaseResponse =
                new PageResponse<>(content, 0, 20, 2, 1, true, true);

        List<UserAgentSummaryApiResponse> apiContent =
                List.of(
                        new UserAgentSummaryApiResponse(
                                1L,
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                                DeviceType.of("DESKTOP"),
                                UserAgentStatus.AVAILABLE,
                                95,
                                150,
                                "2025-01-15 10:30:00",
                                "2025-01-15 10:30:00",
                                "2025-01-15 10:30:00"),
                        new UserAgentSummaryApiResponse(
                                2L,
                                "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0)",
                                DeviceType.of("MOBILE"),
                                UserAgentStatus.SUSPENDED,
                                60,
                                80,
                                "2025-01-15 10:30:00",
                                "2025-01-15 10:30:00",
                                "2025-01-15 10:30:00"));

        PageApiResponse<UserAgentSummaryApiResponse> apiResponse =
                new PageApiResponse<>(apiContent, 0, 20, 2, 1, true, true);

        given(getUserAgentsUseCase.execute(any())).willReturn(useCaseResponse);
        given(userAgentApiMapper.toSummaryApiResponse(any(UserAgentSummaryResponse.class)))
                .willReturn(apiContent.get(0), apiContent.get(1));

        // when & then
        mockMvc.perform(
                        get("/api/v1/crawling/user-agents")
                                .param("status", "AVAILABLE")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(
                        document(
                                "useragent-query/list",
                                RestDocsSecuritySnippets.authorization("useragent:read"),
                                queryParameters(
                                        parameterWithName("status")
                                                .description(
                                                        "UserAgent 상태 필터 (AVAILABLE, SUSPENDED,"
                                                                + " BLOCKED, 선택)")
                                                .optional(),
                                        parameterWithName("page")
                                                .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값: 20, 최대: 100)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.content")
                                                .type(JsonFieldType.ARRAY)
                                                .description("UserAgent 목록"),
                                        fieldWithPath("data.content[].id")
                                                .type(JsonFieldType.NUMBER)
                                                .description("UserAgent ID"),
                                        fieldWithPath("data.content[].userAgentValue")
                                                .type(JsonFieldType.STRING)
                                                .description("User-Agent 문자열 (최대 100자)"),
                                        fieldWithPath("data.content[].deviceType")
                                                .type(JsonFieldType.OBJECT)
                                                .description("디바이스 타입"),
                                        fieldWithPath("data.content[].deviceType.type")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "디바이스 타입 Enum 값 (DESKTOP, MOBILE, TABLET)"),
                                        fieldWithPath("data.content[].deviceType.typeName")
                                                .type(JsonFieldType.STRING)
                                                .description("디바이스 타입 이름"),
                                        fieldWithPath("data.content[].deviceType.displayName")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "디바이스 표시 이름 (Desktop, Mobile, Tablet)"),
                                        fieldWithPath("data.content[].deviceType.mobile")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("모바일 디바이스 여부"),
                                        fieldWithPath("data.content[].deviceType.tablet")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("태블릿 디바이스 여부"),
                                        fieldWithPath("data.content[].deviceType.desktop")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("데스크톱 디바이스 여부"),
                                        fieldWithPath("data.content[].status")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "현재 상태 (AVAILABLE, SUSPENDED, BLOCKED)"),
                                        fieldWithPath("data.content[].healthScore")
                                                .type(JsonFieldType.NUMBER)
                                                .description("건강 점수 (0-100)"),
                                        fieldWithPath("data.content[].requestsPerDay")
                                                .type(JsonFieldType.NUMBER)
                                                .description("일일 요청 수"),
                                        fieldWithPath("data.content[].lastUsedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("마지막 사용 시각"),
                                        fieldWithPath("data.content[].createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 시각"),
                                        fieldWithPath("data.content[].updatedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("수정 시각"),
                                        fieldWithPath("data.page")
                                                .type(JsonFieldType.NUMBER)
                                                .description("현재 페이지 번호"),
                                        fieldWithPath("data.size")
                                                .type(JsonFieldType.NUMBER)
                                                .description("페이지 크기"),
                                        fieldWithPath("data.totalElements")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 데이터 개수"),
                                        fieldWithPath("data.totalPages")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 페이지 수"),
                                        fieldWithPath("data.first")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("첫 페이지 여부"),
                                        fieldWithPath("data.last")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지 여부"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/crawling/user-agents/{userAgentId} - UserAgent 상세 조회 API 문서")
    void getUserAgentById() throws Exception {
        // given
        Long userAgentId = 1L;
        Instant now = Instant.parse("2025-11-20T10:30:00Z");
        Instant sessionExpiresAt = Instant.parse("2025-11-20T11:30:00Z");

        UserAgentDetailResponse useCaseResponse =
                UserAgentDetailResponse.withPoolInfo(
                        userAgentId,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like"
                                + " Gecko) Chrome/120.0.0.0 Safari/537.36",
                        DeviceType.of("DESKTOP"),
                        UserAgentStatus.AVAILABLE,
                        95,
                        150,
                        now,
                        now,
                        now,
                        PoolInfo.of(45, true, sessionExpiresAt));

        UserAgentDetailApiResponse apiResponse =
                new UserAgentDetailApiResponse(
                        userAgentId,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like"
                                + " Gecko) Chrome/120.0.0.0 Safari/537.36",
                        DeviceType.of("DESKTOP"),
                        UserAgentStatus.AVAILABLE,
                        95,
                        150,
                        now,
                        now,
                        now,
                        new PoolInfoApiResponse(true, 45, true, sessionExpiresAt));

        given(getUserAgentByIdUseCase.execute(userAgentId)).willReturn(useCaseResponse);
        given(userAgentApiMapper.toDetailApiResponse(useCaseResponse)).willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/crawling/user-agents/{userAgentId}", userAgentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userAgentId))
                .andExpect(jsonPath("$.data.healthScore").value(95))
                .andExpect(jsonPath("$.data.poolInfo.isInPool").value(true))
                .andDo(
                        document(
                                "useragent-query/detail",
                                RestDocsSecuritySnippets.authorization("useragent:read"),
                                pathParameters(
                                        parameterWithName("userAgentId")
                                                .description("조회할 UserAgent ID")),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.id")
                                                .type(JsonFieldType.NUMBER)
                                                .description("UserAgent ID"),
                                        fieldWithPath("data.userAgentValue")
                                                .type(JsonFieldType.STRING)
                                                .description("User-Agent 문자열 (전체)"),
                                        fieldWithPath("data.deviceType")
                                                .type(JsonFieldType.OBJECT)
                                                .description("디바이스 타입"),
                                        fieldWithPath("data.deviceType.type")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "디바이스 타입 Enum 값 (DESKTOP, MOBILE, TABLET)"),
                                        fieldWithPath("data.deviceType.typeName")
                                                .type(JsonFieldType.STRING)
                                                .description("디바이스 타입 이름"),
                                        fieldWithPath("data.deviceType.displayName")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "디바이스 표시 이름 (Desktop, Mobile, Tablet)"),
                                        fieldWithPath("data.deviceType.mobile")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("모바일 디바이스 여부"),
                                        fieldWithPath("data.deviceType.tablet")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("태블릿 디바이스 여부"),
                                        fieldWithPath("data.deviceType.desktop")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("데스크톱 디바이스 여부"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "현재 상태 (AVAILABLE, SUSPENDED, BLOCKED)"),
                                        fieldWithPath("data.healthScore")
                                                .type(JsonFieldType.NUMBER)
                                                .description("건강 점수 (0-100)"),
                                        fieldWithPath("data.requestsPerDay")
                                                .type(JsonFieldType.NUMBER)
                                                .description("일일 요청 수"),
                                        fieldWithPath("data.lastUsedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("마지막 사용 시각"),
                                        fieldWithPath("data.createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 시각"),
                                        fieldWithPath("data.updatedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("수정 시각"),
                                        fieldWithPath("data.poolInfo")
                                                .type(JsonFieldType.OBJECT)
                                                .description("Redis Pool 정보"),
                                        fieldWithPath("data.poolInfo.isInPool")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("Pool에 존재하는지 여부"),
                                        fieldWithPath("data.poolInfo.remainingTokens")
                                                .type(JsonFieldType.NUMBER)
                                                .description("남은 토큰 수"),
                                        fieldWithPath("data.poolInfo.hasValidSession")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("유효한 세션이 있는지 여부"),
                                        fieldWithPath("data.poolInfo.sessionExpiresAt")
                                                .type(JsonFieldType.STRING)
                                                .description("세션 만료 시각")
                                                .optional(),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }
}
