package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

/**
 * Task 통계 API 응답 DTO
 *
 * <p>어드민용 Task 통계/대시보드 응답입니다.
 *
 * @param period 조회 기간 정보
 * @param summary 전체 요약 정보
 * @param failureAnalysis 실패 분석 정보
 * @param byTaskType 태스크 유형별 통계
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Task 통계 응답")
public record TaskStatisticsApiResponse(
        @Schema(description = "조회 기간 정보") PeriodInfoApiResponse period,
        @Schema(description = "전체 요약 정보") SummaryInfoApiResponse summary,
        @Schema(description = "실패 분석 정보") FailureAnalysisApiResponse failureAnalysis,
        @Schema(description = "태스크 유형별 통계") Map<String, TaskTypeStatsApiResponse> byTaskType) {

    /** 조회 기간 정보 */
    @Schema(description = "조회 기간 정보")
    public record PeriodInfoApiResponse(
            @Schema(description = "조회 시작 일시", example = "2025-01-01T00:00:00Z") String from,
            @Schema(description = "조회 종료 일시", example = "2025-12-31T23:59:59Z") String to) {}

    /** 전체 요약 정보 */
    @Schema(description = "전체 요약 정보")
    public record SummaryInfoApiResponse(
            @Schema(description = "전체 태스크 수", example = "1000") long total,
            @Schema(description = "상태별 태스크 수") Map<String, Long> byStatus,
            @Schema(description = "성공률 (%)", example = "95.5") double successRate) {}

    /** 실패 분석 정보 */
    @Schema(description = "실패 분석 정보")
    public record FailureAnalysisApiResponse(
            @Schema(description = "상위 에러 목록") List<ErrorSummaryApiResponse> topErrors) {}

    /** 에러 요약 */
    @Schema(description = "에러 요약")
    public record ErrorSummaryApiResponse(
            @Schema(description = "에러 메시지", example = "Connection timeout") String error,
            @Schema(description = "발생 횟수", example = "50") long count,
            @Schema(description = "비율 (%)", example = "25.0") double percentage) {}

    /** 태스크 유형별 통계 */
    @Schema(description = "태스크 유형별 통계")
    public record TaskTypeStatsApiResponse(
            @Schema(description = "전체 수", example = "250") long total,
            @Schema(description = "성공 수", example = "240") long success,
            @Schema(description = "실패 수", example = "10") long failed) {}
}
