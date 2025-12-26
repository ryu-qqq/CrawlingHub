package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query.SearchProductImageOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query.SearchProductSyncOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductImageOutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductSyncOutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.mapper.ProductOutboxQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductImageOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductSyncOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductSyncOutboxResponse;
import com.ryuqq.crawlinghub.application.product.port.in.query.SearchProductImageOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.query.SearchProductSyncOutboxUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProductOutbox Query Controller
 *
 * <p>상품 관련 Outbox 조회 API를 제공합니다.
 *
 * <p><strong>제공 API:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/crawling/product-outbox/sync - 상품 동기화 Outbox 목록 조회
 *   <li>GET /api/v1/crawling/product-outbox/image - 이미지 업로드 Outbox 목록 조회
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(ApiPaths.ProductOutbox.BASE)
@Validated
@Tag(name = "ProductOutbox", description = "상품 Outbox 모니터링 API")
public class ProductOutboxQueryController {

    private final SearchProductSyncOutboxUseCase searchSyncOutboxUseCase;
    private final SearchProductImageOutboxUseCase searchImageOutboxUseCase;
    private final ProductOutboxQueryApiMapper mapper;

    public ProductOutboxQueryController(
            SearchProductSyncOutboxUseCase searchSyncOutboxUseCase,
            SearchProductImageOutboxUseCase searchImageOutboxUseCase,
            ProductOutboxQueryApiMapper mapper) {
        this.searchSyncOutboxUseCase = searchSyncOutboxUseCase;
        this.searchImageOutboxUseCase = searchImageOutboxUseCase;
        this.mapper = mapper;
    }

    /**
     * 상품 동기화 Outbox 목록 조회
     *
     * <p>CrawledProduct → 외부 시스템 동기화 Outbox 상태를 페이징하여 조회합니다.
     *
     * @param request 검색 조건
     * @return 페이징된 SyncOutbox 목록
     */
    @GetMapping(ApiPaths.ProductOutbox.SYNC)
    @PreAuthorize("@access.hasPermission('outbox:read')")
    @Operation(
            summary = "상품 동기화 Outbox 목록 조회",
            description =
                    "CrawledProduct → 외부 시스템 동기화 Outbox 목록을 페이징하여 조회합니다. "
                            + "CrawledProduct ID, Seller ID, 상태 등으로 필터링할 수 있습니다. "
                            + "outbox:read 권한이 필요합니다.",
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
                description = "권한 없음 (outbox:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<PageApiResponse<ProductSyncOutboxApiResponse>>>
            searchSyncOutbox(@ModelAttribute @Valid SearchProductSyncOutboxApiRequest request) {
        SearchProductSyncOutboxQuery query = mapper.toSyncQuery(request);
        PageResponse<ProductSyncOutboxResponse> useCaseResponse =
                searchSyncOutboxUseCase.execute(query);
        PageApiResponse<ProductSyncOutboxApiResponse> apiPageResponse =
                mapper.toSyncPageApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiPageResponse));
    }

    /**
     * 이미지 업로드 Outbox 목록 조회
     *
     * <p>CrawledProductImage → 이미지 업로드 Outbox 상태를 페이징하여 조회합니다.
     *
     * @param request 검색 조건
     * @return 페이징된 ImageOutbox 목록
     */
    @GetMapping(ApiPaths.ProductOutbox.IMAGE)
    @PreAuthorize("@access.hasPermission('outbox:read')")
    @Operation(
            summary = "이미지 업로드 Outbox 목록 조회",
            description =
                    "CrawledProductImage → 이미지 업로드 Outbox 목록을 페이징하여 조회합니다. "
                            + "CrawledProductImage ID, 상태 등으로 필터링할 수 있습니다. "
                            + "outbox:read 권한이 필요합니다.",
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
                description = "권한 없음 (outbox:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<PageApiResponse<ProductImageOutboxApiResponse>>>
            searchImageOutbox(@ModelAttribute @Valid SearchProductImageOutboxApiRequest request) {
        SearchProductImageOutboxQuery query = mapper.toImageQuery(request);
        PageResponse<ProductImageOutboxResponse> useCaseResponse =
                searchImageOutboxUseCase.execute(query);
        PageApiResponse<ProductImageOutboxApiResponse> apiPageResponse =
                mapper.toImagePageApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiPageResponse));
    }
}
