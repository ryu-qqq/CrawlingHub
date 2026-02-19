package com.ryuqq.crawlinghub.application.common.dto.result;

import java.util.List;

/**
 * 일괄 처리 전체 결과
 *
 * @param <T> ID 타입
 * @param totalCount 총 건수
 * @param successCount 성공 건수
 * @param failureCount 실패 건수
 * @param results 개별 결과 목록
 */
public record BatchProcessingResult<T>(
        int totalCount, int successCount, int failureCount, List<BatchItemResult<T>> results) {

    public static <T> BatchProcessingResult<T> from(List<BatchItemResult<T>> results) {
        int total = results.size();
        int success = (int) results.stream().filter(r -> r.success()).count();
        int failure = total - success;
        return new BatchProcessingResult<>(total, success, failure, results);
    }
}
