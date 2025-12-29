package com.ryuqq.crawlinghub.adapter.in.rest.product.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.List;

/**
 * SearchCrawledProducts API Request
 *
 * <p>크롤링 상품 목록 조회 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>sellerId: 선택, 양수
 *   <li>itemNos: 선택, 상품 번호 목록 (다중 선택 가능)
 *   <li>itemName: 선택, 부분 일치 검색 (LIKE)
 *   <li>brandName: 선택, 부분 일치 검색 (LIKE)
 *   <li>minPrice: 선택, 최소 가격
 *   <li>maxPrice: 선택, 최대 가격
 *   <li>needsSync: 선택, 동기화 필요 여부
 *   <li>allCrawled: 선택, 모든 크롤링 완료 여부
 *   <li>hasExternalId: 선택, 외부 상품 ID 존재 여부
 *   <li>createdFrom: 선택, 생성일시 시작 (ISO-8601)
 *   <li>createdTo: 선택, 생성일시 종료 (ISO-8601)
 *   <li>page: 최소 0 (기본값: 0)
 *   <li>size: 1-100 (기본값: 20)
 * </ul>
 *
 * @param sellerId 셀러 ID 필터 (선택)
 * @param itemNos 상품 번호 목록 필터 (선택, 다중 선택 가능)
 * @param itemName 상품명 부분 일치 검색 (선택)
 * @param brandName 브랜드명 부분 일치 검색 (선택)
 * @param minPrice 최소 가격 필터 (선택)
 * @param maxPrice 최대 가격 필터 (선택)
 * @param needsSync 동기화 필요 여부 필터 (선택)
 * @param allCrawled 모든 크롤링 완료 여부 필터 (선택)
 * @param hasExternalId 외부 상품 ID 존재 여부 필터 (선택)
 * @param createdFrom 생성일시 시작 (선택)
 * @param createdTo 생성일시 종료 (선택)
 * @param page 페이지 번호 (0부터 시작, 기본값: 0)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawledProductsApiRequest(
        @Min(value = 1, message = "셀러 ID는 1 이상이어야 합니다")
                @Schema(description = "셀러 ID 필터", example = "1")
                Long sellerId,
        @Schema(description = "상품 번호 목록 필터 (다중 선택 가능)", example = "[1001, 1002, 1003]")
                List<Long> itemNos,
        @Schema(description = "상품명 부분 일치 검색 (LIKE)", example = "나이키") String itemName,
        @Schema(description = "브랜드명 부분 일치 검색 (LIKE)", example = "NIKE") String brandName,
        @Min(value = 0, message = "최소 가격은 0 이상이어야 합니다")
                @Schema(description = "최소 가격 필터", example = "10000")
                Long minPrice,
        @Min(value = 0, message = "최대 가격은 0 이상이어야 합니다")
                @Schema(description = "최대 가격 필터", example = "100000")
                Long maxPrice,
        @Schema(description = "동기화 필요 여부 필터", example = "true") Boolean needsSync,
        @Schema(description = "모든 크롤링 완료 여부 필터", example = "false") Boolean allCrawled,
        @Schema(description = "외부 상품 ID 존재 여부 필터", example = "true") Boolean hasExternalId,
        @Schema(description = "생성일시 시작 (ISO-8601)", example = "2025-01-01T00:00:00Z")
                Instant createdFrom,
        @Schema(description = "생성일시 종료 (ISO-8601)", example = "2025-12-31T23:59:59Z")
                Instant createdTo,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
                @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
                Integer page,
        @Min(value = 1, message = "페이지 크기는 최소 1이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 최대 100까지 허용됩니다")
                @Schema(description = "페이지 크기 (최대 100)", example = "20")
                Integer size) {

    /** 기본값 적용 생성자 */
    public SearchCrawledProductsApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
