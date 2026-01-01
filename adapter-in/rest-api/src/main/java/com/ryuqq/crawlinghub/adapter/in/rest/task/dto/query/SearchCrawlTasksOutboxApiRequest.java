package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.List;

/**
 * Search CrawlTasksOutbox API Request
 *
 * <p>CrawlTask Outbox 목록 조회 API 요청 DTO
 *
 * <p><strong>Query Parameters:</strong>
 *
 * <ul>
 *   <li>statuses: 상태 필터 목록 (PENDING, FAILED, SENT) - 다중 선택 가능
 *   <li>createdFrom: 생성일 시작 범위 (ISO-8601, inclusive)
 *   <li>createdTo: 생성일 종료 범위 (ISO-8601, exclusive)
 *   <li>page: 페이지 번호 (0부터 시작, 기본값: 0)
 *   <li>size: 페이지 크기 (기본값: 20, 최대: 100)
 * </ul>
 *
 * @param statuses 상태 필터 목록 (PENDING, FAILED, SENT) - 다중 선택 가능
 * @param createdFrom 생성일 시작 범위 (ISO-8601, inclusive)
 * @param createdTo 생성일 종료 범위 (ISO-8601, exclusive)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawlTasksOutboxApiRequest(
        @Schema(
                        description = "상태 필터 목록 (PENDING, FAILED, SENT) - 다중 선택 가능",
                        example = "[\"PENDING\", \"FAILED\"]")
                List<String> statuses,
        @Schema(description = "생성일 시작 범위 (ISO-8601, inclusive)", example = "2024-01-01T00:00:00Z")
                Instant createdFrom,
        @Schema(description = "생성일 종료 범위 (ISO-8601, exclusive)", example = "2024-12-31T23:59:59Z")
                Instant createdTo,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
                @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
                Integer page,
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                @Schema(description = "페이지 크기 (기본값: 20, 최대: 100)", example = "20")
                Integer size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    /** Compact Constructor - 기본값 설정 */
    public SearchCrawlTasksOutboxApiRequest {
        if (page == null) {
            page = DEFAULT_PAGE;
        }
        if (size == null) {
            size = DEFAULT_SIZE;
        }
    }
}
