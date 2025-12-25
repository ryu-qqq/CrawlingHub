package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Task 통계 조회 API 요청 DTO
 *
 * <p>어드민용 Task 통계/대시보드 조회 요청 파라미터입니다.
 *
 * @param schedulerId 스케줄러 ID 필터 (선택)
 * @param sellerId 셀러 ID 필터 (선택)
 * @param from 조회 시작 일시 (선택)
 * @param to 조회 종료 일시 (선택)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Task 통계 조회 요청")
public record GetTaskStatisticsApiRequest(
        @Schema(description = "스케줄러 ID 필터", example = "1") Long schedulerId,
        @Schema(description = "셀러 ID 필터", example = "1") Long sellerId,
        @Schema(description = "조회 시작 일시 (ISO-8601)", example = "2025-01-01T00:00:00")
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                LocalDateTime from,
        @Schema(description = "조회 종료 일시 (ISO-8601)", example = "2025-12-31T23:59:59")
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                LocalDateTime to) {}
