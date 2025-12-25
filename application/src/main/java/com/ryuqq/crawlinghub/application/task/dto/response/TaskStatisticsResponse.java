package com.ryuqq.crawlinghub.application.task.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Task 통계 Response DTO
 *
 * @param period 조회 기간 정보
 * @param summary 전체 요약 정보
 * @param failureAnalysis 실패 분석 정보
 * @param byTaskType 태스크 유형별 통계
 * @author development-team
 * @since 1.0.0
 */
public record TaskStatisticsResponse(
        PeriodInfo period,
        SummaryInfo summary,
        FailureAnalysis failureAnalysis,
        Map<String, TaskTypeStats> byTaskType) {

    public TaskStatisticsResponse {
        if (byTaskType != null) {
            byTaskType = Map.copyOf(byTaskType);
        }
    }

    /** 조회 기간 정보 */
    public record PeriodInfo(Instant from, Instant to) {}

    /** 전체 요약 정보 */
    public record SummaryInfo(long total, Map<String, Long> byStatus, double successRate) {
        public SummaryInfo {
            if (byStatus != null) {
                byStatus = Map.copyOf(byStatus);
            }
        }
    }

    /** 실패 분석 정보 */
    public record FailureAnalysis(List<ErrorSummary> topErrors) {
        public FailureAnalysis {
            if (topErrors != null) {
                topErrors = List.copyOf(topErrors);
            }
        }
    }

    /** 에러 요약 */
    public record ErrorSummary(String error, long count, double percentage) {}

    /** 태스크 유형별 통계 */
    public record TaskTypeStats(long total, long success, long failed) {}
}
