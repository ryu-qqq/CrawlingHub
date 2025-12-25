package com.ryuqq.crawlinghub.application.product.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductSyncOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductSyncOutboxResponse;

/**
 * ProductSyncOutbox 검색 UseCase
 *
 * <p>페이징된 외부 동기화 Outbox 목록 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchProductSyncOutboxUseCase {

    /**
     * 검색 조건에 따른 SyncOutbox 목록 조회
     *
     * @param query 검색 조건
     * @return 페이징된 SyncOutbox 목록
     */
    PageResponse<ProductSyncOutboxResponse> execute(SearchProductSyncOutboxQuery query);
}
