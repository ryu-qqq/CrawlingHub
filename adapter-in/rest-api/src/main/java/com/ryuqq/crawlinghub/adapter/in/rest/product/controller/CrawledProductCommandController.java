package com.ryuqq.crawlinghub.adapter.in.rest.product.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.ManualSyncTriggerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.mapper.CrawledProductCommandApiMapper;
import com.ryuqq.crawlinghub.application.product.dto.command.TriggerManualSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.ManualSyncTriggerResponse;
import com.ryuqq.crawlinghub.application.product.port.in.command.TriggerManualSyncUseCase;
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
 * CrawledProduct Command Controller
 *
 * <p>크롤링 상품 도메인의 상태 변경 API를 제공합니다.
 *
 * <p><strong>제공 API:</strong>
 *
 * <ul>
 *   <li>POST /api/v1/crawling/crawled-products/{id}/sync - 수동 동기화 트리거
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(ApiPaths.CrawledProducts.BASE)
@Validated
@Tag(name = "CrawledProduct", description = "크롤링 상품 관리 API")
public class CrawledProductCommandController {

    private final TriggerManualSyncUseCase triggerManualSyncUseCase;
    private final CrawledProductCommandApiMapper mapper;

    public CrawledProductCommandController(
            TriggerManualSyncUseCase triggerManualSyncUseCase,
            CrawledProductCommandApiMapper mapper) {
        this.triggerManualSyncUseCase = triggerManualSyncUseCase;
        this.mapper = mapper;
    }

    /**
     * 수동 동기화 트리거
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/crawling/crawled-products/{id}/sync
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>비즈니스 규칙:</strong>
     *
     * <ul>
     *   <li>COMPLETED 상태의 CrawledProduct만 동기화 가능
     *   <li>외부 서버 등록 여부에 따라 CREATE 또는 UPDATE 타입 결정
     *   <li>SyncOutbox에 동기화 요청 등록
     * </ul>
     *
     * @param id CrawledProduct ID
     * @return 동기화 트리거 결과 (200 OK)
     */
    @PostMapping(ApiPaths.CrawledProducts.SYNC)
    @PreAuthorize("@access.hasPermission('product:update')")
    @Operation(
            summary = "수동 동기화 트리거",
            description =
                    "크롤링 상품을 외부 서버에 수동으로 동기화합니다. "
                            + "COMPLETED 상태의 상품만 동기화 가능합니다. "
                            + "외부 서버 등록 여부에 따라 CREATE 또는 UPDATE 타입으로 동기화됩니다. "
                            + "product:update 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "동기화 요청 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        ManualSyncTriggerApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (동기화 불가 상태)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (product:update 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "크롤링 상품을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<ManualSyncTriggerApiResponse>> triggerManualSync(
            @Parameter(description = "크롤링 상품 ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id) {
        TriggerManualSyncCommand command = mapper.toTriggerManualSyncCommand(id);
        ManualSyncTriggerResponse useCaseResponse = triggerManualSyncUseCase.execute(command);
        ManualSyncTriggerApiResponse apiResponse = mapper.toApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
