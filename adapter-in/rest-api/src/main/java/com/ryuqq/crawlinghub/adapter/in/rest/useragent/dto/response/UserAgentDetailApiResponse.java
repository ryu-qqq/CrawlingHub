package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * UserAgent 상세 정보 API Response
 *
 * <p>개별 UserAgent 조회 API 응답 DTO
 *
 * @param id UserAgent ID
 * @param userAgentValue User-Agent 문자열
 * @param deviceType 디바이스 타입
 * @param status 현재 상태
 * @param healthScore 건강 점수 (0-100)
 * @param requestsPerDay 일일 요청 수
 * @param lastUsedAt 마지막 사용 시각
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param poolInfo Redis Pool 정보
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "UserAgent 상세 정보 API 응답")
public record UserAgentDetailApiResponse(
        @Schema(description = "UserAgent ID", example = "1") long id,
        @Schema(
                        description = "User-Agent 문자열",
                        example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                String userAgentValue,
        @Schema(description = "디바이스 타입") DeviceType deviceType,
        @Schema(description = "현재 상태", example = "AVAILABLE") UserAgentStatus status,
        @Schema(description = "건강 점수 (0-100)", example = "95") int healthScore,
        @Schema(description = "일일 요청 수", example = "150") int requestsPerDay,
        @Schema(description = "마지막 사용 시각") Instant lastUsedAt,
        @Schema(description = "생성 시각") Instant createdAt,
        @Schema(description = "수정 시각") Instant updatedAt,
        @Schema(description = "Redis Pool 정보") PoolInfoApiResponse poolInfo) {

    /**
     * Pool 정보 API Response
     *
     * @param isInPool Pool에 존재하는지 여부
     * @param remainingTokens 남은 토큰 수
     * @param hasValidSession 유효한 세션이 있는지 여부
     * @param sessionExpiresAt 세션 만료 시각
     */
    @Schema(description = "Redis Pool 정보")
    public record PoolInfoApiResponse(
            @Schema(description = "Pool에 존재하는지 여부", example = "true") boolean isInPool,
            @Schema(description = "남은 토큰 수", example = "45") int remainingTokens,
            @Schema(description = "유효한 세션이 있는지 여부", example = "true") boolean hasValidSession,
            @Schema(description = "세션 만료 시각") Instant sessionExpiresAt) {}
}
