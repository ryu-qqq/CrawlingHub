package com.ryuqq.crawlinghub.adapter.in.rest.execution.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.query.SearchCrawlExecutionsApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response.CrawlExecutionApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response.CrawlExecutionDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.mapper.CrawlExecutionQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.dto.query.GetCrawlExecutionQuery;
import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;
import com.ryuqq.crawlinghub.application.execution.port.in.query.GetCrawlExecutionUseCase;
import com.ryuqq.crawlinghub.application.execution.port.in.query.ListCrawlExecutionsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CrawlExecution Query Controller
 *
 * <p>CrawlExecution 도메인의 조회 API를 제공합니다.
 *
 * <p><strong>제공하는 API</strong>:
 *
 * <ul>
 *   <li>GET /api/v1/crawling/executions - CrawlExecution 목록 조회 (필터링, 페이징)
 *   <li>GET /api/v1/crawling/executions/{id} - CrawlExecution 상세 조회
 * </ul>
 *
 * <p><strong>Controller 책임</strong>:
 *
 * <ul>
 *   <li>HTTP 요청 수신
 *   <li>API DTO → Application Query DTO 변환 (Mapper)
 *   <li>UseCase 실행 위임
 *   <li>Application Response → API Response 변환 (Mapper)
 *   <li>HTTP 응답 반환 (ResponseEntity)
 * </ul>
 *
 * <p><strong>금지 사항</strong>:
 *
 * <ul>
 *   <li>비즈니스 로직 (UseCase 책임)
 *   <li>Domain 객체 직접 생성/조작 (Domain Layer 책임)
 *   <li>Transaction 관리 (UseCase 책임)
 *   <li>예외 처리 (GlobalExceptionHandler 책임)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(ApiPaths.Executions.BASE)
@Validated
@Tag(name = "Execution", description = "크롤 실행 기록 API")
public class CrawlExecutionQueryController {

    private final ListCrawlExecutionsUseCase listCrawlExecutionsUseCase;
    private final GetCrawlExecutionUseCase getCrawlExecutionUseCase;
    private final CrawlExecutionQueryApiMapper mapper;

    /**
     * CrawlExecutionQueryController 생성자
     *
     * @param listCrawlExecutionsUseCase CrawlExecution 목록 조회 UseCase
     * @param getCrawlExecutionUseCase CrawlExecution 단건 조회 UseCase
     * @param mapper CrawlExecution API Mapper
     */
    public CrawlExecutionQueryController(
            ListCrawlExecutionsUseCase listCrawlExecutionsUseCase,
            GetCrawlExecutionUseCase getCrawlExecutionUseCase,
            CrawlExecutionQueryApiMapper mapper) {
        this.listCrawlExecutionsUseCase = listCrawlExecutionsUseCase;
        this.getCrawlExecutionUseCase = getCrawlExecutionUseCase;
        this.mapper = mapper;
    }

    /**
     * CrawlExecution 목록 조회
     *
     * <p><strong>API 명세</strong>:
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/executions
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>필터 조건</strong>:
     *
     * <ul>
     *   <li>crawlTaskId: 태스크 ID 필터 (optional)
     *   <li>crawlSchedulerId: 스케줄러 ID 필터 (optional)
     *   <li>sellerId: 셀러 ID 필터 (optional)
     *   <li>status: 상태 필터 (optional, RUNNING|SUCCESS|FAILED|TIMEOUT)
     *   <li>from: 조회 시작 시간 (optional, ISO-8601)
     *   <li>to: 조회 종료 시간 (optional, ISO-8601)
     * </ul>
     *
     * @param request 검색 조건 (Query Parameters)
     * @return CrawlExecution 목록 (200 OK)
     */
    @GetMapping
    @PreAuthorize("@access.hasPermission('execution:read')")
    @Operation(
            summary = "크롤 실행 기록 목록 조회",
            description = "크롤 실행 기록 목록을 필터링 및 페이징하여 조회합니다. execution:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(implementation = CrawlExecutionApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 파라미터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (execution:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<PageApiResponse<CrawlExecutionApiResponse>>>
            listCrawlExecutions(@ModelAttribute @Valid SearchCrawlExecutionsApiRequest request) {
        // 1. API Request → Application Query 변환 (Mapper)
        ListCrawlExecutionsQuery query = mapper.toQuery(request);

        // 2. UseCase 실행 (비즈니스 로직)
        PageResponse<CrawlExecutionResponse> useCaseResponse =
                listCrawlExecutionsUseCase.execute(query);

        // 3. Application Response → API Response 변환 (Mapper)
        PageApiResponse<CrawlExecutionApiResponse> apiResponse =
                mapper.toPageApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * CrawlExecution 상세 조회
     *
     * <p><strong>API 명세</strong>:
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/crawling/executions/{id}
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>에러 응답</strong>:
     *
     * <ul>
     *   <li>404: 해당 ID의 CrawlExecution이 없는 경우
     * </ul>
     *
     * @param id CrawlExecution ID
     * @return CrawlExecution 상세 정보 (200 OK)
     */
    @GetMapping(ApiPaths.Executions.BY_ID)
    @PreAuthorize("@access.hasPermission('execution:read')")
    @Operation(
            summary = "크롤 실행 기록 상세 조회",
            description = "크롤 실행 기록 ID로 상세 정보를 조회합니다. execution:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        CrawlExecutionDetailApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (execution:read 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "실행 기록을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<CrawlExecutionDetailApiResponse>> getCrawlExecution(
            @Parameter(description = "실행 기록 ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id) {
        // 1. PathVariable → Application Query 변환 (Mapper)
        GetCrawlExecutionQuery query = mapper.toGetQuery(id);

        // 2. UseCase 실행 (비즈니스 로직)
        CrawlExecutionDetailResponse useCaseResponse = getCrawlExecutionUseCase.execute(query);

        // 3. Application Response → API Response 변환 (Mapper)
        CrawlExecutionDetailApiResponse apiResponse = mapper.toDetailApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
