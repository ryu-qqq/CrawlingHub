package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * UserAgent 요약 정보 API 응답 DTO
 *
 * <p>UserAgent 목록 조회 시 반환되는 요약 정보입니다.
 *
 * @param id UserAgent ID
 * @param userAgentValue User-Agent 문자열 (일부만 표시)
 * @param deviceType 디바이스 타입
 * @param status 현재 상태
 * @param healthScore 건강 점수 (0-100)
 * @param requestsPerDay 일일 요청 수
 * @param lastUsedAt 마지막 사용 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param createdAt 생성 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param updatedAt 수정 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "UserAgent 요약 정보")
public record UserAgentSummaryApiResponse(
        @Schema(description = "UserAgent ID", example = "1") long id,
        @Schema(
                        description = "User-Agent 문자열 (최대 100자)",
                        example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...")
                String userAgentValue,
        @Schema(description = "디바이스 타입", example = "DESKTOP") DeviceType deviceType,
        @Schema(description = "현재 상태", example = "AVAILABLE") UserAgentStatus status,
        @Schema(description = "건강 점수 (0-100)", example = "85") int healthScore,
        @Schema(description = "일일 요청 수", example = "150") int requestsPerDay,
        @Schema(description = "마지막 사용 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String lastUsedAt,
        @Schema(description = "생성 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String createdAt,
        @Schema(description = "수정 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String updatedAt) {}
