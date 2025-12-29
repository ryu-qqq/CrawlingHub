package com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * CrawlExecution 목록 조회 API Request DTO
 *
 * <p>Query 파라미터로 CrawlExecution 목록 필터링
 *
 * <p><strong>필터 조건</strong>:
 *
 * <ul>
 *   <li>crawlTaskId: 태스크 ID 필터 (optional)
 *   <li>crawlSchedulerId: 스케줄러 ID 필터 (optional)
 *   <li>sellerId: 셀러 ID 필터 (optional)
 *   <li>statuses: 상태 필터 목록 (optional, RUNNING|SUCCESS|FAILED|TIMEOUT, 다중 선택 가능)
 *   <li>from: 조회 시작 시간 (optional, ISO-8601)
 *   <li>to: 조회 종료 시간 (optional, ISO-8601)
 * </ul>
 *
 * @param crawlTaskId 태스크 ID 필터 (optional)
 * @param crawlSchedulerId 스케줄러 ID 필터 (optional)
 * @param sellerId 셀러 ID 필터 (optional)
 * @param statuses 상태 필터 목록 (optional, 다중 선택 가능)
 * @param from 조회 시작 시간 (optional)
 * @param to 조회 종료 시간 (optional)
 * @param page 페이지 번호 (기본값: 0)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawlExecutionsApiRequest(
        @Positive(message = "태스크 ID는 양수여야 합니다") @Schema(description = "태스크 ID 필터", example = "1")
                Long crawlTaskId,
        @Positive(message = "스케줄러 ID는 양수여야 합니다") @Schema(description = "스케줄러 ID 필터", example = "1")
                Long crawlSchedulerId,
        @Positive(message = "셀러 ID는 양수여야 합니다") @Schema(description = "셀러 ID 필터", example = "1")
                Long sellerId,
        @Schema(description = "상태 필터 목록 (다중 선택 가능)", example = "[\"SUCCESS\", \"FAILED\"]")
                List<String> statuses,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                @Schema(description = "조회 시작 시간 (ISO-8601)", example = "2025-01-01T00:00:00")
                LocalDateTime from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                @Schema(description = "조회 종료 시간 (ISO-8601)", example = "2025-12-31T23:59:59")
                LocalDateTime to,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
                @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
                Integer page,
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                @Schema(description = "페이지 크기 (최대 100)", example = "20")
                Integer size) {

    /** Compact Constructor - 기본값 설정 */
    public SearchCrawlExecutionsApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
