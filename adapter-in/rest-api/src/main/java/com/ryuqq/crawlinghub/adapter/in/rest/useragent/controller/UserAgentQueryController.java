package com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentPoolStatusUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserAgent Query Controller
 *
 * <p>UserAgent 도메인의 조회 API를 제공합니다.
 *
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/user-agents/pool-status - UserAgent Pool 상태 조회
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
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.user-agent.base}")
@Validated
public class UserAgentQueryController {

    private final GetUserAgentPoolStatusUseCase getUserAgentPoolStatusUseCase;
    private final UserAgentApiMapper userAgentApiMapper;

    /**
     * UserAgentQueryController 생성자
     *
     * @param getUserAgentPoolStatusUseCase UserAgent Pool 상태 조회 UseCase
     * @param userAgentApiMapper UserAgent API Mapper
     */
    public UserAgentQueryController(
            GetUserAgentPoolStatusUseCase getUserAgentPoolStatusUseCase,
            UserAgentApiMapper userAgentApiMapper) {
        this.getUserAgentPoolStatusUseCase = getUserAgentPoolStatusUseCase;
        this.userAgentApiMapper = userAgentApiMapper;
    }

    /**
     * UserAgent Pool 상태 조회
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/user-agents/pool-status
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
    @GetMapping("${api.endpoints.user-agent.pool-status}")
    public ResponseEntity<ApiResponse<UserAgentPoolStatusApiResponse>> getPoolStatus() {
        // 1. UseCase 실행 (비즈니스 로직)
        UserAgentPoolStatusResponse useCaseResponse = getUserAgentPoolStatusUseCase.execute();

        // 2. UseCase Response → API Response 변환 (Mapper)
        UserAgentPoolStatusApiResponse apiResponse = userAgentApiMapper.toApiResponse(useCaseResponse);

        // 3. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
