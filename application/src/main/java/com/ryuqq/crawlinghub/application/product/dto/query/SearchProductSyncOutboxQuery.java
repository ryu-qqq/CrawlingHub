package com.ryuqq.crawlinghub.application.product.dto.query;

import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.List;

/**
 * ProductSyncOutbox 검색 조건 Query
 *
 * @param crawledProductId CrawledProduct ID (정확히 일치)
 * @param sellerId 셀러 ID (정확히 일치)
 * @param itemNos 외부 상품번호 목록 (IN 조건)
 * @param statuses 상태 필터 목록 (IN 조건, PENDING/PROCESSING/COMPLETED/FAILED)
 * @param createdFrom 생성일 시작 범위
 * @param createdTo 생성일 종료 범위
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchProductSyncOutboxQuery(
        Long crawledProductId,
        Long sellerId,
        List<Long> itemNos,
        List<ProductOutboxStatus> statuses,
        Instant createdFrom,
        Instant createdTo,
        int page,
        int size) {

    public SearchProductSyncOutboxQuery {
        itemNos = itemNos == null ? null : List.copyOf(itemNos);
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
