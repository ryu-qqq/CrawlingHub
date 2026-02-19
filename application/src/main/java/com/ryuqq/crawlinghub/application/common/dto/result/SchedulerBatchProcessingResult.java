package com.ryuqq.crawlinghub.application.common.dto.result;

/**
 * 스케줄러/배치 작업의 간단한 처리 결과
 *
 * @param total 총 건수
 * @param success 성공 건수
 * @param failed 실패 건수
 */
public record SchedulerBatchProcessingResult(int total, int success, int failed) {

    public static SchedulerBatchProcessingResult of(int total, int success, int failed) {
        return new SchedulerBatchProcessingResult(total, success, failed);
    }

    public static SchedulerBatchProcessingResult empty() {
        return new SchedulerBatchProcessingResult(0, 0, 0);
    }

    public boolean hasFailures() {
        return failed > 0;
    }
}
