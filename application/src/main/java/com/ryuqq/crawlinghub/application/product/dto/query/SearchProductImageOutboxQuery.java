package com.ryuqq.crawlinghub.application.product.dto.query;

import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.List;

/**
 * ProductImageOutbox 검색 조건 Query
 *
 * @param crawledProductImageId CrawledProductImage ID (정확히 일치)
 * @param crawledProductId CrawledProduct ID (정확히 일치)
 * @param statuses 상태 필터 목록 (IN 조건, PENDING/PROCESSING/COMPLETED/FAILED)
 * @param createdFrom 생성일 시작 범위
 * @param createdTo 생성일 종료 범위
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchProductImageOutboxQuery(
        Long crawledProductImageId,
        Long crawledProductId,
        List<ProductOutboxStatus> statuses,
        Instant createdFrom,
        Instant createdTo,
        int page,
        int size) {

    public SearchProductImageOutboxQuery {
        statuses = statuses == null ? null : List.copyOf(statuses);
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
