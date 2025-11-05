package com.ryuqq.crawlinghub.application.seller.dto.response;

import java.time.LocalDateTime;

/**
 * ScheduleHistoryResponse - 스케줄 실행 이력 응답 DTO
 *
 * @param historyId 이력 ID
 * @param startedAt 시작 시간
 * @param completedAt 완료 시간
 * @param status 실행 상태 (SUCCESS, FAILURE)
 * @param message 실행 메시지
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ScheduleHistoryResponse(
    Long historyId,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    String status,
    String message
) {}

