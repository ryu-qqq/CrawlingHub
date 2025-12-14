package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query.SearchSellersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.query.GetSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.query.SearchSellersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Seller Query Controller
 *
 * <p>Seller 도메인의 조회 API를 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(ApiPaths.Sellers.BASE)
@Validated
@Tag(name = "Seller", description = "셀러 관리 API")
public class SellerQueryController {

    private final GetSellerUseCase getSellerUseCase;
    private final SearchSellersUseCase searchSellersUseCase;
    private final SellerQueryApiMapper sellerQueryApiMapper;

    public SellerQueryController(
            GetSellerUseCase getSellerUseCase,
            SearchSellersUseCase searchSellersUseCase,
            SellerQueryApiMapper sellerQueryApiMapper) {
        this.getSellerUseCase = getSellerUseCase;
        this.searchSellersUseCase = searchSellersUseCase;
        this.sellerQueryApiMapper = sellerQueryApiMapper;
    }

    @GetMapping(ApiPaths.Sellers.BY_ID)
    @PreAuthorize("@access.hasPermission('seller:read')")
    @Operation(
            summary = "셀러 단건 조회",
            description = "셀러 ID로 상세 정보를 조회합니다. seller:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = SellerApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (seller:read 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "셀러를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<SellerApiResponse>> getSeller(
            @Parameter(description = "셀러 ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id) {
        GetSellerQuery query = sellerQueryApiMapper.toQuery(id);
        SellerResponse useCaseResponse = getSellerUseCase.execute(query);
        SellerApiResponse apiResponse = sellerQueryApiMapper.toApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    @GetMapping
    @PreAuthorize("@access.hasPermission('seller:read')")
    @Operation(
            summary = "셀러 목록 조회",
            description = "셀러 목록을 페이징하여 조회합니다. seller:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 파라미터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (seller:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<PageApiResponse<SellerSummaryApiResponse>>> listSellers(
            @ModelAttribute @Valid SearchSellersApiRequest request) {
        SearchSellersQuery query = sellerQueryApiMapper.toQuery(request);
        PageResponse<SellerSummaryResponse> useCasePageResponse =
                searchSellersUseCase.execute(query);
        PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                sellerQueryApiMapper.toPageApiResponse(useCasePageResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiPageResponse));
    }
}
