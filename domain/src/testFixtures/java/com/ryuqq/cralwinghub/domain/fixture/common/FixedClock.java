package com.ryuqq.cralwinghub.domain.fixture.common;

import com.ryuqq.crawlinghub.domain.common.Clock;
import java.time.Instant;

/**
 * 테스트용 고정 시간 Clock
 *
 * <p>테스트에서 일관된 시간을 사용하기 위한 Clock 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
public final class FixedClock implements Clock {

    private static final Instant DEFAULT_INSTANT = Instant.parse("2025-11-27T00:00:00Z");

    private final Instant fixedInstant;

    private FixedClock(Instant fixedInstant) {
        this.fixedInstant = fixedInstant;
    }

    /**
     * 기본 고정 시간 Clock 생성 (2025-11-27T00:00:00Z)
     *
     * @return FixedClock
     */
    public static FixedClock aDefaultClock() {
        return new FixedClock(DEFAULT_INSTANT);
    }

    /**
     * 특정 시간으로 고정된 Clock 생성
     *
     * @param instant 고정할 시간
     * @return FixedClock
     */
    public static FixedClock at(Instant instant) {
        return new FixedClock(instant);
    }

    /**
     * ISO-8601 문자열로 고정된 Clock 생성
     *
     * @param isoDateTime ISO-8601 형식의 시간 문자열
     * @return FixedClock
     */
    public static FixedClock at(String isoDateTime) {
        return new FixedClock(Instant.parse(isoDateTime));
    }

    @Override
    public Instant now() {
        return fixedInstant;
    }

    public Instant getFixedInstant() {
        return fixedInstant;
    }
}
