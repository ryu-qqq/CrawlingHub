package com.ryuqq.crawlinghub.application.task.dto.query;

import java.time.LocalDateTime;

/**
 * Task 통계 조회 Query DTO
 *
 * @param schedulerId 스케줄러 ID 필터 (nullable)
 * @param sellerId 셀러 ID 필터 (nullable)
 * @param from 시작 시각 (nullable)
 * @param to 종료 시각 (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record GetTaskStatisticsQuery(
        Long schedulerId, Long sellerId, LocalDateTime from, LocalDateTime to) {

    public static GetTaskStatisticsQuery of(
            Long schedulerId, Long sellerId, LocalDateTime from, LocalDateTime to) {
        return new GetTaskStatisticsQuery(schedulerId, sellerId, from, to);
    }
}
