package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.List;

/**
 * Search CrawlTasks API Request
 *
 * <p>크롤 태스크 목록 조회 API 요청 DTO
 *
 * <p><strong>Query Parameters:</strong>
 *
 * <ul>
 *   <li>crawlSchedulerIds: 크롤 스케줄러 ID 필터 목록 (선택적, 다중 선택 가능)
 *   <li>sellerIds: 셀러 ID 필터 목록 (선택적, 다중 선택 가능)
 *   <li>statuses: 상태 필터 목록 (선택적, 다중 선택 가능)
 *   <li>taskTypes: 태스크 유형 필터 목록 (선택적, 다중 선택 가능)
 *   <li>createdFrom: 생성일시 시작 (선택적, ISO-8601 형식)
 *   <li>createdTo: 생성일시 종료 (선택적, ISO-8601 형식)
 *   <li>page: 페이지 번호 (선택적, 기본값: 0)
 *   <li>size: 페이지 크기 (선택적, 기본값: 20, 최대: 100)
 * </ul>
 *
 * @param crawlSchedulerIds 크롤 스케줄러 ID 필터 목록 (선택적, 다중 선택 가능)
 * @param sellerIds 셀러 ID 필터 목록 (선택적, 다중 선택 가능)
 * @param statuses 상태 필터 목록 (선택적, 다중 선택 가능)
 * @param taskTypes 태스크 유형 필터 목록 (선택적, 다중 선택 가능)
 * @param createdFrom 생성일시 시작 (선택적)
 * @param createdTo 생성일시 종료 (선택적)
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawlTasksApiRequest(
        @Schema(description = "크롤 스케줄러 ID 필터 목록 (다중 선택 가능)", example = "[1, 2]")
                List<Long> crawlSchedulerIds,
        @Schema(description = "셀러 ID 필터 목록 (다중 선택 가능)", example = "[100, 200]")
                List<Long> sellerIds,
        @Schema(description = "상태 필터 목록 (다중 선택 가능)", example = "[\"SUCCESS\", \"FAILED\"]")
                List<String> statuses,
        @Schema(description = "태스크 유형 필터 목록 (다중 선택 가능)", example = "[\"META\", \"DETAIL\"]")
                List<String> taskTypes,
        @Schema(description = "생성일시 시작 (ISO-8601)", example = "2025-01-01T00:00:00Z")
                Instant createdFrom,
        @Schema(description = "생성일시 종료 (ISO-8601)", example = "2025-12-31T23:59:59Z")
                Instant createdTo,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
                @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
                Integer page,
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                @Schema(description = "페이지 크기 (최대 100)", example = "20")
                Integer size) {

    /** Compact Constructor - 기본값 설정 */
    public SearchCrawlTasksApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
