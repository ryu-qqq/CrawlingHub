package com.ryuqq.crawlinghub.application.seller.dto.response;

import java.time.Instant;

/**
 * Task Summary
 *
 * <p>셀러 상세 조회 시 포함되는 태스크 요약 정보
 *
 * @param taskId 태스크 ID
 * @param status 태스크 상태
 * @param taskType 태스크 유형
 * @param createdAt 생성 시각
 * @author development-team
 * @since 1.0.0
 */
public record TaskSummary(Long taskId, String status, String taskType, Instant createdAt) {}
