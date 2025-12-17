package com.ryuqq.crawlinghub.application.useragent.dto.response;

import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;

/**
 * UserAgent 요약 정보 응답 DTO
 *
 * <p>UserAgent 목록 조회 시 반환되는 요약 정보입니다.
 *
 * @param id UserAgent ID
 * @param userAgentValue User-Agent 문자열 (일부만 표시)
 * @param deviceType 디바이스 타입
 * @param status 현재 상태
 * @param healthScore 건강 점수 (0-100)
 * @param requestsPerDay 일일 요청 수
 * @param lastUsedAt 마지막 사용 시각
 * @param createdAt 생성 시각
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentSummaryResponse(
        long id,
        String userAgentValue,
        DeviceType deviceType,
        UserAgentStatus status,
        int healthScore,
        int requestsPerDay,
        Instant lastUsedAt,
        Instant createdAt) {

    /**
     * UserAgentSummaryResponse 생성
     *
     * @param id UserAgent ID
     * @param userAgentValue User-Agent 문자열
     * @param deviceType 디바이스 타입
     * @param status 현재 상태
     * @param healthScore 건강 점수
     * @param requestsPerDay 일일 요청 수
     * @param lastUsedAt 마지막 사용 시각
     * @param createdAt 생성 시각
     * @return UserAgentSummaryResponse
     */
    public static UserAgentSummaryResponse of(
            long id,
            String userAgentValue,
            DeviceType deviceType,
            UserAgentStatus status,
            int healthScore,
            int requestsPerDay,
            Instant lastUsedAt,
            Instant createdAt) {
        return new UserAgentSummaryResponse(
                id,
                truncateUserAgentValue(userAgentValue),
                deviceType,
                status,
                healthScore,
                requestsPerDay,
                lastUsedAt,
                createdAt);
    }

    /**
     * User-Agent 문자열이 너무 길면 잘라서 반환
     *
     * @param userAgentValue 원본 문자열
     * @return 잘린 문자열 (최대 100자)
     */
    private static String truncateUserAgentValue(String userAgentValue) {
        if (userAgentValue == null) {
            return null;
        }
        int maxLength = 100;
        if (userAgentValue.length() <= maxLength) {
            return userAgentValue;
        }
        return userAgentValue.substring(0, maxLength) + "...";
    }
}
