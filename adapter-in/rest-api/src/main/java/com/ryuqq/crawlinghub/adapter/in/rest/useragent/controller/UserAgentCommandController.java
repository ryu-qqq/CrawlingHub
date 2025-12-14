package com.ryuqq.crawlinghub.adapter.in.rest.useragent.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.RecoverUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper.UserAgentApiMapper;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverUserAgentUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping(ApiPaths.UserAgents.BASE)
@Validated
public class UserAgentCommandController {

    private final RecoverUserAgentUseCase recoverUserAgentUseCase;
    private final UserAgentApiMapper userAgentApiMapper;

    /**
     * UserAgentCommandController 생성자
     *
     * @param recoverUserAgentUseCase UserAgent 복구 UseCase
     * @param userAgentApiMapper UserAgent API Mapper
     */
    public UserAgentCommandController(
            RecoverUserAgentUseCase recoverUserAgentUseCase,
            UserAgentApiMapper userAgentApiMapper) {
        this.recoverUserAgentUseCase = recoverUserAgentUseCase;
        this.userAgentApiMapper = userAgentApiMapper;
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
     *   "success": true,
     *   "data": {
     *     "recoveredCount": 5,
     *     "message": "5 user agents recovered successfully"
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "req-123456"
     * }
     * }</pre>
     *
     * @return 복구 결과 (200 OK)
     */
    @PostMapping(ApiPaths.UserAgents.RECOVER)
    public ResponseEntity<ApiResponse<RecoverUserAgentApiResponse>> recoverUserAgents() {
        // 1. UseCase 실행 (비즈니스 로직)
        int recoveredCount = recoverUserAgentUseCase.recoverAll();

        // 2. UseCase 결과 → API Response 변환 (Mapper)
        RecoverUserAgentApiResponse apiResponse =
                userAgentApiMapper.toRecoverApiResponse(recoveredCount);

        // 3. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
