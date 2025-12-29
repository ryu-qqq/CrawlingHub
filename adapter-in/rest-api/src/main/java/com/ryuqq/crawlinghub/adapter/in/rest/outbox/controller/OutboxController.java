package com.ryuqq.crawlinghub.adapter.in.rest.outbox.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.OutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.RepublishResultApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.mapper.OutboxApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
 *   <li>GET /api/v1/crawling/outbox - Outbox 목록 조회 (페이징)
 *   <li>POST /api/v1/crawling/outbox/{crawlTaskId}/republish - Outbox 재발행 (SQS 활성화 시에만 가능)
 * </ul>
 *
 * <p><strong>SQS 조건부 기능:</strong> republish 기능은 app.messaging.sqs.enabled=true일 때만 사용 가능합니다.
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
    private final Optional<RepublishOutboxUseCase> republishOutboxUseCase;
    private final OutboxApiMapper outboxApiMapper;

    public OutboxController(
            GetOutboxListUseCase getOutboxListUseCase,
            Optional<RepublishOutboxUseCase> republishOutboxUseCase,
            OutboxApiMapper outboxApiMapper) {
        this.getOutboxListUseCase = getOutboxListUseCase;
        this.republishOutboxUseCase = republishOutboxUseCase;
        this.outboxApiMapper = outboxApiMapper;
    }

    /**
     * Outbox 목록 조회 (페이징)
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/crawling/outbox
     *   <li>Status: 200 OK
     * </ul>
     *
     * @param statuses 상태 필터 목록 (PENDING, FAILED, SENT) - 다중 선택 가능
     * @param createdFrom 생성일 시작 범위 (ISO-8601, inclusive)
     * @param createdTo 생성일 종료 범위 (ISO-8601, exclusive)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 20, 최대: 100)
     * @return 페이징된 Outbox 목록
     */
    @GetMapping
    @PreAuthorize("@access.hasPermission('outbox:read')")
    @Operation(
            summary = "Outbox 목록 조회",
            description =
                    "PENDING 또는 FAILED 상태의 Outbox 목록을 페이징하여 조회합니다. "
                            + "상태 필터를 지정하지 않으면 PENDING과 FAILED 상태를 모두 조회합니다. "
                            + "다중 상태 필터와 기간 필터를 조합하여 사용할 수 있습니다. "
                            + "outbox:read 권한이 필요합니다.",
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
                responseCode = "400",
                description = "잘못된 파라미터 (유효하지 않은 상태 값)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (outbox:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<PageApiResponse<OutboxApiResponse>>> getOutboxList(
            @Parameter(
                            description = "상태 필터 목록 (PENDING, FAILED, SENT) - 다중 선택 가능",
                            example = "PENDING")
                    @RequestParam(required = false)
                    List<String> statuses,
            @Parameter(
                            description = "생성일 시작 범위 (ISO-8601, inclusive)",
                            example = "2024-01-01T00:00:00Z")
                    @RequestParam(required = false)
                    Instant createdFrom,
            @Parameter(
                            description = "생성일 종료 범위 (ISO-8601, exclusive)",
                            example = "2024-12-31T23:59:59Z")
                    @RequestParam(required = false)
                    Instant createdTo,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                    @RequestParam(defaultValue = "0")
                    @Min(0)
                    int page,
            @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)", example = "20")
                    @RequestParam(defaultValue = "20")
                    @Min(1)
                    @Max(100)
                    int size) {
        GetOutboxListQuery query =
                outboxApiMapper.toQuery(statuses, createdFrom, createdTo, page, size);
        PageResponse<OutboxResponse> useCaseResponse = getOutboxListUseCase.execute(query);
        PageApiResponse<OutboxApiResponse> apiResponse =
                outboxApiMapper.toPageApiResponse(useCaseResponse);
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
     *   <li>Status: 200 OK (성공 시), 503 Service Unavailable (SQS 비활성화 시)
     * </ul>
     *
     * <p><strong>주의:</strong> SQS 비활성화 시(app.messaging.sqs.enabled=false) 503 에러를 반환합니다.
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
                            + "SQS 비활성화 시 503 에러를 반환합니다. "
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
                description = "권한 없음 (outbox:update 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "503",
                description = "SQS 비활성화 상태 (app.messaging.sqs.enabled=false)")
    })
    public ResponseEntity<ApiResponse<RepublishResultApiResponse>> republishOutbox(
            @Parameter(description = "Task ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long crawlTaskId) {
        if (republishOutboxUseCase.isEmpty()) {
            RepublishResultApiResponse unavailableResponse =
                    outboxApiMapper.toRepublishApiResponse(
                            RepublishResultResponse.failure(
                                    crawlTaskId,
                                    "SQS 비활성화 상태입니다. app.messaging.sqs.enabled=true로 설정하세요."));
            return ResponseEntity.status(503).body(ApiResponse.ofSuccess(unavailableResponse));
        }

        RepublishResultResponse useCaseResponse =
                republishOutboxUseCase.get().republish(crawlTaskId);
        RepublishResultApiResponse apiResponse =
                outboxApiMapper.toRepublishApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
