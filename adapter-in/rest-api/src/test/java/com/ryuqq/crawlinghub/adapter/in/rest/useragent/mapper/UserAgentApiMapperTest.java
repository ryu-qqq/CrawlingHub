package com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.RecoverUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.WarmUpUserAgentApiResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse.HealthScoreStats;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentSummaryResponse;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * UserAgentApiMapper 단위 테스트
 *
 * <p>UserAgent API ↔ Application Layer 변환 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("UserAgentApiMapper 단위 테스트")
class UserAgentApiMapperTest {

    private UserAgentApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserAgentApiMapper();
    }

    @Nested
    @DisplayName("toApiResponse() - Pool 상태 테스트")
    class ToPoolStatusApiResponseTests {

        @Test
        @DisplayName("Pool 상태 응답을 API 응답으로 변환한다")
        void toApiResponse_ShouldConvertCorrectly() {
            // given
            HealthScoreStats healthStats = new HealthScoreStats(85.5, 60, 100);
            UserAgentPoolStatusResponse appResponse =
                    new UserAgentPoolStatusResponse(100, 80, 20, 80.0, healthStats);

            // when
            UserAgentPoolStatusApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.totalAgents()).isEqualTo(100);
            assertThat(result.availableAgents()).isEqualTo(80);
            assertThat(result.suspendedAgents()).isEqualTo(20);
            assertThat(result.availableRate()).isEqualTo(80.0);
            assertThat(result.healthScoreStats().avg()).isEqualTo(85.5);
            assertThat(result.healthScoreStats().min()).isEqualTo(60);
            assertThat(result.healthScoreStats().max()).isEqualTo(100);
            assertThat(result.isCircuitBreakerOpen()).isFalse();
            assertThat(result.isHealthy()).isTrue();
        }

        @Test
        @DisplayName("Circuit Breaker가 열린 상태를 변환한다")
        void toApiResponse_WhenCircuitBreakerOpen_ShouldReflectState() {
            // given
            HealthScoreStats healthStats = new HealthScoreStats(30.0, 10, 50);
            UserAgentPoolStatusResponse appResponse =
                    new UserAgentPoolStatusResponse(100, 15, 85, 15.0, healthStats);

            // when
            UserAgentPoolStatusApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.isCircuitBreakerOpen()).isTrue();
            assertThat(result.isHealthy()).isFalse();
        }

        @Test
        @DisplayName("건강하지 않은 Pool 상태를 변환한다")
        void toApiResponse_WhenUnhealthy_ShouldReflectState() {
            // given
            HealthScoreStats healthStats = new HealthScoreStats(60.0, 40, 80);
            UserAgentPoolStatusResponse appResponse =
                    new UserAgentPoolStatusResponse(100, 45, 55, 45.0, healthStats);

            // when
            UserAgentPoolStatusApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.isCircuitBreakerOpen()).isFalse();
            assertThat(result.isHealthy()).isFalse();
        }

        @Test
        @DisplayName("빈 Pool 상태를 변환한다")
        void toApiResponse_WithEmptyPool_ShouldHandleCorrectly() {
            // given
            HealthScoreStats healthStats = HealthScoreStats.empty();
            UserAgentPoolStatusResponse appResponse =
                    new UserAgentPoolStatusResponse(0, 0, 0, 0.0, healthStats);

            // when
            UserAgentPoolStatusApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.totalAgents()).isZero();
            assertThat(result.availableAgents()).isZero();
            assertThat(result.healthScoreStats().avg()).isZero();
        }
    }

    @Nested
    @DisplayName("toRecoverApiResponse() 테스트")
    class ToRecoverApiResponseTests {

        @Test
        @DisplayName("복구 결과를 API 응답으로 변환한다")
        void toRecoverApiResponse_ShouldConvertCorrectly() {
            // given
            int recoveredCount = 5;

            // when
            RecoverUserAgentApiResponse result = mapper.toRecoverApiResponse(recoveredCount);

            // then
            assertThat(result.recoveredCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("복구된 UserAgent가 없는 경우를 처리한다")
        void toRecoverApiResponse_WithZeroRecovered_ShouldHandleCorrectly() {
            // given
            int recoveredCount = 0;

            // when
            RecoverUserAgentApiResponse result = mapper.toRecoverApiResponse(recoveredCount);

            // then
            assertThat(result.recoveredCount()).isZero();
        }
    }

    @Nested
    @DisplayName("toSummaryApiResponse() 테스트")
    class ToSummaryApiResponseTests {

        @Test
        @DisplayName("UserAgent 요약 응답을 API 응답으로 변환한다")
        void toSummaryApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            Instant lastUsed = now.minusSeconds(3600);
            UserAgentSummaryResponse appResponse =
                    new UserAgentSummaryResponse(
                            1L,
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                            DeviceType.of(DeviceType.Type.DESKTOP),
                            UserAgentStatus.AVAILABLE,
                            95,
                            150,
                            lastUsed,
                            now,
                            now);

            // when
            UserAgentSummaryApiResponse result = mapper.toSummaryApiResponse(appResponse);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.userAgentValue())
                    .isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            assertThat(result.deviceType()).isEqualTo(DeviceType.of(DeviceType.Type.DESKTOP));
            assertThat(result.status()).isEqualTo(UserAgentStatus.AVAILABLE);
            assertThat(result.healthScore()).isEqualTo(95);
            assertThat(result.requestsPerDay()).isEqualTo(150);
            assertThat(result.lastUsedAt()).isNotNull();
            assertThat(result.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("MOBILE DeviceType을 올바르게 변환한다")
        void toSummaryApiResponse_WithMobileDevice_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            UserAgentSummaryResponse appResponse =
                    new UserAgentSummaryResponse(
                            2L,
                            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)",
                            DeviceType.of(DeviceType.Type.MOBILE),
                            UserAgentStatus.SUSPENDED,
                            50,
                            300,
                            now,
                            now,
                            now);

            // when
            UserAgentSummaryApiResponse result = mapper.toSummaryApiResponse(appResponse);

            // then
            assertThat(result.deviceType()).isEqualTo(DeviceType.of(DeviceType.Type.MOBILE));
            assertThat(result.status()).isEqualTo(UserAgentStatus.SUSPENDED);
        }

        @Test
        @DisplayName("null lastUsedAt을 처리한다")
        void toSummaryApiResponse_WithNullLastUsed_ShouldHandleCorrectly() {
            // given
            Instant now = Instant.now();
            UserAgentSummaryResponse appResponse =
                    new UserAgentSummaryResponse(
                            3L,
                            "Mozilla/5.0 (Linux; Android 10)",
                            DeviceType.of(DeviceType.Type.MOBILE),
                            UserAgentStatus.AVAILABLE,
                            100,
                            0,
                            null,
                            now,
                            null);

            // when
            UserAgentSummaryApiResponse result = mapper.toSummaryApiResponse(appResponse);

            // then
            assertThat(result.lastUsedAt()).isNull();
            assertThat(result.requestsPerDay()).isZero();
        }
    }

    @Nested
    @DisplayName("toWarmUpApiResponse() 테스트")
    class ToWarmUpApiResponseTests {

        @Test
        @DisplayName("Warm-up 결과를 API 응답으로 변환한다")
        void toWarmUpApiResponse_ShouldConvertCorrectly() {
            // given
            int addedCount = 10;

            // when
            WarmUpUserAgentApiResponse result = mapper.toWarmUpApiResponse(addedCount);

            // then
            assertThat(result.addedCount()).isEqualTo(10);
            assertThat(result.message()).contains("10");
            assertThat(result.message()).contains("user agents added to pool");
        }

        @Test
        @DisplayName("추가된 UserAgent가 없는 경우를 처리한다")
        void toWarmUpApiResponse_WithZeroAdded_ShouldHandleCorrectly() {
            // given
            int addedCount = 0;

            // when
            WarmUpUserAgentApiResponse result = mapper.toWarmUpApiResponse(addedCount);

            // then
            assertThat(result.addedCount()).isZero();
            assertThat(result.message()).contains("No user agents to warm up");
        }

        @Test
        @DisplayName("단일 UserAgent 추가 시 올바른 메시지를 생성한다")
        void toWarmUpApiResponse_WithSingleAgent_ShouldFormatMessageCorrectly() {
            // given
            int addedCount = 1;

            // when
            WarmUpUserAgentApiResponse result = mapper.toWarmUpApiResponse(addedCount);

            // then
            assertThat(result.addedCount()).isEqualTo(1);
            assertThat(result.message()).contains("1 user agents added to pool");
        }
    }
}
