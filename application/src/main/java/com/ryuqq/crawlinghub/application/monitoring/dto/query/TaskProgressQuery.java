package com.ryuqq.crawlinghub.application.monitoring.dto.query;

/**
 * 태스크 진행 상황 조회 Query
 *
 * @param sellerId 셀러 ID
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record TaskProgressQuery(
    Long sellerId
) {
    public TaskProgressQuery {
        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("셀러 ID는 필수입니다");
        }
    }
}
