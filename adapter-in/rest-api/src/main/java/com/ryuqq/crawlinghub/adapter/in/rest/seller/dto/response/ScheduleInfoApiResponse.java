package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * ScheduleInfoApiResponse - 스케줄 정보 API 응답 DTO
 *
 * @param scheduleId 스케줄 ID
 * @param cronExpression Cron 표현식
 * @param status 상태
 * @param nextExecutionTime 다음 실행 시간
 * @param createdAt 생성 일시
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ScheduleInfoApiResponse(
    Long scheduleId,
    String cronExpression,
    String status,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime nextExecutionTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {}

