package com.ryuqq.crawlinghub.application.product.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchCrawledProductsQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductSummaryResponse;

/**
 * CrawledProduct 검색 UseCase
 *
 * <p>페이징된 크롤링 상품 목록 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchCrawledProductsUseCase {

    /**
     * 검색 조건에 따른 CrawledProduct 목록 조회
     *
     * @param query 검색 조건
     * @return 페이징된 CrawledProduct 요약 목록
     */
    PageResponse<CrawledProductSummaryResponse> execute(SearchCrawledProductsQuery query);
}
