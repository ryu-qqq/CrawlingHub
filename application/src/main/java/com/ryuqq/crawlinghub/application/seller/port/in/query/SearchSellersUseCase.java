package com.ryuqq.crawlinghub.application.seller.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;

/**
 * Search Sellers UseCase (Query)
 *
 * <p>셀러 목록 조회를 담당하는 Inbound Port
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchSellersUseCase {

    /**
     * 셀러 목록 조회 (페이징)
     *
     * @param query 검색 조건
     * @return 페이징된 셀러 목록
     */
    PageResponse<SellerSummaryResponse> execute(SearchSellersQuery query);
}
