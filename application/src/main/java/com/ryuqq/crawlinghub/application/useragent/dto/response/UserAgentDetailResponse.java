package com.ryuqq.crawlinghub.application.useragent.dto.response;

import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;

/**
 * UserAgent 상세 정보 응답 DTO
 *
 * <p>개별 UserAgent 조회 시 반환되는 상세 정보입니다.
 *
 * @param id UserAgent ID
 * @param userAgentValue User-Agent 문자열 (전체)
 * @param deviceType 디바이스 타입
 * @param status 현재 상태
 * @param healthScore 건강 점수 (0-100)
 * @param requestsPerDay 일일 요청 수
 * @param lastUsedAt 마지막 사용 시각
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param poolInfo Redis Pool 정보 (null if not in pool)
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentDetailResponse(
        long id,
        String userAgentValue,
        DeviceType deviceType,
        UserAgentStatus status,
        int healthScore,
        int requestsPerDay,
        Instant lastUsedAt,
        Instant createdAt,
        Instant updatedAt,
        PoolInfo poolInfo) {

    /**
     * Redis Pool 정보
     *
     * @param isInPool Pool에 존재하는지 여부
     * @param remainingTokens 남은 토큰 수
     * @param hasValidSession 유효한 세션이 있는지 여부
     * @param sessionExpiresAt 세션 만료 시각
     */
    public record PoolInfo(
            boolean isInPool,
            int remainingTokens,
            boolean hasValidSession,
            Instant sessionExpiresAt) {

        public static PoolInfo notInPool() {
            return new PoolInfo(false, 0, false, null);
        }

        public static PoolInfo of(
                int remainingTokens, boolean hasValidSession, Instant sessionExpiresAt) {
            return new PoolInfo(true, remainingTokens, hasValidSession, sessionExpiresAt);
        }
    }

    /**
     * UserAgentDetailResponse 생성 (DB 데이터만)
     *
     * @param id UserAgent ID
     * @param userAgentValue User-Agent 문자열
     * @param deviceType 디바이스 타입
     * @param status 현재 상태
     * @param healthScore 건강 점수
     * @param requestsPerDay 일일 요청 수
     * @param lastUsedAt 마지막 사용 시각
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return UserAgentDetailResponse
     */
    public static UserAgentDetailResponse of(
            long id,
            String userAgentValue,
            DeviceType deviceType,
            UserAgentStatus status,
            int healthScore,
            int requestsPerDay,
            Instant lastUsedAt,
            Instant createdAt,
            Instant updatedAt) {
        return new UserAgentDetailResponse(
                id,
                userAgentValue,
                deviceType,
                status,
                healthScore,
                requestsPerDay,
                lastUsedAt,
                createdAt,
                updatedAt,
                PoolInfo.notInPool());
    }

    /**
     * Pool 정보 포함 UserAgentDetailResponse 생성
     *
     * @param id UserAgent ID
     * @param userAgentValue User-Agent 문자열
     * @param deviceType 디바이스 타입
     * @param status 현재 상태
     * @param healthScore 건강 점수
     * @param requestsPerDay 일일 요청 수
     * @param lastUsedAt 마지막 사용 시각
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @param poolInfo Redis Pool 정보
     * @return UserAgentDetailResponse
     */
    public static UserAgentDetailResponse withPoolInfo(
            long id,
            String userAgentValue,
            DeviceType deviceType,
            UserAgentStatus status,
            int healthScore,
            int requestsPerDay,
            Instant lastUsedAt,
            Instant createdAt,
            Instant updatedAt,
            PoolInfo poolInfo) {
        return new UserAgentDetailResponse(
                id,
                userAgentValue,
                deviceType,
                status,
                healthScore,
                requestsPerDay,
                lastUsedAt,
                createdAt,
                updatedAt,
                poolInfo);
    }
}
