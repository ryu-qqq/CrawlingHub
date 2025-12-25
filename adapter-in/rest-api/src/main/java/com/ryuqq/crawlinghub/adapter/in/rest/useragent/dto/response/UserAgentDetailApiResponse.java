package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
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
public record UserAgentDetailApiResponse(
        long id,
        String userAgentValue,
        DeviceType deviceType,
        UserAgentStatus status,
        int healthScore,
        int requestsPerDay,
        Instant lastUsedAt,
        Instant createdAt,
        Instant updatedAt,
        PoolInfoApiResponse poolInfo) {

    /**
     * Pool 정보 API Response
     *
     * @param isInPool Pool에 존재하는지 여부
     * @param remainingTokens 남은 토큰 수
     * @param hasValidSession 유효한 세션이 있는지 여부
     * @param sessionExpiresAt 세션 만료 시각
     */
    public record PoolInfoApiResponse(
            boolean isInPool,
            int remainingTokens,
            boolean hasValidSession,
            Instant sessionExpiresAt) {}
}
