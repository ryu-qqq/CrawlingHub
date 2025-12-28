package com.ryuqq.crawlinghub.adapter.in.rest.dashboard.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse;
import com.ryuqq.crawlinghub.application.dashboard.port.in.query.GetDashboardStatsUseCase;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard Query Controller
 *
 * <p>관리자 대시보드의 통계 정보를 제공하는 API입니다.
 *
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/crawling/dashboard/stats - 대시보드 통계 조회
 * </ul>
 *
 * <p><strong>Controller 책임:</strong>
 *
 * <ul>
 *   <li>HTTP 요청 수신
 *   <li>UseCase 실행 위임
 *   <li>HTTP 응답 반환 (ResponseEntity)
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
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
@RequestMapping(ApiPaths.Dashboard.BASE)
@Validated
@Tag(name = "Dashboard", description = "관리자 대시보드 API")
public class DashboardQueryController {

    private final GetDashboardStatsUseCase getDashboardStatsUseCase;

    /**
     * DashboardQueryController 생성자
     *
     * @param getDashboardStatsUseCase 대시보드 통계 조회 UseCase
     */
    public DashboardQueryController(GetDashboardStatsUseCase getDashboardStatsUseCase) {
        this.getDashboardStatsUseCase = getDashboardStatsUseCase;
    }

    /**
     * 대시보드 통계 조회
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/crawling/dashboard/stats
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>조회 항목:</strong>
     *
     * <ul>
     *   <li>오늘 태스크 통계 (전체/성공/실패/진행중/대기중)
     *   <li>최근 7일 성공률 추이
     *   <li>스케줄 통계 (전체/활성/비활성)
     *   <li>Outbox 통계 (대기/발행완료/실패)
     *   <li>최근 실패 태스크 목록
     * </ul>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "todayTaskStats": { ... },
     *     "weeklySuccessRates": [ ... ],
     *     "scheduleStats": { ... },
     *     "outboxStats": { ... },
     *     "recentFailedTasks": [ ... ]
     *   },
     *   "error": null,
     *   "timestamp": "2025-12-28T10:30:00",
     *   "requestId": "req-123456"
     * }
     * }</pre>
     *
     * @return 대시보드 통계 정보 (200 OK)
     */
    @GetMapping(ApiPaths.Dashboard.STATS)
    @PreAuthorize("@access.hasPermission('dashboard:read')")
    @Operation(
            summary = "대시보드 통계 조회",
            description = "관리자 대시보드에 필요한 전반적인 통계 정보를 조회합니다. dashboard:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = DashboardStatsResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (dashboard:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse response = getDashboardStatsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ofSuccess(response));
    }
}
