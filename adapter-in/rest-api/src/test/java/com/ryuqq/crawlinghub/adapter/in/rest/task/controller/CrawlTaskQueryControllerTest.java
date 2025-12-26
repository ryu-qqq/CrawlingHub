package com.ryuqq.crawlinghub.adapter.in.rest.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.GetCrawlTaskQuery;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.port.in.query.GetCrawlTaskUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.query.ListCrawlTasksUseCase;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
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
 *   <li>Path Variable Validation
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
    private ListCrawlTasksUseCase listCrawlTasksUseCase;
    private GetCrawlTaskUseCase getCrawlTaskUseCase;
    private CrawlTaskQueryApiMapper crawlTaskQueryApiMapper;

    @BeforeEach
    void setUp() {
        listCrawlTasksUseCase = mock(ListCrawlTasksUseCase.class);
        getCrawlTaskUseCase = mock(GetCrawlTaskUseCase.class);
        crawlTaskQueryApiMapper = mock(CrawlTaskQueryApiMapper.class);

        CrawlTaskQueryController controller =
                new CrawlTaskQueryController(
                        listCrawlTasksUseCase, getCrawlTaskUseCase, crawlTaskQueryApiMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .addPlaceholderValue("api.endpoints.base-v1", "/api/v1")
                        .setValidator(validator)
                        .build();
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/tasks - 크롤 태스크 목록 조회")
    class ListCrawlTasksTests {

        @Test
        @DisplayName("성공: 200 OK, 페이징 응답 반환")
        void listCrawlTasks_Success() throws Exception {
            // Given
            Long crawlSchedulerId = 100L;
            ListCrawlTasksQuery query =
                    new ListCrawlTasksQuery(crawlSchedulerId, null, null, 0, 20);

            Instant now = Instant.now();
            List<CrawlTaskResponse> useCaseContent =
                    List.of(
                            new CrawlTaskResponse(
                                    1L,
                                    crawlSchedulerId,
                                    10L,
                                    "https://example.com/products/1",
                                    CrawlTaskStatus.SUCCESS,
                                    CrawlTaskType.DETAIL,
                                    0,
                                    now),
                            new CrawlTaskResponse(
                                    2L,
                                    crawlSchedulerId,
                                    10L,
                                    "https://example.com/products/2",
                                    CrawlTaskStatus.RUNNING,
                                    CrawlTaskType.DETAIL,
                                    0,
                                    now));

            PageResponse<CrawlTaskResponse> useCasePageResponse =
                    new PageResponse<>(useCaseContent, 0, 20, 2, 1, true, true);

            List<CrawlTaskApiResponse> apiContent =
                    List.of(
                            new CrawlTaskApiResponse(
                                    1L,
                                    crawlSchedulerId,
                                    10L,
                                    "https://example.com/products/1",
                                    "SUCCESS",
                                    "DETAIL",
                                    0,
                                    now.toString()),
                            new CrawlTaskApiResponse(
                                    2L,
                                    crawlSchedulerId,
                                    10L,
                                    "https://example.com/products/2",
                                    "RUNNING",
                                    "DETAIL",
                                    0,
                                    now.toString()));

            PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                    new PageApiResponse<>(apiContent, 0, 20, 2, 1, true, true);

            given(crawlTaskQueryApiMapper.toQuery(any(SearchCrawlTasksApiRequest.class)))
                    .willReturn(query);
            given(listCrawlTasksUseCase.execute(any(ListCrawlTasksQuery.class)))
                    .willReturn(useCasePageResponse);
            given(crawlTaskQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerId", crawlSchedulerId.toString())
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
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
                    .andExpect(jsonPath("$.data.last").value(true))
                    .andExpect(jsonPath("$.error").isEmpty());

            // UseCase 호출 검증
            verify(crawlTaskQueryApiMapper).toQuery(any(SearchCrawlTasksApiRequest.class));
            verify(listCrawlTasksUseCase).execute(any(ListCrawlTasksQuery.class));
            verify(crawlTaskQueryApiMapper).toPageApiResponse(any());
        }

        @Test
        @DisplayName("성공: 필터 조건 적용 (status, taskType)")
        void listCrawlTasks_WithFilters_Success() throws Exception {
            // Given
            Long crawlSchedulerId = 100L;
            ListCrawlTasksQuery query =
                    new ListCrawlTasksQuery(
                            crawlSchedulerId, CrawlTaskStatus.FAILED, CrawlTaskType.META, 0, 20);

            Instant now = Instant.now();
            List<CrawlTaskResponse> useCaseContent =
                    List.of(
                            new CrawlTaskResponse(
                                    3L,
                                    crawlSchedulerId,
                                    10L,
                                    "https://example.com/meta",
                                    CrawlTaskStatus.FAILED,
                                    CrawlTaskType.META,
                                    2,
                                    now));

            PageResponse<CrawlTaskResponse> useCasePageResponse =
                    new PageResponse<>(useCaseContent, 0, 20, 1, 1, true, true);

            List<CrawlTaskApiResponse> apiContent =
                    List.of(
                            new CrawlTaskApiResponse(
                                    3L,
                                    crawlSchedulerId,
                                    10L,
                                    "https://example.com/meta",
                                    "FAILED",
                                    "META",
                                    2,
                                    now.toString()));

            PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                    new PageApiResponse<>(apiContent, 0, 20, 1, 1, true, true);

            given(crawlTaskQueryApiMapper.toQuery(any(SearchCrawlTasksApiRequest.class)))
                    .willReturn(query);
            given(listCrawlTasksUseCase.execute(any(ListCrawlTasksQuery.class)))
                    .willReturn(useCasePageResponse);
            given(crawlTaskQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerId", crawlSchedulerId.toString())
                                    .param("status", "FAILED")
                                    .param("taskType", "META")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].status").value("FAILED"))
                    .andExpect(jsonPath("$.data.content[0].taskType").value("META"))
                    .andExpect(jsonPath("$.data.content[0].retryCount").value(2))
                    .andExpect(jsonPath("$.error").isEmpty());

            // UseCase 호출 검증
            verify(listCrawlTasksUseCase).execute(any(ListCrawlTasksQuery.class));
        }

        @Test
        @DisplayName("성공: 페이징 파라미터 적용")
        void listCrawlTasks_WithPaging_Success() throws Exception {
            // Given
            Long crawlSchedulerId = 100L;
            ListCrawlTasksQuery query =
                    new ListCrawlTasksQuery(crawlSchedulerId, null, null, 2, 10);

            PageResponse<CrawlTaskResponse> useCasePageResponse =
                    new PageResponse<>(List.of(), 2, 10, 25, 3, false, true);

            PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                    new PageApiResponse<>(List.of(), 2, 10, 25, 3, false, true);

            given(crawlTaskQueryApiMapper.toQuery(any(SearchCrawlTasksApiRequest.class)))
                    .willReturn(query);
            given(listCrawlTasksUseCase.execute(any(ListCrawlTasksQuery.class)))
                    .willReturn(useCasePageResponse);
            given(crawlTaskQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerId", crawlSchedulerId.toString())
                                    .param("page", "2")
                                    .param("size", "10")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.page").value(2))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.totalElements").value(25))
                    .andExpect(jsonPath("$.data.totalPages").value(3))
                    .andExpect(jsonPath("$.data.first").value(false))
                    .andExpect(jsonPath("$.data.last").value(true))
                    .andExpect(jsonPath("$.error").isEmpty());
        }

        @Test
        @DisplayName("실패: 잘못된 status 값인 경우 400 Bad Request")
        void listCrawlTasks_InvalidStatus_BadRequest() throws Exception {
            // Given
            Long crawlSchedulerId = 100L;

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerId", crawlSchedulerId.toString())
                                    .param("status", "INVALID_STATUS")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 잘못된 taskType 값인 경우 400 Bad Request")
        void listCrawlTasks_InvalidTaskType_BadRequest() throws Exception {
            // Given
            Long crawlSchedulerId = 100L;

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerId", crawlSchedulerId.toString())
                                    .param("taskType", "INVALID_TYPE")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: page가 음수인 경우 400 Bad Request")
        void listCrawlTasks_NegativePage_BadRequest() throws Exception {
            // Given
            Long crawlSchedulerId = 100L;

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerId", crawlSchedulerId.toString())
                                    .param("page", "-1")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: size가 100 초과인 경우 400 Bad Request")
        void listCrawlTasks_SizeExceedsMax_BadRequest() throws Exception {
            // Given
            Long crawlSchedulerId = 100L;

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks")
                                    .param("crawlSchedulerId", crawlSchedulerId.toString())
                                    .param("size", "101")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/tasks/{id} - 크롤 태스크 상세 조회")
    class GetCrawlTaskTests {

        @Test
        @DisplayName("성공: 200 OK, 상세 정보 반환")
        void getCrawlTask_Success() throws Exception {
            // Given
            Long taskId = 1L;
            GetCrawlTaskQuery query = new GetCrawlTaskQuery(taskId);

            Instant now = Instant.now();
            Map<String, String> queryParams = Map.of("page", "1", "size", "100");

            CrawlTaskDetailResponse useCaseResponse =
                    new CrawlTaskDetailResponse(
                            taskId,
                            100L,
                            10L,
                            CrawlTaskStatus.SUCCESS,
                            CrawlTaskType.DETAIL,
                            0,
                            "https://example.com",
                            "/products",
                            queryParams,
                            "https://example.com/products?page=1&size=100",
                            now,
                            now);

            CrawlTaskDetailApiResponse apiResponse =
                    new CrawlTaskDetailApiResponse(
                            taskId,
                            100L,
                            10L,
                            "SUCCESS",
                            "DETAIL",
                            0,
                            "https://example.com",
                            "/products",
                            queryParams,
                            "https://example.com/products?page=1&size=100",
                            now.toString(),
                            now.toString());

            given(crawlTaskQueryApiMapper.toGetQuery(taskId)).willReturn(query);
            given(getCrawlTaskUseCase.execute(any(GetCrawlTaskQuery.class)))
                    .willReturn(useCaseResponse);
            given(crawlTaskQueryApiMapper.toDetailApiResponse(any(CrawlTaskDetailResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks/{id}", taskId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.crawlTaskId").value(taskId))
                    .andExpect(jsonPath("$.data.crawlSchedulerId").value(100))
                    .andExpect(jsonPath("$.data.sellerId").value(10))
                    .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.taskType").value("DETAIL"))
                    .andExpect(jsonPath("$.data.retryCount").value(0))
                    .andExpect(jsonPath("$.data.baseUrl").value("https://example.com"))
                    .andExpect(jsonPath("$.data.path").value("/products"))
                    .andExpect(jsonPath("$.data.queryParams.page").value("1"))
                    .andExpect(jsonPath("$.data.queryParams.size").value("100"))
                    .andExpect(
                            jsonPath("$.data.fullUrl")
                                    .value("https://example.com/products?page=1&size=100"))
                    .andExpect(jsonPath("$.data.createdAt").exists())
                    .andExpect(jsonPath("$.data.updatedAt").exists())
                    .andExpect(jsonPath("$.error").isEmpty());

            // UseCase 호출 검증
            verify(crawlTaskQueryApiMapper).toGetQuery(taskId);
            verify(getCrawlTaskUseCase).execute(any(GetCrawlTaskQuery.class));
            verify(crawlTaskQueryApiMapper).toDetailApiResponse(any(CrawlTaskDetailResponse.class));
        }

        @Test
        @DisplayName("성공: FAILED 상태 태스크 상세 조회")
        void getCrawlTask_FailedTask_Success() throws Exception {
            // Given
            Long taskId = 2L;
            GetCrawlTaskQuery query = new GetCrawlTaskQuery(taskId);

            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now();
            Map<String, String> queryParams = Map.of();

            CrawlTaskDetailResponse useCaseResponse =
                    new CrawlTaskDetailResponse(
                            taskId,
                            101L,
                            11L,
                            CrawlTaskStatus.FAILED,
                            CrawlTaskType.META,
                            3,
                            "https://example.com",
                            "/meta",
                            queryParams,
                            "https://example.com/meta",
                            createdAt,
                            updatedAt);

            CrawlTaskDetailApiResponse apiResponse =
                    new CrawlTaskDetailApiResponse(
                            taskId,
                            101L,
                            11L,
                            "FAILED",
                            "META",
                            3,
                            "https://example.com",
                            "/meta",
                            queryParams,
                            "https://example.com/meta",
                            createdAt.toString(),
                            updatedAt.toString());

            given(crawlTaskQueryApiMapper.toGetQuery(taskId)).willReturn(query);
            given(getCrawlTaskUseCase.execute(any(GetCrawlTaskQuery.class)))
                    .willReturn(useCaseResponse);
            given(crawlTaskQueryApiMapper.toDetailApiResponse(any(CrawlTaskDetailResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks/{id}", taskId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.crawlTaskId").value(taskId))
                    .andExpect(jsonPath("$.data.status").value("FAILED"))
                    .andExpect(jsonPath("$.data.taskType").value("META"))
                    .andExpect(jsonPath("$.data.retryCount").value(3))
                    .andExpect(jsonPath("$.error").isEmpty());

            // UseCase 호출 검증
            verify(getCrawlTaskUseCase).execute(any(GetCrawlTaskQuery.class));
        }

        /**
         * PathVariable @Positive validation은 AOP 기반 MethodValidationInterceptor가 필요합니다.
         * standaloneSetup에서는 지원되지 않으므로 통합 테스트에서 검증합니다.
         */
        @Test
        @DisplayName("실패: taskId가 0 이하인 경우 400 Bad Request")
        @org.junit.jupiter.api.Disabled("PathVariable validation은 통합 테스트에서 검증")
        void getCrawlTask_InvalidTaskId_BadRequest() throws Exception {
            // Given
            Long invalidTaskId = 0L;

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/tasks/{id}", invalidTaskId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
