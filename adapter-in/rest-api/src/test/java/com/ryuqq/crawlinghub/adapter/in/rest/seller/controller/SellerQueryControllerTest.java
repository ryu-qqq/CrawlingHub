package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.crawlinghub.adapter.in.rest.common.controller.GlobalExceptionHandler;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query.SearchSellersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailStatisticsApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailStatistics;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.query.GetSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.query.SearchSellersUseCase;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
 * SellerQueryController 단위 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>HTTP 요청/응답 매핑
 *   <li>Request DTO Validation
 *   <li>Response DTO 직렬화
 *   <li>HTTP Status Code
 *   <li>UseCase 호출 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SellerQueryController 단위 테스트")
@Tag("unit")
@Tag("adapter-rest")
class SellerQueryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private GetSellerUseCase getSellerUseCase;
    private SearchSellersUseCase searchSellersUseCase;
    private SellerQueryApiMapper sellerQueryApiMapper;

    @BeforeEach
    void setUp() {
        getSellerUseCase = mock(GetSellerUseCase.class);
        searchSellersUseCase = mock(SearchSellersUseCase.class);
        sellerQueryApiMapper = mock(SellerQueryApiMapper.class);

        SellerQueryController controller =
                new SellerQueryController(
                        getSellerUseCase, searchSellersUseCase, sellerQueryApiMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ErrorMapperRegistry errorMapperRegistry = new ErrorMapperRegistry(Collections.emptyList());
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler(errorMapperRegistry);

        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .addPlaceholderValue("api.endpoints.base-v1", "/api/v1")
                        .addPlaceholderValue("api.endpoints.seller.base", "/sellers")
                        .addPlaceholderValue("api.endpoints.seller.by-id", "/{id}")
                        .setValidator(validator)
                        .setControllerAdvice(exceptionHandler)
                        .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/sellers/{id} - 셀러 단건 조회")
    class GetSellerTests {

        @Test
        @DisplayName("성공: 200 OK, Response 구조 검증")
        void getSeller_Success() throws Exception {
            // Given
            Long sellerId = 1L;

            GetSellerQuery query = new GetSellerQuery(sellerId);

            SellerDetailResponse useCaseResponse =
                    new SellerDetailResponse(
                            sellerId,
                            "머스트잇 테스트 셀러",
                            "테스트 셀러",
                            true,
                            Instant.now(),
                            null,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            SellerDetailStatistics.empty());

            SellerDetailApiResponse apiResponse =
                    new SellerDetailApiResponse(
                            sellerId,
                            "머스트잇 테스트 셀러",
                            "테스트 셀러",
                            "ACTIVE",
                            Instant.now().toString(),
                            null,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            new SellerDetailStatisticsApiResponse(0L, 0L, 0L, 0.0));

            given(sellerQueryApiMapper.toQuery(any(Long.class))).willReturn(query);
            given(getSellerUseCase.execute(any(GetSellerQuery.class))).willReturn(useCaseResponse);
            given(sellerQueryApiMapper.toDetailApiResponse(any(SellerDetailResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/sellers/{id}", sellerId)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.sellerId").value(sellerId))
                    .andExpect(jsonPath("$.data.mustItSellerName").value("머스트잇 테스트 셀러"))
                    .andExpect(jsonPath("$.data.sellerName").value("테스트 셀러"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.createdAt").exists())
                    .andExpect(jsonPath("$.data.schedulers").isArray())
                    .andExpect(jsonPath("$.data.recentTasks").isArray())
                    .andExpect(jsonPath("$.data.statistics").exists());

            // UseCase 호출 검증
            verify(sellerQueryApiMapper).toQuery(any(Long.class));
            verify(getSellerUseCase).execute(any(GetSellerQuery.class));
            verify(sellerQueryApiMapper).toDetailApiResponse(any(SellerDetailResponse.class));
        }

        /**
         * PathVariable @Positive validation은 AOP 기반 MethodValidationInterceptor가 필요합니다.
         * standaloneSetup에서는 지원되지 않으므로 통합 테스트에서 검증합니다.
         */
        @Test
        @DisplayName("실패: sellerId가 0 이하인 경우 400 Bad Request")
        @org.junit.jupiter.api.Disabled("PathVariable validation은 통합 테스트에서 검증")
        void getSeller_InvalidSellerId_BadRequest() throws Exception {
            // Given
            Long invalidSellerId = 0L;

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/sellers/{id}", invalidSellerId)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        /**
         * PathVariable @Positive validation은 AOP 기반 MethodValidationInterceptor가 필요합니다.
         * standaloneSetup에서는 지원되지 않으므로 통합 테스트에서 검증합니다.
         */
        @Test
        @DisplayName("실패: sellerId가 음수인 경우 400 Bad Request")
        @org.junit.jupiter.api.Disabled("PathVariable validation은 통합 테스트에서 검증")
        void getSeller_NegativeSellerId_BadRequest() throws Exception {
            // Given
            Long negativeSellerId = -1L;

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/sellers/{id}", negativeSellerId)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/sellers - 셀러 목록 조회")
    class ListSellersTests {

        @Test
        @DisplayName("성공: 200 OK, 페이징 응답 구조 검증")
        void listSellers_Success() throws Exception {
            // Given
            SearchSellersApiRequest request =
                    new SearchSellersApiRequest(null, null, List.of("ACTIVE"), null, null, 0, 20);

            SearchSellersQuery query = new SearchSellersQuery(null, null, null, null, null, 0, 20);

            List<SellerSummaryResponse> content =
                    List.of(
                            new SellerSummaryResponse(
                                    1L,
                                    "머스트잇 셀러1",
                                    "셀러1",
                                    true,
                                    Instant.now(),
                                    Instant.now(),
                                    2,
                                    3,
                                    "COMPLETED",
                                    Instant.now(),
                                    50L),
                            new SellerSummaryResponse(
                                    2L,
                                    "머스트잇 셀러2",
                                    "셀러2",
                                    true,
                                    Instant.now(),
                                    Instant.now(),
                                    1,
                                    2,
                                    "RUNNING",
                                    Instant.now(),
                                    30L));

            PageResponse<SellerSummaryResponse> useCasePageResponse =
                    new PageResponse<>(content, 0, 20, 100L, 5, true, false);

            List<SellerSummaryApiResponse> apiContent =
                    List.of(
                            new SellerSummaryApiResponse(
                                    1L,
                                    "머스트잇 셀러1",
                                    "셀러1",
                                    "ACTIVE",
                                    "2025-01-15 10:30:00",
                                    "2025-01-15 10:30:00"),
                            new SellerSummaryApiResponse(
                                    2L,
                                    "머스트잇 셀러2",
                                    "셀러2",
                                    "ACTIVE",
                                    "2025-01-15 10:30:00",
                                    "2025-01-15 10:30:00"));

            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    new PageApiResponse<>(apiContent, 0, 20, 100L, 5, true, false);

            given(sellerQueryApiMapper.toQuery(any(SearchSellersApiRequest.class)))
                    .willReturn(query);
            given(searchSellersUseCase.execute(any(SearchSellersQuery.class)))
                    .willReturn(useCasePageResponse);
            given(sellerQueryApiMapper.toPageApiResponse(any(PageResponse.class)))
                    .willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/sellers")
                                    .param("status", "ACTIVE")
                                    .param("page", "0")
                                    .param("size", "20")
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].sellerId").value(1))
                    .andExpect(jsonPath("$.data.content[0].mustItSellerName").value("머스트잇 셀러1"))
                    .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.content[1].sellerId").value(2))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(20))
                    .andExpect(jsonPath("$.data.totalElements").value(100))
                    .andExpect(jsonPath("$.data.totalPages").value(5))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(false));

            // UseCase 호출 검증
            verify(sellerQueryApiMapper).toQuery(any(SearchSellersApiRequest.class));
            verify(searchSellersUseCase).execute(any(SearchSellersQuery.class));
            verify(sellerQueryApiMapper).toPageApiResponse(any(PageResponse.class));
        }

        @Test
        @DisplayName("성공: 기본 페이징 파라미터 (page=0, size=20)")
        void listSellers_DefaultPagination() throws Exception {
            // Given
            SearchSellersQuery query = new SearchSellersQuery(null, null, null, null, null, 0, 20);

            List<SellerSummaryResponse> content =
                    List.of(
                            new SellerSummaryResponse(
                                    1L,
                                    "머스트잇 셀러1",
                                    "셀러1",
                                    true,
                                    Instant.now(),
                                    Instant.now(),
                                    2,
                                    3,
                                    "COMPLETED",
                                    Instant.now(),
                                    50L));

            PageResponse<SellerSummaryResponse> useCasePageResponse =
                    new PageResponse<>(content, 0, 20, 1L, 1, true, true);

            List<SellerSummaryApiResponse> apiContent =
                    List.of(
                            new SellerSummaryApiResponse(
                                    1L,
                                    "머스트잇 셀러1",
                                    "셀러1",
                                    "ACTIVE",
                                    "2025-01-15 10:30:00",
                                    "2025-01-15 10:30:00"));

            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    new PageApiResponse<>(apiContent, 0, 20, 1L, 1, true, true);

            given(sellerQueryApiMapper.toQuery(any(SearchSellersApiRequest.class)))
                    .willReturn(query);
            given(searchSellersUseCase.execute(any(SearchSellersQuery.class)))
                    .willReturn(useCasePageResponse);
            given(sellerQueryApiMapper.toPageApiResponse(any(PageResponse.class)))
                    .willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/crawling/sellers").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(20));

            // UseCase 호출 검증
            verify(searchSellersUseCase).execute(any(SearchSellersQuery.class));
        }

        @Test
        @DisplayName("성공: INACTIVE 상태 필터링")
        void listSellers_FilterByInactiveStatus() throws Exception {
            // Given
            SearchSellersQuery query = new SearchSellersQuery(null, null, null, null, null, 0, 20);

            List<SellerSummaryResponse> content =
                    List.of(
                            new SellerSummaryResponse(
                                    3L,
                                    "머스트잇 셀러3",
                                    "셀러3",
                                    false,
                                    Instant.now(),
                                    null,
                                    0,
                                    1,
                                    null,
                                    null,
                                    0L));

            PageResponse<SellerSummaryResponse> useCasePageResponse =
                    new PageResponse<>(content, 0, 20, 1L, 1, true, true);

            List<SellerSummaryApiResponse> apiContent =
                    List.of(
                            new SellerSummaryApiResponse(
                                    3L,
                                    "머스트잇 셀러3",
                                    "셀러3",
                                    "INACTIVE",
                                    "2025-01-15 10:30:00",
                                    null));

            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    new PageApiResponse<>(apiContent, 0, 20, 1L, 1, true, true);

            given(sellerQueryApiMapper.toQuery(any(SearchSellersApiRequest.class)))
                    .willReturn(query);
            given(searchSellersUseCase.execute(any(SearchSellersQuery.class)))
                    .willReturn(useCasePageResponse);
            given(sellerQueryApiMapper.toPageApiResponse(any(PageResponse.class)))
                    .willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/sellers")
                                    .param("status", "INACTIVE")
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].status").value("INACTIVE"));

            // UseCase 호출 검증
            verify(searchSellersUseCase).execute(any(SearchSellersQuery.class));
        }

        @Test
        @DisplayName("성공: 빈 결과 (content가 빈 배열)")
        void listSellers_EmptyResult() throws Exception {
            // Given
            SearchSellersQuery query = new SearchSellersQuery(null, null, null, null, null, 0, 20);

            PageResponse<SellerSummaryResponse> useCasePageResponse =
                    new PageResponse<>(List.of(), 0, 20, 0L, 0, true, true);

            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    new PageApiResponse<>(List.of(), 0, 20, 0L, 0, true, true);

            given(sellerQueryApiMapper.toQuery(any(SearchSellersApiRequest.class)))
                    .willReturn(query);
            given(searchSellersUseCase.execute(any(SearchSellersQuery.class)))
                    .willReturn(useCasePageResponse);
            given(sellerQueryApiMapper.toPageApiResponse(any(PageResponse.class)))
                    .willReturn(apiPageResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/crawling/sellers").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));

            // UseCase 호출 검증
            verify(searchSellersUseCase).execute(any(SearchSellersQuery.class));
        }

        @Test
        @DisplayName("실패: 잘못된 status 값 (400 Bad Request)")
        void listSellers_InvalidStatus_BadRequest() throws Exception {
            // Given
            given(sellerQueryApiMapper.toQuery(any(SearchSellersApiRequest.class)))
                    .willThrow(new IllegalArgumentException("Invalid status: INVALID_STATUS"));

            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/sellers")
                                    .param("status", "INVALID_STATUS")
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: page가 음수인 경우 (400 Bad Request)")
        void listSellers_NegativePage_BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/sellers")
                                    .param("page", "-1")
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: size가 0 이하인 경우 (400 Bad Request)")
        void listSellers_InvalidSize_BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/sellers")
                                    .param("size", "0")
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: size가 100을 초과하는 경우 (400 Bad Request)")
        void listSellers_SizeTooLarge_BadRequest() throws Exception {
            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/sellers")
                                    .param("size", "101")
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
