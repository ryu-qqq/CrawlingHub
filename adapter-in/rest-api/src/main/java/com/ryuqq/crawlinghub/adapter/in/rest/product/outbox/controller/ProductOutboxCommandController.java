package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.OutboxRetryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.mapper.ProductOutboxCommandApiMapper;
import com.ryuqq.crawlinghub.application.product.dto.command.RetryImageOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.command.RetrySyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.OutboxRetryResponse;
import com.ryuqq.crawlinghub.application.product.port.in.command.RetryImageOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.command.RetrySyncOutboxUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProductOutbox Command Controller
 *
 * <p>상품 동기화 및 이미지 Outbox의 상태 변경 API를 제공합니다.
 *
 * <p><strong>제공 API:</strong>
 *
 * <ul>
 *   <li>POST /api/v1/crawling/product-outbox/sync/{id}/retry - SyncOutbox 재시도
 *   <li>POST /api/v1/crawling/product-outbox/image/{id}/retry - ImageOutbox 재시도
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(ApiPaths.ProductOutbox.BASE)
@Validated
@Tag(name = "ProductOutbox", description = "상품 Outbox 관리 API")
public class ProductOutboxCommandController {

    private final RetrySyncOutboxUseCase retrySyncOutboxUseCase;
    private final RetryImageOutboxUseCase retryImageOutboxUseCase;
    private final ProductOutboxCommandApiMapper mapper;

    public ProductOutboxCommandController(
            RetrySyncOutboxUseCase retrySyncOutboxUseCase,
            RetryImageOutboxUseCase retryImageOutboxUseCase,
            ProductOutboxCommandApiMapper mapper) {
        this.retrySyncOutboxUseCase = retrySyncOutboxUseCase;
        this.retryImageOutboxUseCase = retryImageOutboxUseCase;
        this.mapper = mapper;
    }

    /**
     * SyncOutbox 재시도
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/crawling/product-outbox/sync/{id}/retry
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>비즈니스 규칙:</strong>
     *
     * <ul>
     *   <li>FAILED 상태의 SyncOutbox만 재시도 가능
     *   <li>최대 재시도 횟수(3회) 초과 시 재시도 불가
     *   <li>재시도 시 상태가 PENDING으로 변경됨
     * </ul>
     *
     * @param id SyncOutbox ID
     * @return 재시도 결과 (200 OK)
     */
    @PostMapping(ApiPaths.ProductOutbox.SYNC_RETRY)
    @PreAuthorize("@access.hasPermission('outbox:update')")
    @Operation(
            summary = "SyncOutbox 재시도",
            description =
                    "실패한 상품 동기화 Outbox를 재시도합니다. "
                            + "FAILED 상태의 Outbox만 재시도 가능합니다. "
                            + "최대 재시도 횟수 초과 시 재시도할 수 없습니다. "
                            + "outbox:update 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "재시도 요청 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = OutboxRetryApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (재시도 불가 상태 또는 최대 재시도 횟수 초과)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (outbox:update 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "SyncOutbox를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<OutboxRetryApiResponse>> retrySyncOutbox(
            @Parameter(description = "SyncOutbox ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id) {
        RetrySyncOutboxCommand command = mapper.toRetrySyncOutboxCommand(id);
        OutboxRetryResponse useCaseResponse = retrySyncOutboxUseCase.execute(command);
        OutboxRetryApiResponse apiResponse = mapper.toApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * ImageOutbox 재시도
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/crawling/product-outbox/image/{id}/retry
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>비즈니스 규칙:</strong>
     *
     * <ul>
     *   <li>FAILED 상태의 ImageOutbox만 재시도 가능
     *   <li>최대 재시도 횟수(3회) 초과 시 재시도 불가
     *   <li>재시도 시 상태가 PENDING으로 변경됨
     * </ul>
     *
     * @param id ImageOutbox ID
     * @return 재시도 결과 (200 OK)
     */
    @PostMapping(ApiPaths.ProductOutbox.IMAGE_RETRY)
    @PreAuthorize("@access.hasPermission('outbox:update')")
    @Operation(
            summary = "ImageOutbox 재시도",
            description =
                    "실패한 이미지 Outbox를 재시도합니다. "
                            + "FAILED 상태의 Outbox만 재시도 가능합니다. "
                            + "최대 재시도 횟수 초과 시 재시도할 수 없습니다. "
                            + "outbox:update 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "재시도 요청 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = OutboxRetryApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (재시도 불가 상태 또는 최대 재시도 횟수 초과)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (outbox:update 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "ImageOutbox를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<OutboxRetryApiResponse>> retryImageOutbox(
            @Parameter(description = "ImageOutbox ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id) {
        RetryImageOutboxCommand command = mapper.toRetryImageOutboxCommand(id);
        OutboxRetryResponse useCaseResponse = retryImageOutboxUseCase.execute(command);
        OutboxRetryApiResponse apiResponse = mapper.toApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
