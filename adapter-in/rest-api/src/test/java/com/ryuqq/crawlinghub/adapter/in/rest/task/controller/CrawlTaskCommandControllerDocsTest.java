package com.ryuqq.crawlinghub.adapter.in.rest.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskCommandApiMapper;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.port.in.command.RetryCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * CrawlTaskCommandController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(CrawlTaskCommandController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("CrawlTaskCommandController REST Docs")
class CrawlTaskCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private RetryCrawlTaskUseCase retryCrawlTaskUseCase;

    @MockitoBean private CrawlTaskCommandApiMapper crawlTaskCommandApiMapper;

    @Test
    @DisplayName("POST /api/v1/crawling/tasks/{id}/retry - 크롤 태스크 재시도 API 문서")
    void retryCrawlTask() throws Exception {
        // given
        Long taskId = 100L;

        CrawlTaskResponse useCaseResponse =
                new CrawlTaskResponse(
                        100L,
                        1L,
                        1L,
                        "https://api.example.com/sellers/1/meta",
                        CrawlTaskStatus.RETRY,
                        CrawlTaskType.META,
                        1,
                        Instant.parse("2025-11-20T10:30:00Z"));

        given(crawlTaskCommandApiMapper.toRetryCommand(any())).willReturn(null);
        given(retryCrawlTaskUseCase.retry(any())).willReturn(useCaseResponse);
        given(crawlTaskCommandApiMapper.toApiResponse(any()))
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
        mockMvc.perform(post("/api/v1/crawling/tasks/{id}/retry", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawlTaskId").value(100))
                .andExpect(jsonPath("$.data.status").value("RETRY"))
                .andExpect(jsonPath("$.data.retryCount").value(1))
                .andDo(
                        document(
                                "task-command/retry",
                                RestDocsSecuritySnippets.authorization("task:update"),
                                pathParameters(
                                        parameterWithName("id").description("크롤 태스크 ID (양수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 태스크 ID"),
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
                                                .description("상태 (RETRY)"),
                                        fieldWithPath("data.taskType")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "태스크 유형 (META/MINI_SHOP/DETAIL/OPTION)"),
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
