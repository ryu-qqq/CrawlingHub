package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Task 상세 + Execution 이력 API 응답 DTO
 *
 * <p>어드민용 Task 상세 조회 응답입니다. Task 정보와 최근 실행 이력을 함께 포함합니다.
 *
 * @param task Task 상세 정보
 * @param executionHistory 최근 실행 이력 목록
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Task 상세 + Execution 이력 응답")
public record TaskWithExecutionsApiResponse(
        @Schema(description = "Task 상세 정보") TaskInfoApiResponse task,
        @Schema(description = "최근 실행 이력 목록")
                List<ExecutionHistoryItemApiResponse> executionHistory) {

    /** Task 정보 */
    @Schema(description = "Task 정보")
    public record TaskInfoApiResponse(
            @Schema(description = "Task ID", example = "1") Long crawlTaskId,
            @Schema(description = "스케줄러 ID", example = "1") Long crawlSchedulerId,
            @Schema(description = "셀러 ID", example = "1") Long sellerId,
            @Schema(description = "Task 상태", example = "SUCCESS") String status,
            @Schema(description = "Task 유형", example = "DETAIL") String taskType,
            @Schema(description = "재시도 횟수", example = "0") int retryCount,
            @Schema(description = "Base URL", example = "https://api.example.com") String baseUrl,
            @Schema(description = "Path", example = "/products/123") String path,
            @Schema(description = "전체 URL", example = "https://api.example.com/products/123")
                    String fullUrl,
            @Schema(description = "생성 일시", example = "2025-01-15T10:30:00Z") String createdAt,
            @Schema(description = "수정 일시", example = "2025-01-15T10:35:00Z") String updatedAt) {}

    /** Execution 이력 항목 */
    @Schema(description = "Execution 이력 항목")
    public record ExecutionHistoryItemApiResponse(
            @Schema(description = "Execution ID", example = "100") Long executionId,
            @Schema(description = "실행 상태", example = "SUCCESS") String status,
            @Schema(description = "HTTP 상태 코드", example = "200") Integer httpStatusCode,
            @Schema(description = "에러 메시지", example = "Connection timeout") String errorMessage,
            @Schema(description = "소요 시간 (ms)", example = "1234") Long durationMs,
            @Schema(description = "시작 일시", example = "2025-01-15T10:30:00Z") String startedAt,
            @Schema(description = "완료 일시", example = "2025-01-15T10:30:01Z") String completedAt) {}
}
