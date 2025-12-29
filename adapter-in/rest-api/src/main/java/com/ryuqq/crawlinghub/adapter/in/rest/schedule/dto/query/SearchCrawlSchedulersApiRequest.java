package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;

/**
 * Search CrawlSchedulers API Request
 *
 * <p>크롤 스케줄러 목록 조회 API 요청 DTO
 *
 * <p><strong>Query Parameters:</strong>
 *
 * <ul>
 *   <li>sellerId: 셀러 ID 필터 (선택적)
 *   <li>statuses: 상태 필터 목록 (선택적, ACTIVE/INACTIVE, 다중 선택 가능)
 *   <li>createdFrom: 생성일 시작 (선택적, ISO-8601 형식)
 *   <li>createdTo: 생성일 종료 (선택적, ISO-8601 형식)
 *   <li>page: 페이지 번호 (선택적, 기본값: 0)
 *   <li>size: 페이지 크기 (선택적, 기본값: 20, 최대: 100)
 * </ul>
 *
 * @param sellerId 셀러 ID 필터 (선택적)
 * @param statuses 상태 필터 목록 (선택적, 다중 선택 가능)
 * @param createdFrom 생성일 시작 (선택적)
 * @param createdTo 생성일 종료 (선택적)
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
        @Schema(description = "생성일 시작 (ISO-8601)", example = "2025-01-01T00:00:00Z")
                Instant createdFrom,
        @Schema(description = "생성일 종료 (ISO-8601)", example = "2025-12-31T23:59:59Z")
                Instant createdTo,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
                @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
                Integer page,
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                @Schema(description = "페이지 크기 (최대 100)", example = "20")
                Integer size) {

    /**
     * 기본값 적용 생성자
     *
     * @param sellerId 셀러 ID
     * @param statuses 상태 목록
     * @param createdFrom 생성일 시작
     * @param createdTo 생성일 종료
     * @param page 페이지 번호 (null이면 0)
     * @param size 페이지 크기 (null이면 20)
     */
    public SearchCrawlSchedulersApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
