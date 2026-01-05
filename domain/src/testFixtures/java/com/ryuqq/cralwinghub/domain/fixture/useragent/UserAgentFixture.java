package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Clock;
import java.time.Instant;

/**
 * UserAgent Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class UserAgentFixture {

    private static final Clock DEFAULT_CLOCK = FixedClock.aDefaultClock();
    private static final Instant DEFAULT_TIME = DEFAULT_CLOCK.instant();

    /**
     * 신규 UserAgent 생성 (ID 미할당, AVAILABLE, Health Score 100)
     *
     * @return UserAgent
     */
    public static UserAgent forNew() {
        return UserAgent.forNew(
                TokenFixture.aDefaultToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DEFAULT_CLOCK);
    }

    /**
     * 신규 UserAgent 생성 (Clock 지정)
     *
     * @param clock 시간 제어
     * @return UserAgent
     */
    public static UserAgent forNew(Clock clock) {
        return UserAgent.forNew(
                TokenFixture.aDefaultToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                clock);
    }

    /**
     * 토큰 없이 신규 UserAgent 생성 (Lazy Token Issuance)
     *
     * @return UserAgent (토큰 없음, AVAILABLE, Health Score 100)
     */
    public static UserAgent forNewWithoutToken() {
        return UserAgent.forNewWithoutToken(
                UserAgentStringFixture.aDefaultUserAgentString(), DEFAULT_CLOCK);
    }

    /**
     * 토큰 없이 신규 UserAgent 생성 (Clock 지정)
     *
     * @param clock 시간 제어
     * @return UserAgent (토큰 없음, AVAILABLE, Health Score 100)
     */
    public static UserAgent forNewWithoutToken(Clock clock) {
        return UserAgent.forNewWithoutToken(
                UserAgentStringFixture.aDefaultUserAgentString(), clock);
    }

    /**
     * ID가 할당된 토큰 없는 UserAgent 생성
     *
     * @return UserAgent (ID = 1L, 토큰 없음, AVAILABLE, Health Score 100)
     */
    public static UserAgent aUserAgentWithoutToken() {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.anEmptyToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DeviceTypeFixture.aDefaultDeviceType(),
                UserAgentMetadataFixture.aDefaultMetadata(),
                UserAgentStatus.READY,
                HealthScoreFixture.initial(),
                DEFAULT_TIME,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 특정 ID를 가진 토큰 없는 UserAgent 생성
     *
     * @param id UserAgent ID
     * @return UserAgent (토큰 없음, AVAILABLE)
     */
    public static UserAgent aUserAgentWithoutToken(Long id) {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(id),
                TokenFixture.anEmptyToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DeviceTypeFixture.aDefaultDeviceType(),
                UserAgentMetadataFixture.aDefaultMetadata(),
                UserAgentStatus.READY,
                HealthScoreFixture.initial(),
                DEFAULT_TIME,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * ID가 할당된 AVAILABLE 상태 UserAgent 생성
     *
     * @return UserAgent (ID = 1L, AVAILABLE, Health Score 100)
     */
    public static UserAgent anAvailableUserAgent() {
        return reconstitute(UserAgentStatus.READY, HealthScoreFixture.initial());
    }

    /**
     * ID가 할당된 AVAILABLE 상태 UserAgent 생성 (Clock 지정)
     *
     * @param clock 시간 제어
     * @return UserAgent (ID = 1L, AVAILABLE, Health Score 100)
     */
    public static UserAgent anAvailableUserAgent(Clock clock) {
        Instant now = clock.instant();
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DeviceTypeFixture.aDefaultDeviceType(),
                UserAgentMetadataFixture.aDefaultMetadata(),
                UserAgentStatus.READY,
                HealthScoreFixture.initial(),
                now,
                0,
                now,
                now);
    }

    /**
     * SUSPENDED 상태 UserAgent 생성
     *
     * @return UserAgent (ID = 1L, SUSPENDED, Health Score 29)
     */
    public static UserAgent aSuspendedUserAgent() {
        return reconstitute(UserAgentStatus.SUSPENDED, HealthScoreFixture.belowThreshold());
    }

    /**
     * 오래 전에 SUSPENDED된 UserAgent 생성 (복구 대상)
     *
     * @return UserAgent (SUSPENDED, lastUsedAt = 2시간 전)
     */
    public static UserAgent aRecoverableSuspendedUserAgent() {
        Instant twoHoursAgo = DEFAULT_TIME.minusSeconds(7200);
        Instant threeHoursAgo = DEFAULT_TIME.minusSeconds(10800);
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DeviceTypeFixture.aDefaultDeviceType(),
                UserAgentMetadataFixture.aDefaultMetadata(),
                UserAgentStatus.SUSPENDED,
                HealthScoreFixture.belowThreshold(),
                twoHoursAgo,
                0,
                threeHoursAgo,
                twoHoursAgo);
    }

    /**
     * BLOCKED 상태 UserAgent 생성
     *
     * @return UserAgent (ID = 1L, BLOCKED)
     */
    public static UserAgent aBlockedUserAgent() {
        return reconstitute(UserAgentStatus.BLOCKED, HealthScoreFixture.minimum());
    }

    /**
     * 정지 임계값 근처의 AVAILABLE UserAgent 생성
     *
     * @return UserAgent (AVAILABLE, Health Score 35)
     */
    public static UserAgent anAlmostSuspendedUserAgent() {
        return reconstitute(UserAgentStatus.READY, HealthScoreFixture.of(35));
    }

    /**
     * 높은 요청 수를 가진 AVAILABLE UserAgent 생성
     *
     * @return UserAgent (AVAILABLE, requestsPerDay = 100)
     */
    public static UserAgent aHighUsageUserAgent() {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DeviceTypeFixture.aDefaultDeviceType(),
                UserAgentMetadataFixture.aDefaultMetadata(),
                UserAgentStatus.READY,
                HealthScoreFixture.initial(),
                DEFAULT_TIME,
                100,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 특정 ID를 가진 UserAgent 생성
     *
     * @param id UserAgent ID
     * @return UserAgent (AVAILABLE)
     */
    public static UserAgent anUserAgentWithId(Long id) {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(id),
                TokenFixture.aDefaultToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DeviceTypeFixture.aDefaultDeviceType(),
                UserAgentMetadataFixture.aDefaultMetadata(),
                UserAgentStatus.READY,
                HealthScoreFixture.initial(),
                DEFAULT_TIME,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 특정 Health Score를 가진 AVAILABLE UserAgent 생성 (Clock 지정)
     *
     * @param healthScoreValue 건강 점수
     * @param clock 시간 제어
     * @return UserAgent
     */
    public static UserAgent of(int healthScoreValue, Clock clock) {
        Instant now = clock.instant();
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DeviceTypeFixture.aDefaultDeviceType(),
                UserAgentMetadataFixture.aDefaultMetadata(),
                UserAgentStatus.READY,
                HealthScoreFixture.of(healthScoreValue),
                now,
                0,
                now,
                now);
    }

    /**
     * 영속성 복원용 Fixture
     *
     * @param status 상태
     * @param healthScore 건강 점수
     * @return UserAgent
     */
    public static UserAgent reconstitute(
            UserAgentStatus status,
            com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore healthScore) {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DeviceTypeFixture.aDefaultDeviceType(),
                UserAgentMetadataFixture.aDefaultMetadata(),
                status,
                healthScore,
                DEFAULT_TIME,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    private UserAgentFixture() {
        // Utility class
    }
}
