package com.ryuqq.crawlinghub.application.product.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductImageOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxWithImageResponse;

/**
 * ProductImageOutbox 검색 UseCase
 *
 * <p>페이징된 이미지 업로드 Outbox 목록 조회 (이미지 정보 포함)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchProductImageOutboxUseCase {

    /**
     * 검색 조건에 따른 ImageOutbox 목록 조회
     *
     * <p>CrawledProductImage와 JOIN하여 이미지 정보(originalUrl, s3Url, imageType)를 함께 반환합니다.
     *
     * @param query 검색 조건
     * @return 페이징된 ImageOutbox 목록 (이미지 정보 포함)
     */
    PageResponse<ProductImageOutboxWithImageResponse> execute(SearchProductImageOutboxQuery query);
}
