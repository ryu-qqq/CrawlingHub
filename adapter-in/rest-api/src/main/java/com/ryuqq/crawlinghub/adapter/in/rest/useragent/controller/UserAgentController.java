package com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.IssueTokenApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.UserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.useragent.dto.command.DisableUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverRateLimitCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.query.GetUserAgentDetailQuery;
import com.ryuqq.crawlinghub.application.useragent.port.in.DisableUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.GetUserAgentDetailUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.IssueTokenUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.RecoverRateLimitUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * UserAgentController - UserAgent REST API 컨트롤러
 *
 * <p>RESTful API를 제공하는 Inbound Adapter입니다.</p>
 * <p>비즈니스 로직은 포함하지 않으며, UseCase 호출과 응답 변환만 담당합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@RestController
@RequestMapping("/api/v1/user-agents")
@Tag(name = "UserAgent API", description = "User-Agent 관리 API")
public class UserAgentController {

    private final IssueTokenUseCase issueTokenUseCase;
    private final RecoverRateLimitUseCase recoverRateLimitUseCase;
    private final DisableUserAgentUseCase disableUserAgentUseCase;
    private final GetUserAgentDetailUseCase getUserAgentDetailUseCase;
    private final UserAgentApiMapper userAgentApiMapper;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param issueTokenUseCase 토큰 발급 UseCase
     * @param recoverRateLimitUseCase Rate Limit 복구 UseCase
     * @param disableUserAgentUseCase UserAgent 비활성화 UseCase
     * @param getUserAgentDetailUseCase UserAgent 상세 조회 UseCase
     * @param userAgentApiMapper API Mapper
     */
    public UserAgentController(
            IssueTokenUseCase issueTokenUseCase,
            RecoverRateLimitUseCase recoverRateLimitUseCase,
            DisableUserAgentUseCase disableUserAgentUseCase,
            GetUserAgentDetailUseCase getUserAgentDetailUseCase,
            UserAgentApiMapper userAgentApiMapper
    ) {
        this.issueTokenUseCase = Objects.requireNonNull(issueTokenUseCase, "issueTokenUseCase must not be null");
        this.recoverRateLimitUseCase = Objects.requireNonNull(recoverRateLimitUseCase, "recoverRateLimitUseCase must not be null");
        this.disableUserAgentUseCase = Objects.requireNonNull(disableUserAgentUseCase, "disableUserAgentUseCase must not be null");
        this.getUserAgentDetailUseCase = Objects.requireNonNull(getUserAgentDetailUseCase, "getUserAgentDetailUseCase must not be null");
        this.userAgentApiMapper = Objects.requireNonNull(userAgentApiMapper, "userAgentApiMapper must not be null");
    }

    /**
     * UserAgent 상세 조회
     *
     * <p>GET /api/v1/user-agents/{userAgentId}</p>
     *
     * @param userAgentId UserAgent ID
     * @return UserAgent 상세 정보
     */
    @GetMapping("/{userAgentId}")
    @Operation(summary = "UserAgent 상세 조회", description = "UserAgent 상세 정보 조회")
    public ResponseEntity<ApiResponse<UserAgentApiResponse>> getUserAgentDetail(
            @PathVariable("userAgentId") Long userAgentId
    ) {
        // 1. Query 생성
        GetUserAgentDetailQuery query = new GetUserAgentDetailQuery(userAgentId);

        // 2. UseCase 실행
        var response = getUserAgentDetailUseCase.execute(query);

        // 3. Application Response → API Response 변환
        UserAgentApiResponse apiResponse = userAgentApiMapper.toResponse(response);

        // 4. ApiResponse로 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 토큰 발급
     *
     * <p>POST /api/v1/user-agents/{userAgentId}/tokens</p>
     *
     * @param userAgentId UserAgent ID
     * @param request 토큰 발급 Request (Bean Validation 적용)
     * @return 토큰이 발급된 UserAgent 정보
     */
    @PostMapping("/{userAgentId}/tokens")
    @Operation(summary = "토큰 발급", description = "UserAgent에 새 토큰 발급")
    public ResponseEntity<ApiResponse<UserAgentApiResponse>> issueToken(
            @PathVariable("userAgentId") Long userAgentId,
            @Valid @RequestBody IssueTokenApiRequest request
    ) {
        // 1. Request → Command 변환
        var command = userAgentApiMapper.toCommand(userAgentId, request);

        // 2. UseCase 실행
        var response = issueTokenUseCase.execute(command);

        // 3. Application Response → API Response 변환
        UserAgentApiResponse apiResponse = userAgentApiMapper.toResponse(response);

        // 4. ApiResponse로 래핑
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * Rate Limit 복구
     *
     * <p>PUT /api/v1/user-agents/{userAgentId}/recover</p>
     *
     * @param userAgentId UserAgent ID
     * @return 복구된 UserAgent 정보
     */
    @PutMapping("/{userAgentId}/recover")
    @Operation(summary = "Rate Limit 복구", description = "UserAgent의 Rate Limit 복구")
    public ResponseEntity<ApiResponse<UserAgentApiResponse>> recoverRateLimit(
            @PathVariable("userAgentId") Long userAgentId
    ) {
        // 1. Command 생성
        RecoverRateLimitCommand command = new RecoverRateLimitCommand(userAgentId);

        // 2. UseCase 실행
        var response = recoverRateLimitUseCase.execute(command);

        // 3. Application Response → API Response 변환
        UserAgentApiResponse apiResponse = userAgentApiMapper.toResponse(response);

        // 4. ApiResponse로 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * UserAgent 비활성화
     *
     * <p>DELETE /api/v1/user-agents/{userAgentId}</p>
     *
     * @param userAgentId UserAgent ID
     * @return 비활성화된 UserAgent 정보
     */
    @DeleteMapping("/{userAgentId}")
    @Operation(summary = "UserAgent 비활성화", description = "UserAgent 비활성화")
    public ResponseEntity<ApiResponse<UserAgentApiResponse>> disableUserAgent(
            @PathVariable("userAgentId") Long userAgentId
    ) {
        // 1. Command 생성
        DisableUserAgentCommand command = new DisableUserAgentCommand(userAgentId);

        // 2. UseCase 실행
        var response = disableUserAgentUseCase.execute(command);

        // 3. Application Response → API Response 변환
        UserAgentApiResponse apiResponse = userAgentApiMapper.toResponse(response);

        // 4. ApiResponse로 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}

