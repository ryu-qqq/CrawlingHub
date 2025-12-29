package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/**
 * List Sellers API Request
 *
 * <p>셀러 목록 조회 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>sellerName: 선택, 최대 100자 (부분 일치 검색)
 *   <li>mustItSellerName: 선택, 최대 100자 (부분 일치 검색)
 *   <li>statuses: 선택, "ACTIVE" 또는 "INACTIVE" 목록 (다중 선택 가능)
 *   <li>createdFrom: 선택, 생성일 시작 (ISO-8601 형식)
 *   <li>createdTo: 선택, 생성일 종료 (ISO-8601 형식)
 *   <li>page: 최소 0 (기본값: 0)
 *   <li>size: 1-100 (기본값: 20)
 * </ul>
 *
 * @param sellerName 셀러명 필터 (부분 일치 검색, 선택)
 * @param mustItSellerName 머스트잇 셀러명 필터 (부분 일치 검색, 선택)
 * @param statuses 셀러 상태 필터 목록 (다중 선택 가능, 선택)
 * @param createdFrom 생성일 시작 (선택)
 * @param createdTo 생성일 종료 (선택)
 * @param page 페이지 번호 (0부터 시작, 기본값: 0)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @author development-team
 * @since 1.0.0
 */
public record SearchSellersApiRequest(
        @Size(max = 100, message = "셀러명은 최대 100자까지 허용됩니다")
                @Schema(description = "셀러명 필터 (부분 일치 검색)", example = "테스트셀러")
                String sellerName,
        @Size(max = 100, message = "머스트잇 셀러명은 최대 100자까지 허용됩니다")
                @Schema(description = "머스트잇 셀러명 필터 (부분 일치 검색)", example = "머스트잇셀러")
                String mustItSellerName,
        @Schema(description = "상태 필터 목록 (다중 선택 가능)", example = "[\"ACTIVE\", \"INACTIVE\"]")
                List<String> statuses,
        @Schema(description = "생성일 시작 (ISO-8601)", example = "2025-01-01T00:00:00Z")
                Instant createdFrom,
        @Schema(description = "생성일 종료 (ISO-8601)", example = "2025-12-31T23:59:59Z")
                Instant createdTo,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
                @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
                Integer page,
        @Min(value = 1, message = "페이지 크기는 최소 1이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 최대 100까지 허용됩니다")
                @Schema(description = "페이지 크기 (최대 100)", example = "20")
                Integer size) {

    /** 기본값 적용 생성자 */
    public SearchSellersApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
