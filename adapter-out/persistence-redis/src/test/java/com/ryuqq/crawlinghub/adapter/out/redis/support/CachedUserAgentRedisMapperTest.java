package com.ryuqq.crawlinghub.adapter.out.redis.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CachedUserAgentRedisMapper 단위 테스트
 *
 * <p>Redis Hash 데이터를 CachedUserAgent 도메인 객체로 변환하는 매핑 로직을 검증합니다.
 *
 * <p>테스트 범위:
 *
 * <ul>
 *   <li>기본 필드 매핑 (userAgentId, userAgentValue, sessionToken 등)
 *   <li>Instant 파싱 (epochMilli, null, 0, 빈 문자열)
 *   <li>UserAgentStatus 파싱 (정상값, 레거시 READY/AVAILABLE, 알 수 없는 값)
 *   <li>빈 sessionToken/nid/mustitUid의 null 변환
 *   <li>기본값 처리 (remainingTokens, maxTokens, healthScore)
 *   <li>Phase 2 신규 필드 (borrowedAt, cooldownUntil, consecutiveRateLimits)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("mapper")
@DisplayName("CachedUserAgentRedisMapper 단위 테스트")
class CachedUserAgentRedisMapperTest {

    private CachedUserAgentRedisMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CachedUserAgentRedisMapper();
    }

    /** 기본 Redis Hash 데이터 빌더 */
    private Map<String, String> buildFullHashData() {
        Map<String, String> data = new HashMap<>();
        data.put("userAgentId", "1");
        data.put("userAgentValue", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        data.put("sessionToken", "session-abc-123");
        data.put("nid", "nid-value");
        data.put("mustitUid", "uid-value");
        data.put("sessionExpiresAt", "1705312800000"); // 고정 Instant 밀리초
        data.put("remainingTokens", "60");
        data.put("maxTokens", "80");
        data.put("healthScore", "85");
        data.put("status", "IDLE");
        data.put("windowStart", "1705309200000");
        data.put("windowEnd", "1705312800000");
        data.put("suspendedAt", "0");
        data.put("borrowedAt", "1705309500000");
        data.put("cooldownUntil", "1705310400000");
        data.put("consecutiveRateLimits", "2");
        return data;
    }

    // ========================================
    // 기본 필드 매핑 테스트
    // ========================================

    @Nested
    @DisplayName("기본 필드 매핑")
    class BasicFieldMappingTests {

        @Test
        @DisplayName("성공 - userAgentId 올바르게 파싱")
        void shouldParseUserAgentId() {
            // Given
            Map<String, String> data = buildFullHashData();

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.userAgentId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공 - userAgentValue 올바르게 파싱")
        void shouldParseUserAgentValue() {
            // Given
            Map<String, String> data = buildFullHashData();

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.userAgentValue())
                    .isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        }

        @Test
        @DisplayName("성공 - sessionToken 올바르게 파싱")
        void shouldParseSessionToken() {
            // Given
            Map<String, String> data = buildFullHashData();

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.sessionToken()).isEqualTo("session-abc-123");
        }

        @Test
        @DisplayName("성공 - nid, mustitUid 올바르게 파싱")
        void shouldParseNidAndMusitUid() {
            // Given
            Map<String, String> data = buildFullHashData();

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.nid()).isEqualTo("nid-value");
            assertThat(result.mustitUid()).isEqualTo("uid-value");
        }

        @Test
        @DisplayName("성공 - remainingTokens, maxTokens, healthScore 올바르게 파싱")
        void shouldParseNumericFields() {
            // Given
            Map<String, String> data = buildFullHashData();

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.remainingTokens()).isEqualTo(60);
            assertThat(result.maxTokens()).isEqualTo(80);
            assertThat(result.healthScore()).isEqualTo(85);
        }

        @Test
        @DisplayName("성공 - consecutiveRateLimits 올바르게 파싱")
        void shouldParseConsecutiveRateLimits() {
            // Given
            Map<String, String> data = buildFullHashData();

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.consecutiveRateLimits()).isEqualTo(2);
        }
    }

    // ========================================
    // Instant 파싱 테스트
    // ========================================

    @Nested
    @DisplayName("Instant 필드 파싱")
    class InstantParsingTests {

        @Test
        @DisplayName("성공 - epochMilli 값을 Instant으로 변환")
        void shouldParseEpochMilliToInstant() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("sessionExpiresAt", "1705312800000");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.sessionExpiresAt()).isEqualTo(Instant.ofEpochMilli(1705312800000L));
        }

        @Test
        @DisplayName("성공 - sessionExpiresAt이 '0'이면 null 반환")
        void shouldReturnNullForZeroSessionExpiresAt() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("sessionExpiresAt", "0");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.sessionExpiresAt()).isNull();
        }

        @Test
        @DisplayName("성공 - sessionExpiresAt이 null이면 null 반환")
        void shouldReturnNullForNullSessionExpiresAt() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("sessionExpiresAt", null);

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.sessionExpiresAt()).isNull();
        }

        @Test
        @DisplayName("성공 - sessionExpiresAt이 빈 문자열이면 null 반환")
        void shouldReturnNullForEmptySessionExpiresAt() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("sessionExpiresAt", "");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.sessionExpiresAt()).isNull();
        }

        @Test
        @DisplayName("성공 - suspendedAt이 '0'이면 null 반환")
        void shouldReturnNullForZeroSuspendedAt() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("suspendedAt", "0");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.suspendedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - suspendedAt이 유효한 값이면 Instant로 변환")
        void shouldParseValidSuspendedAt() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("suspendedAt", "1705309200000");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.suspendedAt()).isEqualTo(Instant.ofEpochMilli(1705309200000L));
        }

        @Test
        @DisplayName("성공 - borrowedAt이 '0'이면 null 반환")
        void shouldReturnNullForZeroBorrowedAt() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("borrowedAt", "0");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.borrowedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - borrowedAt이 유효한 값이면 Instant로 변환")
        void shouldParseValidBorrowedAt() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("borrowedAt", "1705309500000");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.borrowedAt()).isEqualTo(Instant.ofEpochMilli(1705309500000L));
        }

        @Test
        @DisplayName("성공 - cooldownUntil이 '0'이면 null 반환")
        void shouldReturnNullForZeroCooldownUntil() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("cooldownUntil", "0");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.cooldownUntil()).isNull();
        }

        @Test
        @DisplayName("성공 - cooldownUntil이 유효한 값이면 Instant로 변환")
        void shouldParseValidCooldownUntil() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("cooldownUntil", "1705310400000");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.cooldownUntil()).isEqualTo(Instant.ofEpochMilli(1705310400000L));
        }

        @Test
        @DisplayName("성공 - windowStart, windowEnd도 올바르게 파싱")
        void shouldParseWindowFields() {
            // Given
            Map<String, String> data = buildFullHashData();

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.windowStart()).isEqualTo(Instant.ofEpochMilli(1705309200000L));
            assertThat(result.windowEnd()).isEqualTo(Instant.ofEpochMilli(1705312800000L));
        }
    }

    // ========================================
    // UserAgentStatus 파싱 테스트
    // ========================================

    @Nested
    @DisplayName("UserAgentStatus 파싱")
    class UserAgentStatusParsingTests {

        @Test
        @DisplayName("성공 - IDLE 상태 파싱")
        void shouldParseIdleStatus() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("status", "IDLE");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.IDLE);
        }

        @Test
        @DisplayName("성공 - SESSION_REQUIRED 상태 파싱")
        void shouldParseSessionRequiredStatus() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("status", "SESSION_REQUIRED");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.SESSION_REQUIRED);
        }

        @Test
        @DisplayName("성공 - SUSPENDED 상태 파싱")
        void shouldParseSuspendedStatus() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("status", "SUSPENDED");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.SUSPENDED);
        }

        @Test
        @DisplayName("성공 - BORROWED 상태 파싱")
        void shouldParseBorrowedStatus() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("status", "BORROWED");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.BORROWED);
        }

        @Test
        @DisplayName("성공 - COOLDOWN 상태 파싱")
        void shouldParseCooldownStatus() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("status", "COOLDOWN");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.COOLDOWN);
        }

        @Test
        @DisplayName("레거시 - READY 값은 IDLE로 매핑")
        void shouldMapLegacyReadyToIdle() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("status", "READY");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.IDLE);
        }

        @Test
        @DisplayName("레거시 - AVAILABLE 값은 IDLE로 매핑")
        void shouldMapLegacyAvailableToIdle() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("status", "AVAILABLE");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.IDLE);
        }

        @Test
        @DisplayName("알 수 없는 status 값은 SESSION_REQUIRED로 fallback")
        void shouldFallbackToSessionRequiredForUnknownStatus() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("status", "UNKNOWN_STATUS_XYZ");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.SESSION_REQUIRED);
        }

        @Test
        @DisplayName("status 필드 누락 시 SESSION_REQUIRED 기본값 사용")
        void shouldUseDefaultStatusWhenMissing() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.remove("status");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.SESSION_REQUIRED);
        }
    }

    // ========================================
    // 빈 문자열 -> null 변환 테스트
    // ========================================

    @Nested
    @DisplayName("빈 문자열의 null 변환")
    class EmptyStringToNullConversionTests {

        @Test
        @DisplayName("성공 - 빈 sessionToken은 null로 변환")
        void shouldConvertEmptySessionTokenToNull() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("sessionToken", "");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.sessionToken()).isNull();
        }

        @Test
        @DisplayName("성공 - 빈 nid는 null로 변환")
        void shouldConvertEmptyNidToNull() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("nid", "");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.nid()).isNull();
        }

        @Test
        @DisplayName("성공 - 빈 mustitUid는 null로 변환")
        void shouldConvertEmptyMusitUidToNull() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("mustitUid", "");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.mustitUid()).isNull();
        }

        @Test
        @DisplayName("성공 - sessionToken이 null이면 null 유지")
        void shouldKeepNullSessionToken() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("sessionToken", null);

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.sessionToken()).isNull();
        }
    }

    // ========================================
    // 기본값 처리 테스트
    // ========================================

    @Nested
    @DisplayName("기본값 처리")
    class DefaultValueTests {

        @Test
        @DisplayName("성공 - remainingTokens 누락 시 기본값 80 사용")
        void shouldUseDefaultRemainingTokens() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.remove("remainingTokens");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.remainingTokens()).isEqualTo(80);
        }

        @Test
        @DisplayName("성공 - maxTokens 누락 시 기본값 80 사용")
        void shouldUseDefaultMaxTokens() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.remove("maxTokens");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.maxTokens()).isEqualTo(80);
        }

        @Test
        @DisplayName("성공 - healthScore 누락 시 기본값 100 사용")
        void shouldUseDefaultHealthScore() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.remove("healthScore");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.healthScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("성공 - consecutiveRateLimits 누락 시 기본값 0 사용")
        void shouldUseDefaultConsecutiveRateLimits() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.remove("consecutiveRateLimits");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.consecutiveRateLimits()).isEqualTo(0);
        }
    }

    // ========================================
    // 전체 매핑 시나리오 테스트
    // ========================================

    @Nested
    @DisplayName("전체 매핑 시나리오")
    class FullMappingScenarioTests {

        @Test
        @DisplayName("성공 - IDLE 상태의 완전한 UserAgent 매핑")
        void shouldMapFullIdleUserAgent() {
            // Given
            Map<String, String> data = buildFullHashData();

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.userAgentId()).isEqualTo(1L);
            assertThat(result.userAgentValue())
                    .isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            assertThat(result.sessionToken()).isEqualTo("session-abc-123");
            assertThat(result.nid()).isEqualTo("nid-value");
            assertThat(result.mustitUid()).isEqualTo("uid-value");
            assertThat(result.remainingTokens()).isEqualTo(60);
            assertThat(result.maxTokens()).isEqualTo(80);
            assertThat(result.healthScore()).isEqualTo(85);
            assertThat(result.status()).isEqualTo(UserAgentStatus.IDLE);
            assertThat(result.consecutiveRateLimits()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 새로 추가된 (세션 없는) UserAgent 매핑")
        void shouldMapNewUserAgentWithNoSession() {
            // Given
            Map<String, String> data = new HashMap<>();
            data.put("userAgentId", "10");
            data.put("userAgentValue", "Chrome/120");
            data.put("sessionToken", "");
            data.put("nid", "");
            data.put("mustitUid", "");
            data.put("sessionExpiresAt", "0");
            data.put("remainingTokens", "80");
            data.put("maxTokens", "80");
            data.put("healthScore", "100");
            data.put("status", "SESSION_REQUIRED");
            data.put("windowStart", "0");
            data.put("windowEnd", "0");
            data.put("suspendedAt", "0");
            data.put("borrowedAt", "0");
            data.put("cooldownUntil", "0");
            data.put("consecutiveRateLimits", "0");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.userAgentId()).isEqualTo(10L);
            assertThat(result.sessionToken()).isNull();
            assertThat(result.nid()).isNull();
            assertThat(result.mustitUid()).isNull();
            assertThat(result.sessionExpiresAt()).isNull();
            assertThat(result.borrowedAt()).isNull();
            assertThat(result.cooldownUntil()).isNull();
            assertThat(result.suspendedAt()).isNull();
            assertThat(result.status()).isEqualTo(UserAgentStatus.SESSION_REQUIRED);
        }

        @Test
        @DisplayName("성공 - SUSPENDED 상태의 UserAgent 매핑 (suspendedAt 포함)")
        void shouldMapSuspendedUserAgent() {
            // Given
            Map<String, String> data = buildFullHashData();
            data.put("status", "SUSPENDED");
            data.put("sessionToken", "");
            data.put("nid", "");
            data.put("mustitUid", "");
            data.put("sessionExpiresAt", "0");
            data.put("suspendedAt", "1705309200000");

            // When
            CachedUserAgent result = mapper.mapToCachedUserAgent(data);

            // Then
            assertThat(result.status()).isEqualTo(UserAgentStatus.SUSPENDED);
            assertThat(result.suspendedAt()).isEqualTo(Instant.ofEpochMilli(1705309200000L));
            assertThat(result.sessionToken()).isNull();
        }
    }
}
