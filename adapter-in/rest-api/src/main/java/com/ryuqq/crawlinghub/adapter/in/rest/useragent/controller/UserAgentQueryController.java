package com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentSearchCriteria;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentSummaryResponse;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentPoolStatusUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentsUseCase;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserAgent Query Controller
 *
 * <p>UserAgent 도메인의 조회 API를 제공합니다.
 *
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/crawling/user-agents/pool-status - UserAgent Pool 상태 조회
 * </ul>
 *
 * <p><strong>Controller 책임:</strong>
 *
 * <ul>
 *   <li>HTTP 요청 수신
 *   <li>UseCase 실행 위임
 *   <li>UseCase DTO → API DTO 변환 (Mapper)
 *   <li>HTTP 응답 반환 (ResponseEntity)
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직 (UseCase 책임)
 *   <li>❌ Domain 객체 직접 생성/조작 (Domain Layer 책임)
 *   <li>❌ Transaction 관리 (UseCase 책임)
 *   <li>❌ 예외 처리 (GlobalExceptionHandler 책임)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(ApiPaths.UserAgents.BASE)
@Validated
@Tag(name = "UserAgent", description = "UserAgent 관리 API")
public class UserAgentQueryController {

    private final GetUserAgentPoolStatusUseCase getUserAgentPoolStatusUseCase;
    private final GetUserAgentsUseCase getUserAgentsUseCase;
    private final UserAgentApiMapper userAgentApiMapper;

    /**
     * UserAgentQueryController 생성자
     *
     * @param getUserAgentPoolStatusUseCase UserAgent Pool 상태 조회 UseCase
     * @param getUserAgentsUseCase UserAgent 목록 조회 UseCase
     * @param userAgentApiMapper UserAgent API Mapper
     */
    public UserAgentQueryController(
            GetUserAgentPoolStatusUseCase getUserAgentPoolStatusUseCase,
            GetUserAgentsUseCase getUserAgentsUseCase,
            UserAgentApiMapper userAgentApiMapper) {
        this.getUserAgentPoolStatusUseCase = getUserAgentPoolStatusUseCase;
        this.getUserAgentsUseCase = getUserAgentsUseCase;
        this.userAgentApiMapper = userAgentApiMapper;
    }

    /**
     * UserAgent Pool 상태 조회
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/crawling/user-agents/pool-status
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "totalAgents": 100,
     *     "availableAgents": 85,
     *     "suspendedAgents": 15,
     *     "availableRate": 85.0,
     *     "healthScoreStats": {
     *       "avg": 75.5,
     *       "min": 30,
     *       "max": 100
     *     },
     *     "isCircuitBreakerOpen": false,
     *     "isHealthy": true
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "req-123456"
     * }
     * }</pre>
     *
     * @return UserAgent Pool 상태 (200 OK)
     */
    @GetMapping(ApiPaths.UserAgents.POOL_STATUS)
    @PreAuthorize("@access.hasPermission('useragent:read')")
    @Operation(
            summary = "UserAgent Pool 상태 조회",
            description = "UserAgent Pool의 상태를 조회합니다. useragent:read 권한이 필요합니다.",
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
                                                        UserAgentPoolStatusApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (useragent:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<UserAgentPoolStatusApiResponse>> getPoolStatus() {
        // 1. UseCase 실행 (비즈니스 로직)
        UserAgentPoolStatusResponse useCaseResponse = getUserAgentPoolStatusUseCase.execute();

        // 2. UseCase Response → API Response 변환 (Mapper)
        UserAgentPoolStatusApiResponse apiResponse =
                userAgentApiMapper.toApiResponse(useCaseResponse);

        // 3. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * UserAgent 목록 조회 (페이징, 상태 필터링)
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/crawling/user-agents
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Query Parameters:</strong>
     *
     * <ul>
     *   <li>status: UserAgent 상태 필터 (AVAILABLE, SUSPENDED, BLOCKED, 미지정시 전체)
     *   <li>page: 페이지 번호 (0부터 시작, 기본값 0)
     *   <li>size: 페이지 크기 (기본값 20, 최대 100)
     * </ul>
     *
     * @param status UserAgent 상태 필터 (null이면 전체 조회)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 UserAgent 목록 (200 OK)
     */
    @GetMapping
    @PreAuthorize("@access.hasPermission('useragent:read')")
    @Operation(
            summary = "UserAgent 목록 조회",
            description =
                    "UserAgent 목록을 페이징하여 조회합니다. 상태별 필터링을 지원합니다. " + "useragent:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = PageApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (useragent:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<PageApiResponse<UserAgentSummaryApiResponse>>> getUserAgents(
            @Parameter(
                            description = "UserAgent 상태 필터 (AVAILABLE, SUSPENDED, BLOCKED)",
                            example = "AVAILABLE")
                    @RequestParam(required = false)
                    UserAgentStatus status,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20")
                    @RequestParam(defaultValue = "20")
                    int size) {

        // 1. 검색 조건 생성
        PageRequest pageRequest = PageRequest.of(page, size);
        UserAgentSearchCriteria criteria =
                status != null
                        ? UserAgentSearchCriteria.byStatus(status, pageRequest)
                        : UserAgentSearchCriteria.all(pageRequest);

        // 2. UseCase 실행 (비즈니스 로직)
        PageResponse<UserAgentSummaryResponse> useCaseResponse =
                getUserAgentsUseCase.execute(criteria);

        // 3. UseCase Response → API Response 변환 (Mapper 활용)
        PageApiResponse<UserAgentSummaryApiResponse> apiResponse =
                PageApiResponse.from(useCaseResponse, userAgentApiMapper::toSummaryApiResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
