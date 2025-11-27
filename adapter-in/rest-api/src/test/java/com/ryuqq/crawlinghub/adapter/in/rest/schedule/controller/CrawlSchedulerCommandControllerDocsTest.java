package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.RegisterCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.UpdateCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerCommandApiMapper;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RegisterCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.UpdateCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;

/**
 * CrawlSchedulerCommandController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(CrawlSchedulerCommandController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("CrawlSchedulerCommandController REST Docs")
class CrawlSchedulerCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean
    private RegisterCrawlSchedulerUseCase registerCrawlSchedulerUseCase;

    @MockitoBean
    private UpdateCrawlSchedulerUseCase updateCrawlSchedulerUseCase;

    @MockitoBean
    private CrawlSchedulerCommandApiMapper crawlSchedulerCommandApiMapper;

    @Test
    @DisplayName("POST /api/v1/schedules - 크롤 스케줄러 등록 API 문서")
    void registerCrawlScheduler() throws Exception {
        // given
        RegisterCrawlSchedulerApiRequest request =
                new RegisterCrawlSchedulerApiRequest(1L, "daily-crawl", "0 0 9 * * ?");

        CrawlSchedulerResponse useCaseResponse =
                new CrawlSchedulerResponse(
                        1L,
                        1L,
                        "daily-crawl",
                        "0 0 9 * * ?",
                        SchedulerStatus.ACTIVE,
                        LocalDateTime.of(2025, 11, 20, 10, 30, 0),
                        null);

        given(crawlSchedulerCommandApiMapper.toCommand(any())).willReturn(null);
        given(registerCrawlSchedulerUseCase.register(any())).willReturn(useCaseResponse);
        given(crawlSchedulerCommandApiMapper.toApiResponse(any()))
                .willAnswer(
                        invocation -> {
                            CrawlSchedulerResponse resp = invocation.getArgument(0);
                            return new com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response
                                    .CrawlSchedulerApiResponse(
                                    resp.crawlSchedulerId(),
                                    resp.sellerId(),
                                    resp.schedulerName(),
                                    resp.cronExpression(),
                                    resp.status().name(),
                                    resp.createdAt(),
                                    resp.updatedAt());
                        });

        // when & then
        mockMvc.perform(
                        post("/api/v1/schedules")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawlSchedulerId").value(1))
                .andDo(
                        document(
                                "schedule-command/register",
                                requestFields(
                                        fieldWithPath("sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID (양수, 필수)"),
                                        fieldWithPath("schedulerName")
                                                .type(JsonFieldType.STRING)
                                                .description("스케줄러 이름 (1-100자, 필수)"),
                                        fieldWithPath("cronExpression")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "크론 표현식 (AWS EventBridge 형식, 1-100자, 필수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 스케줄러 ID"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.schedulerName")
                                                .type(JsonFieldType.STRING)
                                                .description("스케줄러 이름"),
                                        fieldWithPath("data.cronExpression")
                                                .type(JsonFieldType.STRING)
                                                .description("크론 표현식"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (ACTIVE/INACTIVE)"),
                                        fieldWithPath("data.createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 일시"),
                                        fieldWithPath("data.updatedAt")
                                                .type(JsonFieldType.NULL)
                                                .description("수정 일시")
                                                .optional(),
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
    @DisplayName("PATCH /api/v1/schedules/{id} - 크롤 스케줄러 수정 API 문서")
    void updateCrawlScheduler() throws Exception {
        // given
        Long schedulerId = 1L;
        UpdateCrawlSchedulerApiRequest request =
                new UpdateCrawlSchedulerApiRequest("new-daily-crawl", "0 0 10 * * ?", false);

        CrawlSchedulerResponse useCaseResponse =
                new CrawlSchedulerResponse(
                        1L,
                        1L,
                        "new-daily-crawl",
                        "0 0 10 * * ?",
                        SchedulerStatus.INACTIVE,
                        LocalDateTime.of(2025, 11, 20, 10, 30, 0),
                        LocalDateTime.of(2025, 11, 20, 11, 0, 0));

        given(crawlSchedulerCommandApiMapper.toCommand(any(), any())).willReturn(null);
        given(updateCrawlSchedulerUseCase.update(any())).willReturn(useCaseResponse);
        given(crawlSchedulerCommandApiMapper.toApiResponse(any()))
                .willAnswer(
                        invocation -> {
                            CrawlSchedulerResponse resp = invocation.getArgument(0);
                            return new com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response
                                    .CrawlSchedulerApiResponse(
                                    resp.crawlSchedulerId(),
                                    resp.sellerId(),
                                    resp.schedulerName(),
                                    resp.cronExpression(),
                                    resp.status().name(),
                                    resp.createdAt(),
                                    resp.updatedAt());
                        });

        // when & then
        mockMvc.perform(
                        patch("/api/v1/schedules/{id}", schedulerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andDo(
                        document(
                                "schedule-command/update",
                                pathParameters(
                                        parameterWithName("id").description("크롤 스케줄러 ID (양수)")),
                                requestFields(
                                        fieldWithPath("schedulerName")
                                                .type(JsonFieldType.STRING)
                                                .description("스케줄러 이름 (1-100자, 선택)")
                                                .optional(),
                                        fieldWithPath("cronExpression")
                                                .type(JsonFieldType.STRING)
                                                .description("크론 표현식 (1-100자, 선택)")
                                                .optional(),
                                        fieldWithPath("active")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description(
                                                        "활성화 여부 (true=ACTIVE, false=INACTIVE, 선택)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 스케줄러 ID"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.schedulerName")
                                                .type(JsonFieldType.STRING)
                                                .description("스케줄러 이름"),
                                        fieldWithPath("data.cronExpression")
                                                .type(JsonFieldType.STRING)
                                                .description("크론 표현식"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (ACTIVE/INACTIVE)"),
                                        fieldWithPath("data.createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 일시"),
                                        fieldWithPath("data.updatedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("수정 일시"),
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
