package com.ryuqq.crawlinghub.adapter.in.rest.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.controller.GlobalExceptionHandler;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskQueryApiMapper;
import com.ryuqq.crawlinghub.application.task.dto.query.CrawlTaskSearchParams;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.application.task.port.in.query.SearchCrawlTaskByOffsetUseCase;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
 * CrawlTaskQueryController 단위 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>HTTP 요청/응답 매핑
 *   <li>Query Parameter Validation
 *   <li>Response DTO 직렬화
 *   <li>HTTP Status Code
 *   <li>UseCase 호출 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskQueryController 단위 테스트")
@Tag("unit")
@Tag("adapter-rest")
class CrawlTaskQueryControllerTest {

    private MockMvc mockMvc;
    private SearchCrawlTaskByOffsetUseCase searchCrawlTaskByOffsetUseCase;
    private CrawlTaskQueryApiMapper crawlTaskQueryApiMapper;

    @BeforeEach
    void setUp() {
        searchCrawlTaskByOffsetUseCase = mock(SearchCrawlTaskByOffsetUseCase.class);
        crawlTaskQueryApiMapper = mock(CrawlTaskQueryApiMapper.class);

        CrawlTaskQueryController controller =
                new CrawlTaskQueryController(
                        searchCrawlTaskByOffsetUseCase, crawlTaskQueryApiMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ErrorMapperRegistry errorMapperRegistry = new ErrorMapperRegistry(Collections.emptyList());
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler(errorMapperRegistry);

        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .addPlaceholderValue("api.endpoints.base-v1", "/api/v1")
                        .setValidator(validator)
                        .setControllerAdvice(exceptionHandler)
                        .build();
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/tasks - 크롤 태스크 목록 조회")
    class ListCrawlTasksTests {

        @Test
        @DisplayName("성공: 200 OK, 페이징 응답 반환")
        void listCrawlTasks_Success() throws Exception {
            // Given
            Instant now = Instant.now();
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(List.of(100L), null, null, null, null, null, 0, 20);

            List<CrawlTaskResult> results =
                    List.of(
                            new CrawlTaskResult(
                                    1L,
                                    100L,
                                    10L,
                                    "https://example.com/products/1",
                                    "https://example.com",
                                    "/products/1",
                                    Map.of(),
                                    "SUCCESS",
                                    "DETAIL",
                                    0,
                                    now,
                                    now),
                            new CrawlTaskResult(
                                    2L,
                                    100L,
                                    10L,
                                    "https://example.com/products/2",
                                    "https://example.com",
                                    "/products/2",
                                    Map.of(),
                                    "RUNNING",
                                    "DETAIL",
                                    0,
                                    now,
                                    now));

            CrawlTaskPageResult pageResult =
                    CrawlTaskPageResult.of(results, PageMeta.of(0, 20, 2L));

            List<CrawlTaskApiResponse> apiContent =
                    List.of(
                            new CrawlTaskApiResponse(
                                    1L,
                                    100L,
                                    10L,
                                    "https://example.com/products/1",
                                    "https://example.com",
                                    "/products/1",
                                    Map.of(),
                                    "SUCCESS",
                                    "DETAIL",
                                    0,
                                    now.toString(),
                                    now.toString()),
                            new CrawlTaskApiResponse(
                                    2L,
                                    100L,
                                    10L,
                                    "https://example.com/products/2",
                                    "https://example.com",
                                    "/products/2",
                                    Map.of(),
                                    "RUNNING",
                                    "DETAIL",
                                    0,
                                    now.toString(),
                                    now.toString()));

            PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                    new PageApiResponse<>(apiContent, 0, 20, 2, 1, true, true);

            given(crawlTaskQueryApiMapper.toSearchParams(any(SearchCrawlTasksApiRequest.class)))
                    .willReturn(params);
            given(searchCrawlTaskByOffsetUseCase.execute(any(CrawlTaskSearchParams.class)))
                    .willReturn(pageResult);
            given(crawlTaskQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerIds", "100")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].crawlTaskId").value(1))
                    .andExpect(jsonPath("$.data.content[0].status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content[1].crawlTaskId").value(2))
                    .andExpect(jsonPath("$.data.content[1].status").value("RUNNING"))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(20))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true));

            // UseCase 호출 검증
            verify(crawlTaskQueryApiMapper).toSearchParams(any(SearchCrawlTasksApiRequest.class));
            verify(searchCrawlTaskByOffsetUseCase).execute(any(CrawlTaskSearchParams.class));
            verify(crawlTaskQueryApiMapper).toPageApiResponse(any());
        }

        @Test
        @DisplayName("성공: 필터 조건 적용 (status, taskType)")
        void listCrawlTasks_WithFilters_Success() throws Exception {
            // Given
            Instant now = Instant.now();
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(
                            List.of(100L),
                            null,
                            List.of("FAILED"),
                            List.of("META"),
                            null,
                            null,
                            0,
                            20);

            List<CrawlTaskResult> results =
                    List.of(
                            new CrawlTaskResult(
                                    3L,
                                    100L,
                                    10L,
                                    "https://example.com/meta",
                                    "https://example.com",
                                    "/meta",
                                    Map.of(),
                                    "FAILED",
                                    "META",
                                    2,
                                    now,
                                    now));

            CrawlTaskPageResult pageResult =
                    CrawlTaskPageResult.of(results, PageMeta.of(0, 20, 1L));

            List<CrawlTaskApiResponse> apiContent =
                    List.of(
                            new CrawlTaskApiResponse(
                                    3L,
                                    100L,
                                    10L,
                                    "https://example.com/meta",
                                    "https://example.com",
                                    "/meta",
                                    Map.of(),
                                    "FAILED",
                                    "META",
                                    2,
                                    now.toString(),
                                    now.toString()));

            PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                    new PageApiResponse<>(apiContent, 0, 20, 1, 1, true, true);

            given(crawlTaskQueryApiMapper.toSearchParams(any(SearchCrawlTasksApiRequest.class)))
                    .willReturn(params);
            given(searchCrawlTaskByOffsetUseCase.execute(any(CrawlTaskSearchParams.class)))
                    .willReturn(pageResult);
            given(crawlTaskQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerIds", "100")
                                    .param("statuses", "FAILED")
                                    .param("taskTypes", "META")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].status").value("FAILED"))
                    .andExpect(jsonPath("$.data.content[0].taskType").value("META"))
                    .andExpect(jsonPath("$.data.content[0].retryCount").value(2));

            // UseCase 호출 검증
            verify(searchCrawlTaskByOffsetUseCase).execute(any(CrawlTaskSearchParams.class));
        }

        @Test
        @DisplayName("성공: 페이징 파라미터 적용")
        void listCrawlTasks_WithPaging_Success() throws Exception {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(List.of(100L), null, null, null, null, null, 2, 10);

            CrawlTaskPageResult pageResult =
                    CrawlTaskPageResult.of(List.of(), PageMeta.of(2, 10, 25L));

            PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                    new PageApiResponse<>(List.of(), 2, 10, 25, 3, false, true);

            given(crawlTaskQueryApiMapper.toSearchParams(any(SearchCrawlTasksApiRequest.class)))
                    .willReturn(params);
            given(searchCrawlTaskByOffsetUseCase.execute(any(CrawlTaskSearchParams.class)))
                    .willReturn(pageResult);
            given(crawlTaskQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerIds", "100")
                                    .param("page", "2")
                                    .param("size", "10")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.page").value(2))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.totalElements").value(25))
                    .andExpect(jsonPath("$.data.totalPages").value(3))
                    .andExpect(jsonPath("$.data.first").value(false))
                    .andExpect(jsonPath("$.data.last").value(true));
        }

        @Test
        @DisplayName("실패: 잘못된 status 값인 경우 400 Bad Request")
        void listCrawlTasks_InvalidStatus_BadRequest() throws Exception {
            // Given
            given(crawlTaskQueryApiMapper.toSearchParams(any(SearchCrawlTasksApiRequest.class)))
                    .willThrow(new IllegalArgumentException("Invalid status: INVALID_STATUS"));

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("statuses", "INVALID_STATUS")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 잘못된 taskType 값인 경우 400 Bad Request")
        void listCrawlTasks_InvalidTaskType_BadRequest() throws Exception {
            // Given
            given(crawlTaskQueryApiMapper.toSearchParams(any(SearchCrawlTasksApiRequest.class)))
                    .willThrow(new IllegalArgumentException("Invalid taskType: INVALID_TYPE"));

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("taskTypes", "INVALID_TYPE")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: page가 음수인 경우 400 Bad Request")
        void listCrawlTasks_NegativePage_BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("page", "-1")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: size가 100 초과인 경우 400 Bad Request")
        void listCrawlTasks_SizeExceedsMax_BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("size", "101")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
