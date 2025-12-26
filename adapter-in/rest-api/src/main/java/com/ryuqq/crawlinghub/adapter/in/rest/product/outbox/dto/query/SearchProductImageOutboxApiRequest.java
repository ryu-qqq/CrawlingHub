package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * ProductImageOutbox 검색 API 요청
 *
 * @param crawledProductImageId CrawledProductImage ID 필터 (nullable)
 * @param status 상태 필터 (PENDING, PROCESSING, COMPLETED, FAILED) (nullable)
 * @param page 페이지 번호 (0부터 시작, 기본값: 0)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "상품 이미지 Outbox 검색 요청")
public record SearchProductImageOutboxApiRequest(
        @Schema(description = "CrawledProductImage ID 필터", example = "1")
                Long crawledProductImageId,
        @Schema(description = "상태 필터 (PENDING, PROCESSING, COMPLETED, FAILED)", example = "FAILED")
                String status,
        @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0") @Min(0)
                Integer page,
        @Schema(description = "페이지 크기 (최대 100)", example = "20", defaultValue = "20")
                @Min(1)
                @Max(100)
                Integer size) {

    public SearchProductImageOutboxApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
