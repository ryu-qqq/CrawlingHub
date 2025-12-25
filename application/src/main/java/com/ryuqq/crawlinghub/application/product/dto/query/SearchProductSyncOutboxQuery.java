package com.ryuqq.crawlinghub.application.product.dto.query;

import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;

/**
 * ProductSyncOutbox 검색 조건 Query
 *
 * @param crawledProductId CrawledProduct ID (정확히 일치)
 * @param sellerId 셀러 ID (정확히 일치)
 * @param status 상태 필터 (PENDING, PROCESSING, COMPLETED, FAILED)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchProductSyncOutboxQuery(
        Long crawledProductId, Long sellerId, ProductOutboxStatus status, int page, int size) {

    public SearchProductSyncOutboxQuery {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 20;
        }
    }

    public long getOffset() {
        return (long) page * size;
    }
}
