package com.ryuqq.crawlinghub.adapter.in.rest.product.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.query.SearchCrawledProductsApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.mapper.CrawledProductQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchCrawledProductsQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductSummaryResponse;
import com.ryuqq.crawlinghub.application.product.port.in.query.GetCrawledProductDetailUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.query.SearchCrawledProductsUseCase;
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
 * CrawledProduct Query Controller
 *
 * <p>크롤링 상품 도메인의 조회 API를 제공합니다.
 *
 * <p><strong>제공 API:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/crawling/crawled-products - 크롤링 상품 목록 조회 (페이징)
 *   <li>GET /api/v1/crawling/crawled-products/{id} - 크롤링 상품 상세 조회
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(ApiPaths.CrawledProducts.BASE)
@Validated
@Tag(name = "CrawledProduct", description = "크롤링 상품 관리 API")
public class CrawledProductQueryController {

    private final SearchCrawledProductsUseCase searchCrawledProductsUseCase;
    private final GetCrawledProductDetailUseCase getCrawledProductDetailUseCase;
    private final CrawledProductQueryApiMapper mapper;

    public CrawledProductQueryController(
            SearchCrawledProductsUseCase searchCrawledProductsUseCase,
            GetCrawledProductDetailUseCase getCrawledProductDetailUseCase,
            CrawledProductQueryApiMapper mapper) {
        this.searchCrawledProductsUseCase = searchCrawledProductsUseCase;
        this.getCrawledProductDetailUseCase = getCrawledProductDetailUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @PreAuthorize("@access.hasPermission('product:read')")
    @Operation(
            summary = "크롤링 상품 목록 조회",
            description = "크롤링 상품 목록을 페이징하여 조회합니다. 다양한 필터 조건을 지원합니다. product:read 권한이 필요합니다.",
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
                description = "권한 없음 (product:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<PageApiResponse<CrawledProductSummaryApiResponse>>>
            searchCrawledProducts(@ModelAttribute @Valid SearchCrawledProductsApiRequest request) {
        SearchCrawledProductsQuery query = mapper.toQuery(request);
        PageResponse<CrawledProductSummaryResponse> useCaseResponse =
                searchCrawledProductsUseCase.execute(query);
        PageApiResponse<CrawledProductSummaryApiResponse> apiPageResponse =
                mapper.toPageApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiPageResponse));
    }

    @GetMapping(ApiPaths.CrawledProducts.BY_ID)
    @PreAuthorize("@access.hasPermission('product:read')")
    @Operation(
            summary = "크롤링 상품 상세 조회",
            description =
                    "크롤링 상품 ID로 상세 정보를 조회합니다. 가격, 이미지, 옵션, 크롤링 상태 등 모든 정보를 포함합니다. product:read 권한이"
                            + " 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        CrawledProductDetailApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (product:read 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "크롤링 상품을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<CrawledProductDetailApiResponse>> getCrawledProductDetail(
            @Parameter(description = "크롤링 상품 ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id) {
        CrawledProductDetailResponse useCaseResponse = getCrawledProductDetailUseCase.execute(id);
        CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
