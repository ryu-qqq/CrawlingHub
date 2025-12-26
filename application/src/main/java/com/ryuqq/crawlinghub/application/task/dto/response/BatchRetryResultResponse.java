package com.ryuqq.crawlinghub.application.task.dto.response;

import java.util.List;

/**
 * 배치 재처리 결과 응답 DTO
 *
 * <p>여러 Task 재처리 요청에 대한 성공/실패 결과를 반환합니다.
 *
 * @param totalRequested 요청된 총 Task 수
 * @param successCount 성공한 Task 수
 * @param failedCount 실패한 Task 수
 * @param successIds 성공한 Task ID 목록
 * @param failures 실패한 Task 정보 목록
 * @author development-team
 * @since 1.0.0
 */
public record BatchRetryResultResponse(
        int totalRequested,
        int successCount,
        int failedCount,
        List<Long> successIds,
        List<RetryFailureItem> failures) {

    /**
     * 재처리 실패 항목
     *
     * @param crawlTaskId 실패한 Task ID
     * @param reason 실패 사유
     */
    public record RetryFailureItem(Long crawlTaskId, String reason) {}

    public static BatchRetryResultResponse of(
            List<Long> successIds, List<RetryFailureItem> failures) {
        return new BatchRetryResultResponse(
                successIds.size() + failures.size(),
                successIds.size(),
                failures.size(),
                successIds,
                failures);
    }
}
