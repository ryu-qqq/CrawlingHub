package com.ryuqq.crawlinghub.adapter.out.redis.support;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CachedUserAgent ↔ Redis Hash 양방향 매핑
 *
 * <p><strong>Phase 2 변경사항</strong>:
 *
 * <ul>
 *   <li>READY → IDLE 레거시 매핑 추가
 *   <li>AVAILABLE → IDLE 레거시 매핑 유지
 *   <li>borrowedAt, cooldownUntil, consecutiveRateLimits 필드 읽기 (Phase 4에서 CachedUserAgent에 반영)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CachedUserAgentRedisMapper {

    private static final Logger log = LoggerFactory.getLogger(CachedUserAgentRedisMapper.class);

    /**
     * Redis Hash → CachedUserAgent
     *
     * @param data Redis Hash 데이터
     * @return CachedUserAgent
     */
    public CachedUserAgent mapToCachedUserAgent(Map<String, String> data) {
        Long userAgentId = Long.parseLong(data.get("userAgentId"));
        String userAgentValue = data.get("userAgentValue");
        String sessionToken = data.get("sessionToken");
        String nid = data.get("nid");
        String mustitUid = data.get("mustitUid");
        int remainingTokens = Integer.parseInt(data.getOrDefault("remainingTokens", "80"));
        int maxTokens = Integer.parseInt(data.getOrDefault("maxTokens", "80"));
        int healthScore = Integer.parseInt(data.getOrDefault("healthScore", "100"));
        UserAgentStatus status =
                parseStatus(data.getOrDefault("status", UserAgentStatus.SESSION_REQUIRED.name()));

        Instant sessionExpiresAt = parseInstant(data.get("sessionExpiresAt"));
        Instant windowStart = parseInstant(data.get("windowStart"));
        Instant windowEnd = parseInstant(data.get("windowEnd"));
        Instant suspendedAt = parseInstant(data.get("suspendedAt"));

        // Phase 4: 신규 필드 읽기
        Instant borrowedAt = parseInstant(data.get("borrowedAt"));
        Instant cooldownUntil = parseInstant(data.get("cooldownUntil"));
        int consecutiveRateLimits =
                Integer.parseInt(data.getOrDefault("consecutiveRateLimits", "0"));

        if (sessionToken != null && sessionToken.isEmpty()) {
            sessionToken = null;
        }
        if (nid != null && nid.isEmpty()) {
            nid = null;
        }
        if (mustitUid != null && mustitUid.isEmpty()) {
            mustitUid = null;
        }

        return new CachedUserAgent(
                userAgentId,
                userAgentValue,
                sessionToken,
                nid,
                mustitUid,
                sessionExpiresAt,
                remainingTokens,
                maxTokens,
                windowStart,
                windowEnd,
                healthScore,
                status,
                suspendedAt,
                borrowedAt,
                cooldownUntil,
                consecutiveRateLimits);
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isEmpty() || "0".equals(value)) {
            return null;
        }
        return Instant.ofEpochMilli(Long.parseLong(value));
    }

    private UserAgentStatus parseStatus(String statusValue) {
        if (statusValue == null || statusValue.isEmpty()) {
            return UserAgentStatus.SESSION_REQUIRED;
        }

        try {
            return UserAgentStatus.valueOf(statusValue);
        } catch (IllegalArgumentException e) {
            // Phase 2: 레거시 status 값 매핑
            if ("READY".equals(statusValue)) {
                log.warn("레거시 status 값 발견: READY -> IDLE로 매핑");
                return UserAgentStatus.IDLE;
            }
            if ("AVAILABLE".equals(statusValue)) {
                log.warn("레거시 status 값 발견: AVAILABLE -> IDLE로 매핑");
                return UserAgentStatus.IDLE;
            }

            log.error("알 수 없는 status 값: {} -> SESSION_REQUIRED로 fallback", statusValue);
            return UserAgentStatus.SESSION_REQUIRED;
        }
    }
}
