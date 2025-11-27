package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.LocalDateTime;

/**
 * UserAgent Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class UserAgentFixture {

    private static final LocalDateTime DEFAULT_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    /**
     * 신규 UserAgent 생성 (ID 미할당, AVAILABLE, Health Score 100)
     *
     * @return UserAgent
     */
    public static UserAgent aNewUserAgent() {
        return UserAgent.create(TokenFixture.aDefaultToken());
    }

    /**
     * ID가 할당된 AVAILABLE 상태 UserAgent 생성
     *
     * @return UserAgent (ID = 1L, AVAILABLE, Health Score 100)
     */
    public static UserAgent anAvailableUserAgent() {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStatus.AVAILABLE,
                HealthScoreFixture.initial(),
                DEFAULT_TIME,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * SUSPENDED 상태 UserAgent 생성
     *
     * @return UserAgent (ID = 1L, SUSPENDED, Health Score 29)
     */
    public static UserAgent aSuspendedUserAgent() {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStatus.SUSPENDED,
                HealthScoreFixture.belowThreshold(),
                DEFAULT_TIME,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 오래 전에 SUSPENDED된 UserAgent 생성 (복구 대상)
     *
     * @return UserAgent (SUSPENDED, lastUsedAt = 2시간 전)
     */
    public static UserAgent aRecoverableSuspendedUserAgent() {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStatus.SUSPENDED,
                HealthScoreFixture.belowThreshold(),
                DEFAULT_TIME.minusHours(2),
                0,
                DEFAULT_TIME.minusHours(3),
                DEFAULT_TIME.minusHours(2));
    }

    /**
     * BLOCKED 상태 UserAgent 생성
     *
     * @return UserAgent (ID = 1L, BLOCKED)
     */
    public static UserAgent aBlockedUserAgent() {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStatus.BLOCKED,
                HealthScoreFixture.minimum(),
                DEFAULT_TIME,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 정지 임계값 근처의 AVAILABLE UserAgent 생성
     *
     * @return UserAgent (AVAILABLE, Health Score 35)
     */
    public static UserAgent anAlmostSuspendedUserAgent() {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(),
                TokenFixture.aDefaultToken(),
                UserAgentStatus.AVAILABLE,
                HealthScoreFixture.of(35),
                DEFAULT_TIME,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
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
                UserAgentStatus.AVAILABLE,
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
                UserAgentStatus.AVAILABLE,
                HealthScoreFixture.initial(),
                DEFAULT_TIME,
                0,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    private UserAgentFixture() {
        // Utility class
    }
}
