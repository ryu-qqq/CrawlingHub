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

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.RegisterCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.UpdateCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.UpdateSchedulerStatusApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerCommandApiMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RegisterCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.UpdateCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

    @MockitoBean private RegisterCrawlSchedulerUseCase registerCrawlSchedulerUseCase;

    @MockitoBean private UpdateCrawlSchedulerUseCase updateCrawlSchedulerUseCase;

    @MockitoBean private TriggerCrawlTaskUseCase triggerCrawlTaskUseCase;

    @MockitoBean private CrawlSchedulerCommandApiMapper crawlSchedulerCommandApiMapper;

    @Test
    @DisplayName("POST /api/v1/crawling/schedules - 크롤 스케줄러 등록 API 문서")
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
                        Instant.parse("2025-11-20T10:30:00Z"),
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
                                    resp.createdAt() != null ? resp.createdAt().toString() : null,
                                    resp.updatedAt() != null ? resp.updatedAt().toString() : null);
                        });

        // when & then
        mockMvc.perform(
                        post("/api/v1/crawling/schedules")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawlSchedulerId").value(1))
                .andDo(
                        document(
                                "schedule-command/register",
                                RestDocsSecuritySnippets.authorization("scheduler:create"),
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
    @DisplayName("PATCH /api/v1/crawling/schedules/{id} - 크롤 스케줄러 수정 API 문서")
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
                        Instant.parse("2025-11-20T10:30:00Z"),
                        Instant.parse("2025-11-20T11:00:00Z"));

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
                                    resp.createdAt() != null ? resp.createdAt().toString() : null,
                                    resp.updatedAt() != null ? resp.updatedAt().toString() : null);
                        });

        // when & then
        mockMvc.perform(
                        patch("/api/v1/crawling/schedules/{id}", schedulerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andDo(
                        document(
                                "schedule-command/update",
                                RestDocsSecuritySnippets.authorization("scheduler:update"),
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

    @Test
    @DisplayName("PATCH /api/v1/crawling/schedules/{id}/status - 크롤 스케줄러 상태 변경 API 문서")
    void updateSchedulerStatus() throws Exception {
        // given
        Long schedulerId = 1L;
        UpdateSchedulerStatusApiRequest request = new UpdateSchedulerStatusApiRequest(false);

        CrawlSchedulerResponse useCaseResponse =
                new CrawlSchedulerResponse(
                        1L,
                        1L,
                        "daily-crawl",
                        "0 0 9 * * ?",
                        SchedulerStatus.INACTIVE,
                        Instant.parse("2025-11-20T10:30:00Z"),
                        Instant.parse("2025-11-20T11:00:00Z"));

        given(crawlSchedulerCommandApiMapper.toStatusCommand(any(), any())).willReturn(null);
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
                                    resp.createdAt() != null ? resp.createdAt().toString() : null,
                                    resp.updatedAt() != null ? resp.updatedAt().toString() : null);
                        });

        // when & then
        mockMvc.perform(
                        patch("/api/v1/crawling/schedules/{id}/status", schedulerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andDo(
                        document(
                                "schedule-command/update-status",
                                RestDocsSecuritySnippets.authorization("scheduler:update"),
                                pathParameters(
                                        parameterWithName("id").description("크롤 스케줄러 ID (양수)")),
                                requestFields(
                                        fieldWithPath("active")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description(
                                                        "활성화 여부 (true=ACTIVE, false=INACTIVE,"
                                                                + " 필수)")),
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

    @Test
    @DisplayName("POST /api/v1/crawling/schedules/{id}/trigger - 크롤 스케줄러 수동 트리거 API 문서")
    void triggerScheduler() throws Exception {
        // given
        Long schedulerId = 1L;

        CrawlTaskResponse useCaseResponse =
                new CrawlTaskResponse(
                        100L,
                        1L,
                        1L,
                        "https://api.example.com/sellers/1/meta",
                        CrawlTaskStatus.WAITING,
                        CrawlTaskType.META,
                        0,
                        Instant.parse("2025-11-20T10:30:00Z"));

        given(triggerCrawlTaskUseCase.execute(any())).willReturn(useCaseResponse);
        given(crawlSchedulerCommandApiMapper.toTaskApiResponse(any()))
                .willAnswer(
                        invocation -> {
                            CrawlTaskResponse resp = invocation.getArgument(0);
                            return new CrawlTaskApiResponse(
                                    resp.crawlTaskId(),
                                    resp.crawlSchedulerId(),
                                    resp.sellerId(),
                                    resp.requestUrl(),
                                    resp.status().name(),
                                    resp.taskType().name(),
                                    resp.retryCount(),
                                    resp.createdAt() != null ? resp.createdAt().toString() : null);
                        });

        // when & then
        mockMvc.perform(post("/api/v1/crawling/schedules/{id}/trigger", schedulerId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawlTaskId").value(100))
                .andExpect(jsonPath("$.data.status").value("WAITING"))
                .andDo(
                        document(
                                "schedule-command/trigger",
                                RestDocsSecuritySnippets.authorization("scheduler:update"),
                                pathParameters(
                                        parameterWithName("id").description("크롤 스케줄러 ID (양수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("생성된 크롤 태스크 ID"),
                                        fieldWithPath("data.crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 스케줄러 ID"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.requestUrl")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 URL"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (PENDING)"),
                                        fieldWithPath("data.taskType")
                                                .type(JsonFieldType.STRING)
                                                .description("태스크 유형 (META)"),
                                        fieldWithPath("data.retryCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("재시도 횟수"),
                                        fieldWithPath("data.createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 일시"),
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
