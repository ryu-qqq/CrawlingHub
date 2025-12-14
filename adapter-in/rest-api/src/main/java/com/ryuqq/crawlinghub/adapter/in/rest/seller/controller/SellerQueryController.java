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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
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
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/crawling/sellers/{id} - 셀러 단건 조회
 *   <li>GET /api/v1/crawling/sellers - 셀러 목록 조회
 * </ul>
 *
 * <p><strong>Controller 책임:</strong>
 *
 * <ul>
 *   <li>HTTP 요청 수신 및 유효성 검증 (@Valid)
 *   <li>API DTO → UseCase DTO 변환 (Mapper)
 *   <li>UseCase 실행 위임
 *   <li>UseCase DTO → API DTO 변환 (Mapper)
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
@RequestMapping(ApiPaths.Sellers.BASE)
@Validated
public class SellerQueryController {

    private final GetSellerUseCase getSellerUseCase;
    private final SearchSellersUseCase searchSellersUseCase;
    private final SellerQueryApiMapper sellerQueryApiMapper;

    /**
     * SellerQueryController 생성자
     *
     * @param getSellerUseCase 셀러 단건 조회 UseCase
     * @param searchSellersUseCase 셀러 목록 조회 UseCase
     * @param sellerQueryApiMapper Seller Query API Mapper
     */
    public SellerQueryController(
            GetSellerUseCase getSellerUseCase,
            SearchSellersUseCase searchSellersUseCase,
            SellerQueryApiMapper sellerQueryApiMapper) {
        this.getSellerUseCase = getSellerUseCase;
        this.searchSellersUseCase = searchSellersUseCase;
        this.sellerQueryApiMapper = sellerQueryApiMapper;
    }

    /**
     * 셀러 단건 조회
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/crawling/sellers/{id}
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "sellerId": 1,
     *     "mustItSellerName": "머스트잇 셀러명",
     *     "sellerName": "커머스 셀러명",
     *     "status": "ACTIVE",
     *     "createdAt": "2025-11-19T10:30:00",
     *     "updatedAt": null
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-19T10:30:00",
     *   "requestId": "req-123456"
     * }
     * }</pre>
     *
     * @param id 셀러 ID (양수, PathVariable)
     * @return 셀러 상세 정보 (200 OK)
     */
    @GetMapping(ApiPaths.Sellers.BY_ID)
    public ResponseEntity<ApiResponse<SellerApiResponse>> getSeller(
            @PathVariable @Positive Long id) {
        // 1. ID → UseCase Query 변환 (Mapper)
        GetSellerQuery query = sellerQueryApiMapper.toQuery(id);

        // 2. UseCase 실행 (비즈니스 로직)
        SellerResponse useCaseResponse = getSellerUseCase.execute(query);

        // 3. UseCase Response → API Response 변환 (Mapper)
        SellerApiResponse apiResponse = sellerQueryApiMapper.toApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 셀러 목록 조회 (페이징)
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/crawling/sellers
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Query Parameters:</strong>
     *
     * <ul>
     *   <li>status: 셀러 상태 필터 (선택, ACTIVE/INACTIVE)
     *   <li>page: 페이지 번호 (선택, 기본값: 0)
     *   <li>size: 페이지 크기 (선택, 기본값: 20, 최대: 100)
     * </ul>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "content": [
     *       {
     *         "sellerId": 1,
     *         "mustItSellerName": "머스트잇 셀러명",
     *         "sellerName": "커머스 셀러명",
     *         "status": "ACTIVE"
     *       }
     *     ],
     *     "page": 0,
     *     "size": 20,
     *     "totalElements": 100,
     *     "totalPages": 5,
     *     "first": true,
     *     "last": false
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-19T10:30:00",
     *   "requestId": "req-123456"
     * }
     * }</pre>
     *
     * @param request 셀러 목록 조회 요청 DTO (Bean Validation 적용)
     * @return 셀러 목록 (페이징) (200 OK)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageApiResponse<SellerSummaryApiResponse>>> listSellers(
            @ModelAttribute @Valid SearchSellersApiRequest request) {
        // 1. API Request → UseCase Query 변환 (Mapper)
        SearchSellersQuery query = sellerQueryApiMapper.toQuery(request);

        // 2. UseCase 실행 (비즈니스 로직)
        PageResponse<SellerSummaryResponse> useCasePageResponse =
                searchSellersUseCase.execute(query);

        // 3. UseCase Response → API Response 변환 (Mapper)
        PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                sellerQueryApiMapper.toPageApiResponse(useCasePageResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiPageResponse));
    }
}
