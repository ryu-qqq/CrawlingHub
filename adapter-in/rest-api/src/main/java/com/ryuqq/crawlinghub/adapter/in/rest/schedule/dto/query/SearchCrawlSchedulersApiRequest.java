package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Search CrawlSchedulers API Request
 *
 * <p>크롤 스케줄러 목록 조회 API 요청 DTO
 *
 * @param sellerId 셀러 ID 필터 (선택적)
 * @param statuses 상태 필터 목록 (선택적, 다중 선택 가능)
 * @param searchField 검색 필드 (선택적, schedulerName)
 * @param searchWord 검색어 (선택적)
 * @param sortKey 정렬 키 (선택적, createdAt/updatedAt/schedulerName)
 * @param sortDirection 정렬 방향 (선택적, ASC/DESC)
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawlSchedulersApiRequest(
        @Positive(message = "셀러 ID는 양수여야 합니다") @Schema(description = "셀러 ID 필터", example = "1")
                Long sellerId,
        @Schema(description = "상태 필터 목록 (다중 선택 가능)", example = "[\"ACTIVE\", \"INACTIVE\"]")
                List<String> statuses,
        @Schema(description = "검색 필드", example = "schedulerName") String searchField,
        @Schema(description = "검색어", example = "DAILY") String searchWord,
        @Schema(description = "정렬 키 (createdAt, updatedAt, schedulerName)", example = "createdAt")
                String sortKey,
        @Schema(description = "정렬 방향 (ASC, DESC)", example = "DESC") String sortDirection,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
                @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
                Integer page,
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                @Schema(description = "페이지 크기 (최대 100)", example = "20")
                Integer size) {

    public SearchCrawlSchedulersApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
