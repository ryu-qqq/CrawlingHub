package com.ryuqq.crawlinghub.adapter.in.rest.outbox.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.OutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.RepublishResultApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.mapper.OutboxApiMapper;
import com.ryuqq.crawlinghub.application.outbox.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.outbox.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.application.outbox.dto.response.RepublishResultResponse;
import com.ryuqq.crawlinghub.application.outbox.port.in.command.RepublishOutboxUseCase;
import com.ryuqq.crawlinghub.application.outbox.port.in.query.GetOutboxListUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Outbox Controller
 *
 * <p>Outbox 상태 조회 및 재발행 API를 제공합니다.
 *
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/crawling/outbox - Outbox 목록 조회
 *   <li>POST /api/v1/crawling/outbox/{crawlTaskId}/republish - Outbox 재발행
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(ApiPaths.Outbox.BASE)
@Validated
@Tag(name = "Outbox", description = "Outbox 상태 관리 API")
public class OutboxController {

    private final GetOutboxListUseCase getOutboxListUseCase;
    private final RepublishOutboxUseCase republishOutboxUseCase;
    private final OutboxApiMapper outboxApiMapper;

    public OutboxController(
            GetOutboxListUseCase getOutboxListUseCase,
            RepublishOutboxUseCase republishOutboxUseCase,
            OutboxApiMapper outboxApiMapper) {
        this.getOutboxListUseCase = getOutboxListUseCase;
        this.republishOutboxUseCase = republishOutboxUseCase;
        this.outboxApiMapper = outboxApiMapper;
    }

    /**
     * Outbox 목록 조회
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/crawling/outbox
     *   <li>Status: 200 OK
     * </ul>
     *
     * @param status 상태 필터 (PENDING, FAILED, SENT)
     * @param limit 조회 개수 제한 (기본값: 100, 최대: 500)
     * @return Outbox 목록
     */
    @GetMapping
    @PreAuthorize("@access.hasPermission('outbox:read')")
    @Operation(
            summary = "Outbox 목록 조회",
            description =
                    "PENDING 또는 FAILED 상태의 Outbox 목록을 조회합니다. "
                            + "상태 필터를 지정하지 않으면 PENDING과 FAILED 상태를 모두 조회합니다. "
                            + "outbox:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = OutboxApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (outbox:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<List<OutboxApiResponse>>> getOutboxList(
            @Parameter(description = "상태 필터 (PENDING, FAILED, SENT)", example = "PENDING")
                    @RequestParam(required = false)
                    String status,
            @Parameter(description = "조회 개수 제한", example = "100")
                    @RequestParam(defaultValue = "100")
                    @Positive
                    int limit) {
        GetOutboxListQuery query = outboxApiMapper.toQuery(status, Math.min(limit, 500));
        List<OutboxResponse> useCaseResponse = getOutboxListUseCase.execute(query);
        List<OutboxApiResponse> apiResponse =
                useCaseResponse.stream().map(outboxApiMapper::toApiResponse).toList();
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * Outbox 재발행
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/crawling/outbox/{crawlTaskId}/republish
     *   <li>Status: 200 OK
     * </ul>
     *
     * @param crawlTaskId 재발행할 Task ID
     * @return 재발행 결과
     */
    @PostMapping(ApiPaths.Outbox.REPUBLISH)
    @PreAuthorize("@access.hasPermission('outbox:update')")
    @Operation(
            summary = "Outbox 재발행",
            description =
                    "특정 CrawlTask의 Outbox를 SQS로 다시 발행합니다. "
                            + "PENDING 또는 FAILED 상태의 Outbox만 재발행할 수 있습니다. "
                            + "outbox:update 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "재발행 처리 완료 (성공/실패 결과 포함)",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        RepublishResultApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (outbox:update 권한 필요)")
    })
    public ResponseEntity<ApiResponse<RepublishResultApiResponse>> republishOutbox(
            @Parameter(description = "Task ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long crawlTaskId) {
        RepublishResultResponse useCaseResponse = republishOutboxUseCase.republish(crawlTaskId);
        RepublishResultApiResponse apiResponse =
                outboxApiMapper.toRepublishApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
