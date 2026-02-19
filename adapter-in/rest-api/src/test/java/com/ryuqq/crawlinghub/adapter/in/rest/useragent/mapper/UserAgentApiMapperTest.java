package com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command.RegisterUserAgentApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command.UpdateUserAgentMetadataApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command.UpdateUserAgentStatusApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.query.SearchUserAgentsApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.RecoverUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.RegisterUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UpdateUserAgentMetadataApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UpdateUserAgentStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.WarmUpUserAgentApiResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RegisterUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentMetadataCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentStatusCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentSearchCriteria;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentDetailResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse.HealthScoreStats;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentSummaryResponse;
import com.ryuqq.crawlinghub.domain.useragent.vo.BrowserType;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceBrand;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.OsType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.List;
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
                            UserAgentStatus.READY,
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
            assertThat(result.status()).isEqualTo(UserAgentStatus.READY);
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
                            UserAgentStatus.READY,
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

    @Nested
    @DisplayName("toDetailApiResponse() 테스트")
    class ToDetailApiResponseTests {

        @Test
        @DisplayName("Pool 정보를 포함한 상세 응답을 API 응답으로 변환한다")
        void toDetailApiResponse_WithPoolInfo_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            Instant sessionExpiresAt = now.plusSeconds(3600);
            UserAgentDetailResponse.PoolInfo poolInfo =
                    UserAgentDetailResponse.PoolInfo.of(45, true, sessionExpiresAt);
            UserAgentDetailResponse appResponse =
                    UserAgentDetailResponse.withPoolInfo(
                            1L,
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                            DeviceType.of(DeviceType.Type.DESKTOP),
                            UserAgentStatus.READY,
                            95,
                            150,
                            now.minusSeconds(3600),
                            now,
                            now,
                            poolInfo);

            // when
            UserAgentDetailApiResponse result = mapper.toDetailApiResponse(appResponse);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.userAgentValue())
                    .isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            assertThat(result.deviceType()).isEqualTo(DeviceType.of(DeviceType.Type.DESKTOP));
            assertThat(result.status()).isEqualTo(UserAgentStatus.READY);
            assertThat(result.healthScore()).isEqualTo(95);
            assertThat(result.requestsPerDay()).isEqualTo(150);
            assertThat(result.poolInfo().isInPool()).isTrue();
            assertThat(result.poolInfo().remainingTokens()).isEqualTo(45);
            assertThat(result.poolInfo().hasValidSession()).isTrue();
            assertThat(result.poolInfo().sessionExpiresAt()).isEqualTo(sessionExpiresAt);
        }

        @Test
        @DisplayName("Pool에 없는 UserAgent 상세 응답을 API 응답으로 변환한다")
        void toDetailApiResponse_NotInPool_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            UserAgentDetailResponse appResponse =
                    UserAgentDetailResponse.of(
                            2L,
                            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)",
                            DeviceType.of(DeviceType.Type.MOBILE),
                            UserAgentStatus.SUSPENDED,
                            50,
                            300,
                            null,
                            now,
                            now);

            // when
            UserAgentDetailApiResponse result = mapper.toDetailApiResponse(appResponse);

            // then
            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.status()).isEqualTo(UserAgentStatus.SUSPENDED);
            assertThat(result.poolInfo().isInPool()).isFalse();
            assertThat(result.poolInfo().remainingTokens()).isZero();
            assertThat(result.poolInfo().hasValidSession()).isFalse();
            assertThat(result.poolInfo().sessionExpiresAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toCommand(UpdateUserAgentStatusApiRequest) 테스트")
    class ToUpdateStatusCommandTests {

        @Test
        @DisplayName("상태 변경 요청을 Command로 변환한다")
        void toCommand_UpdateStatus_ShouldConvertCorrectly() {
            // given
            List<Long> ids = List.of(1L, 2L, 3L);
            UpdateUserAgentStatusApiRequest request =
                    new UpdateUserAgentStatusApiRequest(ids, UserAgentStatus.SUSPENDED);

            // when
            UpdateUserAgentStatusCommand result = mapper.toCommand(request);

            // then
            assertThat(result.userAgentIds()).containsExactly(1L, 2L, 3L);
            assertThat(result.status()).isEqualTo(UserAgentStatus.SUSPENDED);
        }

        @Test
        @DisplayName("단일 ID로 상태 변경 요청을 Command로 변환한다")
        void toCommand_UpdateStatus_WithSingleId_ShouldConvertCorrectly() {
            // given
            UpdateUserAgentStatusApiRequest request =
                    new UpdateUserAgentStatusApiRequest(List.of(10L), UserAgentStatus.BLOCKED);

            // when
            UpdateUserAgentStatusCommand result = mapper.toCommand(request);

            // then
            assertThat(result.userAgentIds()).containsExactly(10L);
            assertThat(result.status()).isEqualTo(UserAgentStatus.BLOCKED);
            assertThat(result.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("toCommand(RegisterUserAgentApiRequest) 테스트")
    class ToRegisterCommandTests {

        @Test
        @DisplayName("등록 요청을 Command로 변환하며 User-Agent 문자열에서 메타데이터를 자동 파싱한다")
        void toCommand_Register_ShouldConvertWithAutoParsing() {
            // given
            String userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                            + " AppleWebKit/537.36 (KHTML, like Gecko)"
                            + " Chrome/120.0.0.0 Safari/537.36";
            RegisterUserAgentApiRequest request =
                    new RegisterUserAgentApiRequest(
                            userAgentString, DeviceType.of(DeviceType.Type.DESKTOP));

            // when
            RegisterUserAgentCommand result = mapper.toCommand(request);

            // then
            assertThat(result.userAgentString()).isEqualTo(userAgentString);
            assertThat(result.deviceType()).isEqualTo(DeviceType.of(DeviceType.Type.DESKTOP));
            assertThat(result.deviceBrand()).isEqualTo(DeviceBrand.GENERIC);
            assertThat(result.osType()).isEqualTo(OsType.WINDOWS);
            assertThat(result.browserType()).isEqualTo(BrowserType.CHROME);
        }

        @Test
        @DisplayName("iOS User-Agent 등록 요청을 Command로 변환한다")
        void toCommand_Register_WithiOsUserAgent_ShouldParseCorrectly() {
            // given
            String userAgentString =
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)"
                            + " AppleWebKit/605.1.15 (KHTML, like Gecko)"
                            + " Version/14.0 Mobile/15E148 Safari/604.1";
            RegisterUserAgentApiRequest request =
                    new RegisterUserAgentApiRequest(
                            userAgentString, DeviceType.of(DeviceType.Type.MOBILE));

            // when
            RegisterUserAgentCommand result = mapper.toCommand(request);

            // then
            assertThat(result.userAgentString()).isEqualTo(userAgentString);
            assertThat(result.deviceType()).isEqualTo(DeviceType.of(DeviceType.Type.MOBILE));
            assertThat(result.deviceBrand()).isEqualTo(DeviceBrand.IPHONE);
            assertThat(result.osType()).isEqualTo(OsType.IOS);
            assertThat(result.browserType()).isEqualTo(BrowserType.SAFARI);
        }
    }

    @Nested
    @DisplayName("toCommand(long, UpdateUserAgentMetadataApiRequest) 테스트")
    class ToUpdateMetadataCommandTests {

        @Test
        @DisplayName("메타데이터 수정 요청을 Command로 변환한다")
        void toCommand_UpdateMetadata_ShouldConvertCorrectly() {
            // given
            long userAgentId = 42L;
            UpdateUserAgentMetadataApiRequest request =
                    new UpdateUserAgentMetadataApiRequest(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                            DeviceType.of(DeviceType.Type.DESKTOP),
                            DeviceBrand.GENERIC,
                            OsType.WINDOWS,
                            "10.0",
                            BrowserType.CHROME,
                            "120.0.0.0");

            // when
            UpdateUserAgentMetadataCommand result = mapper.toCommand(userAgentId, request);

            // then
            assertThat(result.userAgentId()).isEqualTo(42L);
            assertThat(result.userAgentString())
                    .isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            assertThat(result.deviceType()).isEqualTo(DeviceType.of(DeviceType.Type.DESKTOP));
            assertThat(result.deviceBrand()).isEqualTo(DeviceBrand.GENERIC);
            assertThat(result.osType()).isEqualTo(OsType.WINDOWS);
            assertThat(result.osVersion()).isEqualTo("10.0");
            assertThat(result.browserType()).isEqualTo(BrowserType.CHROME);
            assertThat(result.browserVersion()).isEqualTo("120.0.0.0");
        }

        @Test
        @DisplayName("null 필드가 포함된 메타데이터 수정 요청을 Command로 변환한다")
        void toCommand_UpdateMetadata_WithNullFields_ShouldConvertCorrectly() {
            // given
            long userAgentId = 7L;
            UpdateUserAgentMetadataApiRequest request =
                    new UpdateUserAgentMetadataApiRequest(null, null, null, null, null, null, null);

            // when
            UpdateUserAgentMetadataCommand result = mapper.toCommand(userAgentId, request);

            // then
            assertThat(result.userAgentId()).isEqualTo(7L);
            assertThat(result.userAgentString()).isNull();
            assertThat(result.deviceType()).isNull();
            assertThat(result.deviceBrand()).isNull();
            assertThat(result.osType()).isNull();
            assertThat(result.osVersion()).isNull();
            assertThat(result.browserType()).isNull();
            assertThat(result.browserVersion()).isNull();
        }
    }

    @Nested
    @DisplayName("toStatusUpdateApiResponse() 테스트")
    class ToStatusUpdateApiResponseTests {

        @Test
        @DisplayName("상태 변경 결과를 API 응답으로 변환한다")
        void toStatusUpdateApiResponse_ShouldConvertCorrectly() {
            // given
            int updatedCount = 5;
            String status = "SUSPENDED";

            // when
            UpdateUserAgentStatusApiResponse result =
                    mapper.toStatusUpdateApiResponse(updatedCount, status);

            // then
            assertThat(result.updatedCount()).isEqualTo(5);
            assertThat(result.message()).contains("5");
            assertThat(result.message()).contains("SUSPENDED");
        }

        @Test
        @DisplayName("변경된 UserAgent가 없는 경우 결과를 API 응답으로 변환한다")
        void toStatusUpdateApiResponse_WithZeroUpdated_ShouldConvertCorrectly() {
            // given
            int updatedCount = 0;
            String status = "BLOCKED";

            // when
            UpdateUserAgentStatusApiResponse result =
                    mapper.toStatusUpdateApiResponse(updatedCount, status);

            // then
            assertThat(result.updatedCount()).isZero();
            assertThat(result.message()).contains("0");
            assertThat(result.message()).contains("BLOCKED");
        }
    }

    @Nested
    @DisplayName("toRegisterApiResponse() 테스트")
    class ToRegisterApiResponseTests {

        @Test
        @DisplayName("생성된 UserAgent ID를 등록 응답으로 변환한다")
        void toRegisterApiResponse_ShouldConvertCorrectly() {
            // given
            long userAgentId = 99L;

            // when
            RegisterUserAgentApiResponse result = mapper.toRegisterApiResponse(userAgentId);

            // then
            assertThat(result.userAgentId()).isEqualTo(99L);
            assertThat(result.message()).isEqualTo("UserAgent registered successfully");
        }

        @Test
        @DisplayName("첫 번째 UserAgent 등록 시 ID 1을 포함한 응답을 반환한다")
        void toRegisterApiResponse_WithFirstId_ShouldConvertCorrectly() {
            // given
            long userAgentId = 1L;

            // when
            RegisterUserAgentApiResponse result = mapper.toRegisterApiResponse(userAgentId);

            // then
            assertThat(result.userAgentId()).isEqualTo(1L);
            assertThat(result.message()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("toMetadataUpdateApiResponse() 테스트")
    class ToMetadataUpdateApiResponseTests {

        @Test
        @DisplayName("수정된 UserAgent ID를 메타데이터 수정 응답으로 변환한다")
        void toMetadataUpdateApiResponse_ShouldConvertCorrectly() {
            // given
            long userAgentId = 42L;

            // when
            UpdateUserAgentMetadataApiResponse result =
                    mapper.toMetadataUpdateApiResponse(userAgentId);

            // then
            assertThat(result.userAgentId()).isEqualTo(42L);
            assertThat(result.message()).isEqualTo("UserAgent metadata updated successfully");
        }

        @Test
        @DisplayName("다양한 UserAgent ID에 대해 응답을 올바르게 변환한다")
        void toMetadataUpdateApiResponse_WithVariousIds_ShouldConvertCorrectly() {
            // given
            long userAgentId = 1000L;

            // when
            UpdateUserAgentMetadataApiResponse result =
                    mapper.toMetadataUpdateApiResponse(userAgentId);

            // then
            assertThat(result.userAgentId()).isEqualTo(1000L);
            assertThat(result.message()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("toCriteria() 테스트")
    class ToCriteriaTests {

        @Test
        @DisplayName("상태 필터와 날짜 범위가 있는 검색 요청을 SearchCriteria로 변환한다")
        void toCriteria_WithStatusAndDateRange_ShouldConvertCorrectly() {
            // given
            Instant from = Instant.parse("2024-01-01T00:00:00Z");
            Instant to = Instant.parse("2024-12-31T23:59:59Z");
            SearchUserAgentsApiRequest request =
                    new SearchUserAgentsApiRequest(List.of("READY", "SUSPENDED"), from, to, 0, 20);

            // when
            UserAgentSearchCriteria result = mapper.toCriteria(request);

            // then
            assertThat(result.statuses())
                    .containsExactly(UserAgentStatus.READY, UserAgentStatus.SUSPENDED);
            assertThat(result.createdFrom()).isEqualTo(from);
            assertThat(result.createdTo()).isEqualTo(to);
            assertThat(result.pageRequest().page()).isZero();
            assertThat(result.pageRequest().size()).isEqualTo(20);
        }

        @Test
        @DisplayName("상태 필터가 null인 경우 상태 조건 없이 SearchCriteria로 변환한다")
        void toCriteria_WithNullStatuses_ShouldReturnNullStatuses() {
            // given
            SearchUserAgentsApiRequest request =
                    new SearchUserAgentsApiRequest(null, null, null, 0, 10);

            // when
            UserAgentSearchCriteria result = mapper.toCriteria(request);

            // then
            assertThat(result.statuses()).isNull();
            assertThat(result.createdFrom()).isNull();
            assertThat(result.createdTo()).isNull();
            assertThat(result.hasStatusFilter()).isFalse();
        }

        @Test
        @DisplayName("빈 상태 목록인 경우 상태 조건 없이 SearchCriteria로 변환한다")
        void toCriteria_WithEmptyStatuses_ShouldReturnNullStatuses() {
            // given
            SearchUserAgentsApiRequest request =
                    new SearchUserAgentsApiRequest(List.of(), null, null, 0, 20);

            // when
            UserAgentSearchCriteria result = mapper.toCriteria(request);

            // then
            assertThat(result.statuses()).isNull();
            assertThat(result.hasStatusFilter()).isFalse();
        }

        @Test
        @DisplayName("페이지 기본값이 적용된 검색 요청을 SearchCriteria로 변환한다")
        void toCriteria_WithDefaultPagination_ShouldConvertCorrectly() {
            // given
            SearchUserAgentsApiRequest request =
                    new SearchUserAgentsApiRequest(null, null, null, null, null);

            // when
            UserAgentSearchCriteria result = mapper.toCriteria(request);

            // then
            assertThat(result.pageRequest().page()).isZero();
            assertThat(result.pageRequest().size()).isEqualTo(20);
        }

        @Test
        @DisplayName("단일 상태 필터로 SearchCriteria를 변환한다")
        void toCriteria_WithSingleStatus_ShouldConvertCorrectly() {
            // given
            SearchUserAgentsApiRequest request =
                    new SearchUserAgentsApiRequest(List.of("BLOCKED"), null, null, 2, 50);

            // when
            UserAgentSearchCriteria result = mapper.toCriteria(request);

            // then
            assertThat(result.statuses()).containsExactly(UserAgentStatus.BLOCKED);
            assertThat(result.hasStatusFilter()).isTrue();
            assertThat(result.pageRequest().page()).isEqualTo(2);
            assertThat(result.pageRequest().size()).isEqualTo(50);
        }
    }
}
