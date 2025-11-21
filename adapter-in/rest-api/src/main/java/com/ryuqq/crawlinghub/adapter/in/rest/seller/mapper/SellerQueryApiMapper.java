package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query.SearchSellersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.springframework.stereotype.Component;

/**
 * SellerQueryApiMapper - Seller Query REST API ↔ Application Layer 변환
 *
 * <p>Seller Query 요청/응답에 대한 DTO 변환을 담당합니다.
 *
 * <p><strong>변환 방향:</strong>
 *
 * <ul>
 *   <li>API Query Request → Application Query (Controller → Application)
 *   <li>Application Response → API Response (Application → Controller)
 * </ul>
 *
 * <p><strong>CQRS 패턴 적용:</strong>
 *
 * <ul>
 *   <li>Query: GetSeller, ListSellers 요청 변환
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 *   <li>Enum 변환 (String ↔ SellerStatus)
 *   <li>페이징 응답 변환 (PageResponse → PageApiResponse)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerQueryApiMapper {

    /**
     * ID → GetSellerQuery 변환
     *
     * <p>PathVariable의 ID를 GetSellerQuery로 변환합니다.
     *
     * @param sellerId 셀러 ID
     * @return Application Layer 셀러 조회 쿼리
     */
    public GetSellerQuery toQuery(Long sellerId) {
        return new GetSellerQuery(sellerId);
    }

    /**
     * ListSellersApiRequest → ListSellersQuery 변환
     *
     * @param request REST API 셀러 목록 조회 요청
     * @return Application Layer 셀러 목록 조회 쿼리
     */
    public SearchSellersQuery toQuery(SearchSellersApiRequest request) {
        SellerStatus status =
                (request.status() != null && !request.status().isBlank())
                        ? SellerStatus.valueOf(request.status())
                        : null;

        return new SearchSellersQuery(null, null, status, request.page(), request.size());
    }

    /**
     * SellerResponse → SellerApiResponse 변환
     *
     * @param appResponse Application Layer 셀러 응답
     * @return REST API 셀러 응답
     */
    public SellerApiResponse toApiResponse(SellerResponse appResponse) {
        String statusName = appResponse.active() ? "ACTIVE" : "INACTIVE";
        return new SellerApiResponse(
                appResponse.sellerId(),
                appResponse.mustItSellerName(),
                appResponse.sellerName(),
                statusName,
                appResponse.createdAt(),
                appResponse.updatedAt());
    }

    /**
     * SellerSummaryResponse → SellerSummaryApiResponse 변환
     *
     * @param appResponse Application Layer 셀러 요약 응답
     * @return REST API 셀러 요약 응답
     */
    public SellerSummaryApiResponse toSummaryApiResponse(SellerSummaryResponse appResponse) {
        String statusName = appResponse.active() ? "ACTIVE" : "INACTIVE";
        return new SellerSummaryApiResponse(
                appResponse.sellerId(),
                appResponse.mustItSellerName(),
                appResponse.sellerName(),
                statusName);
    }

    /**
     * PageResponse<SellerSummaryResponse> → PageApiResponse<SellerSummaryApiResponse> 변환
     *
     * <p>Application Layer의 페이지 응답을 REST API Layer의 페이지 응답으로 변환합니다.
     *
     * @param appPageResponse Application Layer 페이지 응답
     * @return REST API 페이지 응답
     */
    public PageApiResponse<SellerSummaryApiResponse> toPageApiResponse(
            PageResponse<SellerSummaryResponse> appPageResponse) {
        return PageApiResponse.from(appPageResponse, this::toSummaryApiResponse);
    }
}
