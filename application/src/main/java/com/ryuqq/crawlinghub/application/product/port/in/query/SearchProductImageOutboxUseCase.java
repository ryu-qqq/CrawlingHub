package com.ryuqq.crawlinghub.application.product.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductImageOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxResponse;

/**
 * ProductImageOutbox 검색 UseCase
 *
 * <p>페이징된 이미지 업로드 Outbox 목록 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchProductImageOutboxUseCase {

    /**
     * 검색 조건에 따른 ImageOutbox 목록 조회
     *
     * @param query 검색 조건
     * @return 페이징된 ImageOutbox 목록
     */
    PageResponse<ProductImageOutboxResponse> execute(SearchProductImageOutboxQuery query);
}
