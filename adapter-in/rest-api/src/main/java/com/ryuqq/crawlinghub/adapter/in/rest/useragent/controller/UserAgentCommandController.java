package com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller;

import com.ryuqq.authhub.sdk.annotation.RequirePermission;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.UserAgentEndpoints;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command.RegisterUserAgentApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command.UpdateUserAgentMetadataApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command.UpdateUserAgentStatusApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.RecoverUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.RegisterUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UpdateUserAgentMetadataApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UpdateUserAgentStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.WarmUpUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RegisterUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentMetadataCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentStatusCommand;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RegisterUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.UpdateUserAgentMetadataUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.UpdateUserAgentStatusUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.WarmUpUserAgentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserAgent Command Controller
 *
 * <p>UserAgent 도메인의 명령 API를 제공합니다.
 *
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>POST /api/v1/crawling/user-agents/recover - 정지된 UserAgent 복구
 *   <li>PATCH /api/v1/crawling/user-agents/status - UserAgent 상태 일괄 변경
 * </ul>
 *
 * <p><strong>Controller 책임:</strong>
 *
 * <ul>
 *   <li>HTTP 요청 수신
 *   <li>UseCase 실행 위임
 *   <li>UseCase 결과 → API DTO 변환 (Mapper)
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
@RequestMapping(UserAgentEndpoints.BASE)
@Validated
@Tag(name = "UserAgent", description = "UserAgent 관리 API")
public class UserAgentCommandController {

    private final RegisterUserAgentUseCase registerUserAgentUseCase;
    private final UpdateUserAgentMetadataUseCase updateUserAgentMetadataUseCase;
    private final RecoverUserAgentUseCase recoverUserAgentUseCase;
    private final UpdateUserAgentStatusUseCase updateUserAgentStatusUseCase;
    private final WarmUpUserAgentUseCase warmUpUserAgentUseCase;
    private final UserAgentApiMapper userAgentApiMapper;

    /**
     * UserAgentCommandController 생성자
     *
     * @param registerUserAgentUseCase UserAgent 등록 UseCase
     * @param updateUserAgentMetadataUseCase UserAgent 메타데이터 수정 UseCase
     * @param recoverUserAgentUseCase UserAgent 복구 UseCase
     * @param updateUserAgentStatusUseCase UserAgent 상태 변경 UseCase
     * @param warmUpUserAgentUseCase UserAgent Warm-up UseCase
     * @param userAgentApiMapper UserAgent API Mapper
     */
    public UserAgentCommandController(
            RegisterUserAgentUseCase registerUserAgentUseCase,
            UpdateUserAgentMetadataUseCase updateUserAgentMetadataUseCase,
            RecoverUserAgentUseCase recoverUserAgentUseCase,
            UpdateUserAgentStatusUseCase updateUserAgentStatusUseCase,
            WarmUpUserAgentUseCase warmUpUserAgentUseCase,
            UserAgentApiMapper userAgentApiMapper) {
        this.registerUserAgentUseCase = registerUserAgentUseCase;
        this.updateUserAgentMetadataUseCase = updateUserAgentMetadataUseCase;
        this.recoverUserAgentUseCase = recoverUserAgentUseCase;
        this.updateUserAgentStatusUseCase = updateUserAgentStatusUseCase;
        this.warmUpUserAgentUseCase = warmUpUserAgentUseCase;
        this.userAgentApiMapper = userAgentApiMapper;
    }

    /**
     * 새 UserAgent 등록
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/crawling/user-agents
     *   <li>Status: 201 Created
     * </ul>
     *
     * <p><strong>처리 흐름:</strong>
     *
     * <ol>
     *   <li>Request Body 수신 및 Validation
     *   <li>API Request → Application Command 변환
     *   <li>Token 자동 생성 (AES-256-GCM)
     *   <li>UserAgent 저장 후 ID 반환
     * </ol>
     *
     * <p><strong>Request:</strong>
     *
     * <pre>{@code
     * {
     *   "userAgentString": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...",
     *   "deviceType": "DESKTOP",
     *   "deviceBrand": "GENERIC",
     *   "osType": "WINDOWS",
     *   "osVersion": "10.0",
     *   "browserType": "CHROME",
     *   "browserVersion": "120.0.0.0"
     * }
     * }</pre>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "data": {
     *     "userAgentId": 123,
     *     "message": "UserAgent registered successfully"
     *   },
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "550e8400-e29b-41d4-a716-446655440000"
     * }
     * }</pre>
     *
     * @param request 등록 요청 (UserAgent 문자열, 메타데이터 포함)
     * @return 등록 결과 (201 Created, 생성된 ID)
     */
    @PostMapping
    @PreAuthorize("@access.hasPermission('useragent:manage')")
    @RequirePermission(value = "useragent:manage", description = "유저에이전트 등록")
    @Operation(
            summary = "새 UserAgent 등록",
            description = "새로운 UserAgent를 등록합니다. useragent:manage 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "등록 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        RegisterUserAgentApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (Validation 오류)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (useragent:manage 권한 필요)")
    })
    public ResponseEntity<ApiResponse<RegisterUserAgentApiResponse>> registerUserAgent(
            @Valid @RequestBody RegisterUserAgentApiRequest request) {
        // 1. API Request → Application Command 변환 (Mapper)
        RegisterUserAgentCommand command = userAgentApiMapper.toCommand(request);

        // 2. UseCase 실행 (비즈니스 로직 - Token 생성 포함)
        long userAgentId = registerUserAgentUseCase.register(command);

        // 3. UseCase 결과 → API Response 변환 (Mapper)
        RegisterUserAgentApiResponse apiResponse =
                userAgentApiMapper.toRegisterApiResponse(userAgentId);

        // 4. ResponseEntity<ApiResponse<T>> 래핑 (201 Created)
        return ResponseEntity.status(201).body(ApiResponse.of(apiResponse));
    }

    /**
     * UserAgent 메타데이터 수정
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: PUT
     *   <li>Path: /api/v1/crawling/user-agents/{userAgentId}
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>처리 흐름:</strong>
     *
     * <ol>
     *   <li>Path Variable에서 UserAgent ID 추출
     *   <li>Request Body 수신 및 Validation
     *   <li>API Request → Application Command 변환 (Partial Update 지원)
     *   <li>메타데이터 수정 후 결과 반환
     * </ol>
     *
     * <p><strong>Request:</strong>
     *
     * <pre>{@code
     * {
     *   "userAgentString": "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0)...",
     *   "deviceType": "DESKTOP",
     *   "deviceBrand": "APPLE",
     *   "osType": "MACOS",
     *   "osVersion": "14.0",
     *   "browserType": "SAFARI",
     *   "browserVersion": "17.0"
     * }
     * }</pre>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "data": {
     *     "userAgentId": 123,
     *     "message": "UserAgent metadata updated successfully"
     *   },
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "550e8400-e29b-41d4-a716-446655440000"
     * }
     * }</pre>
     *
     * @param userAgentId 수정 대상 UserAgent ID
     * @param request 수정 요청 (변경할 메타데이터, 제공된 필드만 수정)
     * @return 수정 결과 (200 OK)
     */
    @PutMapping(UserAgentEndpoints.BY_ID)
    @PreAuthorize("@access.hasPermission('useragent:manage')")
    @RequirePermission(value = "useragent:manage", description = "유저에이전트 메타데이터 수정")
    @Operation(
            summary = "UserAgent 메타데이터 수정",
            description = "기존 UserAgent의 메타데이터를 수정합니다. 제공된 필드만 수정됩니다. useragent:manage 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        UpdateUserAgentMetadataApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (Validation 오류)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (useragent:manage 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "UserAgent를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<UpdateUserAgentMetadataApiResponse>> updateUserAgentMetadata(
            @PathVariable long userAgentId,
            @Valid @RequestBody UpdateUserAgentMetadataApiRequest request) {
        // 1. API Request → Application Command 변환 (Mapper)
        UpdateUserAgentMetadataCommand command = userAgentApiMapper.toCommand(userAgentId, request);

        // 2. UseCase 실행 (비즈니스 로직)
        updateUserAgentMetadataUseCase.updateMetadata(command);

        // 3. UseCase 결과 → API Response 변환 (Mapper)
        UpdateUserAgentMetadataApiResponse apiResponse =
                userAgentApiMapper.toMetadataUpdateApiResponse(userAgentId);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.of(apiResponse));
    }

    /**
     * 정지된 UserAgent 복구
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/crawling/user-agents/recover
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>복구 조건:</strong>
     *
     * <ul>
     *   <li>SUSPENDED 상태
     *   <li>1시간 경과
     *   <li>Health Score ≥ 30
     * </ul>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "data": {
     *     "recoveredCount": 5,
     *     "message": "5 user agents recovered successfully"
     *   },
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "550e8400-e29b-41d4-a716-446655440000"
     * }
     * }</pre>
     *
     * @return 복구 결과 (200 OK)
     */
    @PostMapping(UserAgentEndpoints.RECOVER)
    @PreAuthorize("@access.hasPermission('useragent:manage')")
    @RequirePermission(value = "useragent:manage", description = "유저에이전트 복구")
    @Operation(
            summary = "정지된 UserAgent 복구",
            description = "정지된 UserAgent를 복구합니다. useragent:manage 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "복구 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        RecoverUserAgentApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (useragent:manage 권한 필요)")
    })
    public ResponseEntity<ApiResponse<RecoverUserAgentApiResponse>> recoverUserAgents() {
        // 1. UseCase 실행 (비즈니스 로직)
        int recoveredCount = recoverUserAgentUseCase.recoverAll();

        // 2. UseCase 결과 → API Response 변환 (Mapper)
        RecoverUserAgentApiResponse apiResponse =
                userAgentApiMapper.toRecoverApiResponse(recoveredCount);

        // 3. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.of(apiResponse));
    }

    /**
     * UserAgent 상태 일괄 변경
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: PATCH
     *   <li>Path: /api/v1/crawling/user-agents/status
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>관리자가 체크박스로 여러 UserAgent를 선택하여 상태 변경
     *   <li>문제가 있는 UserAgent들을 일괄 정지(SUSPENDED) 처리
     *   <li>점검 완료 후 일괄 활성화(AVAILABLE) 처리
     *   <li>보안 문제로 일괄 차단(BLOCKED) 처리
     * </ul>
     *
     * <p><strong>Request:</strong>
     *
     * <pre>{@code
     * {
     *   "userAgentIds": [1, 2, 3],
     *   "status": "SUSPENDED"
     * }
     * }</pre>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "data": {
     *     "updatedCount": 3,
     *     "message": "3 user agent(s) status updated to SUSPENDED"
     *   },
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "550e8400-e29b-41d4-a716-446655440000"
     * }
     * }</pre>
     *
     * @param request 상태 변경 요청 (ID 목록 + 변경할 상태)
     * @return 상태 변경 결과 (200 OK)
     */
    @PatchMapping(UserAgentEndpoints.STATUS)
    @PreAuthorize("@access.hasPermission('useragent:manage')")
    @RequirePermission(value = "useragent:manage", description = "유저에이전트 상태 변경")
    @Operation(
            summary = "UserAgent 상태 일괄 변경",
            description = "여러 UserAgent의 상태를 일괄 변경합니다. useragent:manage 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "상태 변경 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        UpdateUserAgentStatusApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (ID 목록 비어있음, 상태 null 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (useragent:manage 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "UserAgent를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<UpdateUserAgentStatusApiResponse>> updateUserAgentStatus(
            @Valid @RequestBody UpdateUserAgentStatusApiRequest request) {
        // 1. API Request → Application Command 변환 (Mapper)
        UpdateUserAgentStatusCommand command = userAgentApiMapper.toCommand(request);

        // 2. UseCase 실행 (비즈니스 로직)
        int updatedCount = updateUserAgentStatusUseCase.execute(command);

        // 3. UseCase 결과 → API Response 변환 (Mapper)
        UpdateUserAgentStatusApiResponse apiResponse =
                userAgentApiMapper.toStatusUpdateApiResponse(updatedCount, request.status().name());

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.of(apiResponse));
    }

    /**
     * UserAgent Pool Warm-up
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/crawling/user-agents/warmup
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Warm-up 동작:</strong>
     *
     * <ul>
     *   <li>DB에서 AVAILABLE 상태 UserAgent 조회
     *   <li>Redis Pool에 없는 UserAgent만 추가
     *   <li>Lazy Token Issuance - 세션은 소비 시점에 발급
     * </ul>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "data": {
     *     "addedCount": 10,
     *     "message": "10 user agents added to pool"
     *   },
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "550e8400-e29b-41d4-a716-446655440000"
     * }
     * }</pre>
     *
     * @return Warm-up 결과 (200 OK)
     */
    @PostMapping(UserAgentEndpoints.WARMUP)
    @PreAuthorize("@access.hasPermission('useragent:manage')")
    @RequirePermission(value = "useragent:manage", description = "유저에이전트 워밍업")
    @Operation(
            summary = "UserAgent Pool Warm-up",
            description = "AVAILABLE 상태 UserAgent를 Redis Pool에 추가합니다. useragent:manage 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Warm-up 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        WarmUpUserAgentApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (useragent:manage 권한 필요)")
    })
    public ResponseEntity<ApiResponse<WarmUpUserAgentApiResponse>> warmUpUserAgents() {
        // 1. UseCase 실행 (비즈니스 로직)
        int addedCount = warmUpUserAgentUseCase.warmUp();

        // 2. UseCase 결과 → API Response 변환 (Mapper)
        WarmUpUserAgentApiResponse apiResponse = userAgentApiMapper.toWarmUpApiResponse(addedCount);

        // 3. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.of(apiResponse));
    }
}
