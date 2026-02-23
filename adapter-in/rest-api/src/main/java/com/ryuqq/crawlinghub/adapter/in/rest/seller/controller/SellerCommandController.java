package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.SellerEndpoints;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerCommandApiMapper;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Seller Command Controller
 *
 * <p>Seller 도메인의 상태 변경 API를 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(SellerEndpoints.BASE)
@Validated
@Tag(name = "Seller", description = "셀러 관리 API")
public class SellerCommandController {

    private final RegisterSellerUseCase registerSellerUseCase;
    private final UpdateSellerUseCase updateSellerUseCase;
    private final SellerCommandApiMapper sellerCommandApiMapper;

    public SellerCommandController(
            RegisterSellerUseCase registerSellerUseCase,
            UpdateSellerUseCase updateSellerUseCase,
            SellerCommandApiMapper sellerCommandApiMapper) {
        this.registerSellerUseCase = registerSellerUseCase;
        this.updateSellerUseCase = updateSellerUseCase;
        this.sellerCommandApiMapper = sellerCommandApiMapper;
    }

    @PostMapping
    @Operation(
            summary = "셀러 등록",
            description = "새로운 셀러를 등록합니다. seller:create 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "셀러 등록 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = Long.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (유효성 검증 실패)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (seller:create 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "중복된 셀러명")
    })
    public ResponseEntity<ApiResponse<Long>> registerSeller(
            @RequestBody @Valid RegisterSellerApiRequest request) {
        RegisterSellerCommand command = sellerCommandApiMapper.toCommand(request);
        long sellerId = registerSellerUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(sellerId));
    }

    @PatchMapping(SellerEndpoints.BY_ID)
    @Operation(
            summary = "셀러 수정",
            description = "셀러 정보를 수정합니다. seller:update 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "셀러 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (seller:update 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "셀러를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> updateSeller(
            @Parameter(description = "셀러 ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id,
            @RequestBody @Valid UpdateSellerApiRequest request) {
        UpdateSellerCommand command = sellerCommandApiMapper.toCommand(id, request);
        updateSellerUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.of(null));
    }
}
