package com.ryuqq.crawlinghub.application.schedule.dto.response;

import java.time.Instant;

/**
 * 스케줄러 상세 조회 시 포함되는 태스크 요약 정보
 *
 * @param taskId 태스크 ID
 * @param status 태스크 상태
 * @param taskType 태스크 유형
 * @param createdAt 생성 시각
 * @param completedAt 완료 시각 (null 가능)
 * @author development-team
 * @since 1.0.0
 */
public record TaskSummaryForScheduler(
        Long taskId, String status, String taskType, Instant createdAt, Instant completedAt) {}
