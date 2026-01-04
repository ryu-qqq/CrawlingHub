package com.ryuqq.crawlinghub.adapter.in.rest.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskCommandApiMapper;
import com.ryuqq.crawlinghub.application.task.dto.command.RetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.port.in.command.RetryCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * CrawlTaskCommandController 단위 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>HTTP 요청/응답 매핑
 *   <li>Path Variable Validation
 *   <li>Response DTO 직렬화
 *   <li>HTTP Status Code
 *   <li>UseCase 호출 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskCommandController 단위 테스트")
@Tag("unit")
@Tag("adapter-rest")
class CrawlTaskCommandControllerTest {

    private MockMvc mockMvc;
    private RetryCrawlTaskUseCase retryCrawlTaskUseCase;
    private CrawlTaskCommandApiMapper crawlTaskCommandApiMapper;

    @BeforeEach
    void setUp() {
        retryCrawlTaskUseCase = mock(RetryCrawlTaskUseCase.class);
        crawlTaskCommandApiMapper = mock(CrawlTaskCommandApiMapper.class);

        CrawlTaskCommandController controller =
                new CrawlTaskCommandController(retryCrawlTaskUseCase, crawlTaskCommandApiMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .addPlaceholderValue("api.endpoints.base-v1", "/api/v1")
                        .build();
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/tasks/{id}/retry - 크롤 태스크 재시도")
    class RetryCrawlTaskTests {

        @Test
        @DisplayName("성공: 200 OK, 재시도된 태스크 정보 반환")
        void retryCrawlTask_Success() throws Exception {
            // Given
            Long taskId = 1L;
            RetryCrawlTaskCommand command = new RetryCrawlTaskCommand(taskId);

            CrawlTaskResponse useCaseResponse =
                    new CrawlTaskResponse(
                            taskId,
                            100L,
                            10L,
                            "https://example.com/products",
                            CrawlTaskStatus.RETRY,
                            CrawlTaskType.DETAIL,
                            1,
                            Instant.now(),
                            Instant.now());

            CrawlTaskApiResponse apiResponse =
                    new CrawlTaskApiResponse(
                            taskId,
                            100L,
                            10L,
                            "https://example.com/products",
                            "RETRY",
                            "DETAIL",
                            1,
                            "2025-01-15 10:30:00",
                            "2025-01-15 10:30:00");

            given(crawlTaskCommandApiMapper.toRetryCommand(taskId)).willReturn(command);
            given(retryCrawlTaskUseCase.retry(any(RetryCrawlTaskCommand.class)))
                    .willReturn(useCaseResponse);
            given(crawlTaskCommandApiMapper.toApiResponse(any(CrawlTaskResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/tasks/{id}/retry", taskId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.crawlTaskId").value(taskId))
                    .andExpect(jsonPath("$.data.crawlSchedulerId").value(100))
                    .andExpect(jsonPath("$.data.sellerId").value(10))
                    .andExpect(jsonPath("$.data.requestUrl").value("https://example.com/products"))
                    .andExpect(jsonPath("$.data.status").value("RETRY"))
                    .andExpect(jsonPath("$.data.taskType").value("DETAIL"))
                    .andExpect(jsonPath("$.data.retryCount").value(1))
                    .andExpect(jsonPath("$.data.createdAt").exists());

            // UseCase 호출 검증
            verify(crawlTaskCommandApiMapper).toRetryCommand(taskId);
            verify(retryCrawlTaskUseCase).retry(any(RetryCrawlTaskCommand.class));
            verify(crawlTaskCommandApiMapper).toApiResponse(any(CrawlTaskResponse.class));
        }

        @Test
        @DisplayName("성공: FAILED 상태의 태스크 재시도")
        void retryCrawlTask_FailedTaskRetry_Success() throws Exception {
            // Given
            Long taskId = 2L;
            RetryCrawlTaskCommand command = new RetryCrawlTaskCommand(taskId);

            CrawlTaskResponse useCaseResponse =
                    new CrawlTaskResponse(
                            taskId,
                            101L,
                            11L,
                            "https://example.com/minishop",
                            CrawlTaskStatus.RETRY,
                            CrawlTaskType.MINI_SHOP,
                            2,
                            Instant.now(),
                            Instant.now());

            CrawlTaskApiResponse apiResponse =
                    new CrawlTaskApiResponse(
                            taskId,
                            101L,
                            11L,
                            "https://example.com/minishop",
                            "RETRY",
                            "MINI_SHOP",
                            2,
                            "2025-01-15 10:30:00",
                            "2025-01-15 10:30:00");

            given(crawlTaskCommandApiMapper.toRetryCommand(taskId)).willReturn(command);
            given(retryCrawlTaskUseCase.retry(any(RetryCrawlTaskCommand.class)))
                    .willReturn(useCaseResponse);
            given(crawlTaskCommandApiMapper.toApiResponse(any(CrawlTaskResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/tasks/{id}/retry", taskId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.crawlTaskId").value(taskId))
                    .andExpect(jsonPath("$.data.status").value("RETRY"))
                    .andExpect(jsonPath("$.data.taskType").value("MINI_SHOP"))
                    .andExpect(jsonPath("$.data.retryCount").value(2));

            // UseCase 호출 검증
            verify(retryCrawlTaskUseCase).retry(any(RetryCrawlTaskCommand.class));
        }

        @Test
        @DisplayName("성공: TIMEOUT 상태의 태스크 재시도")
        void retryCrawlTask_TimeoutTaskRetry_Success() throws Exception {
            // Given
            Long taskId = 3L;
            RetryCrawlTaskCommand command = new RetryCrawlTaskCommand(taskId);

            CrawlTaskResponse useCaseResponse =
                    new CrawlTaskResponse(
                            taskId,
                            102L,
                            12L,
                            "https://example.com/meta",
                            CrawlTaskStatus.RETRY,
                            CrawlTaskType.META,
                            3,
                            Instant.now(),
                            Instant.now());

            CrawlTaskApiResponse apiResponse =
                    new CrawlTaskApiResponse(
                            taskId,
                            102L,
                            12L,
                            "https://example.com/meta",
                            "RETRY",
                            "META",
                            3,
                            "2025-01-15 10:30:00",
                            "2025-01-15 10:30:00");

            given(crawlTaskCommandApiMapper.toRetryCommand(taskId)).willReturn(command);
            given(retryCrawlTaskUseCase.retry(any(RetryCrawlTaskCommand.class)))
                    .willReturn(useCaseResponse);
            given(crawlTaskCommandApiMapper.toApiResponse(any(CrawlTaskResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/tasks/{id}/retry", taskId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.crawlTaskId").value(taskId))
                    .andExpect(jsonPath("$.data.status").value("RETRY"))
                    .andExpect(jsonPath("$.data.taskType").value("META"))
                    .andExpect(jsonPath("$.data.retryCount").value(3));

            // UseCase 호출 검증
            verify(retryCrawlTaskUseCase).retry(any(RetryCrawlTaskCommand.class));
        }

        /**
         * PathVariable @Positive validation은 AOP 기반 MethodValidationInterceptor가 필요합니다.
         * standaloneSetup에서는 지원되지 않으므로 통합 테스트에서 검증합니다.
         */
        @Test
        @DisplayName("실패: taskId가 0 이하인 경우 400 Bad Request")
        @org.junit.jupiter.api.Disabled("PathVariable validation은 통합 테스트에서 검증")
        void retryCrawlTask_InvalidTaskId_BadRequest() throws Exception {
            // Given
            Long invalidTaskId = 0L;

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/tasks/{id}/retry", invalidTaskId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(retryCrawlTaskUseCase, never()).retry(any());
        }
    }
}
