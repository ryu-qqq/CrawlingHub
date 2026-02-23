package com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto;

import java.time.LocalDateTime;

/**
 * 최근 태스크 목록용 DTO
 *
 * <p>QueryDSL Projections.constructor 용 Persistence 레이어 전용 DTO
 *
 * @param taskId 태스크 ID
 * @param status 태스크 상태
 * @param taskType 태스크 유형
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerTaskSummaryDto(
        Long taskId,
        String status,
        String taskType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
