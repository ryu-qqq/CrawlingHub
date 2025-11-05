package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * ScheduleHistoryApiResponse - 스케줄 실행 이력 API 응답 DTO
 *
 * @param historyId 이력 ID
 * @param startedAt 시작 시간
 * @param completedAt 완료 시간
 * @param status 상태
 * @param message 메시지
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ScheduleHistoryApiResponse(
    Long historyId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startedAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime completedAt,
    String status,
    String message
) {}

