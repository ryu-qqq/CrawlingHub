package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.List;

/**
 * ProductSyncOutbox 검색 API 요청
 *
 * @param crawledProductId CrawledProduct ID 필터 (nullable)
 * @param sellerId Seller ID 필터 (nullable)
 * @param itemNos 외부 상품번호 목록 필터 (IN 조건, nullable)
 * @param statuses 상태 필터 목록 (IN 조건, PENDING/PROCESSING/COMPLETED/FAILED, nullable)
 * @param createdFrom 생성일 시작 범위 (nullable)
 * @param createdTo 생성일 종료 범위 (nullable)
 * @param page 페이지 번호 (0부터 시작, 기본값: 0)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "상품 동기화 Outbox 검색 요청")
public record SearchProductSyncOutboxApiRequest(
        @Schema(description = "CrawledProduct ID 필터", example = "1") Long crawledProductId,
        @Schema(description = "Seller ID 필터", example = "100") Long sellerId,
        @Schema(description = "외부 상품번호 목록 필터 (IN 조건)", example = "[1001, 1002, 1003]")
                List<Long> itemNos,
        @Schema(
                        description = "상태 필터 목록 (IN 조건, PENDING/PROCESSING/COMPLETED/FAILED)",
                        example = "[\"FAILED\", \"PENDING\"]")
                List<String> statuses,
        @Schema(description = "생성일 시작 범위 (ISO-8601)", example = "2025-01-01T00:00:00Z")
                Instant createdFrom,
        @Schema(description = "생성일 종료 범위 (ISO-8601)", example = "2025-12-31T23:59:59Z")
                Instant createdTo,
        @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0") @Min(0)
                Integer page,
        @Schema(description = "페이지 크기 (최대 100)", example = "20", defaultValue = "20")
                @Min(1)
                @Max(100)
                Integer size) {

    public SearchProductSyncOutboxApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
