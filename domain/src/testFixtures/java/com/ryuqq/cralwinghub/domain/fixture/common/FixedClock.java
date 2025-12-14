package com.ryuqq.cralwinghub.domain.fixture.common;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * 테스트용 고정 시간 Clock
 *
 * <p>테스트에서 일관된 시간을 사용하기 위한 java.time.Clock 확장
 *
 * @author development-team
 * @since 1.0.0
 */
public final class FixedClock extends Clock {

    private static final Instant DEFAULT_INSTANT = Instant.parse("2025-11-27T00:00:00Z");
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    private final Instant fixedInstant;
    private final ZoneId zone;

    private FixedClock(Instant fixedInstant, ZoneId zone) {
        this.fixedInstant = fixedInstant;
        this.zone = zone;
    }

    /**
     * 기본 고정 시간 Clock 생성 (2025-11-27T00:00:00Z)
     *
     * @return FixedClock
     */
    public static FixedClock aDefaultClock() {
        return new FixedClock(DEFAULT_INSTANT, DEFAULT_ZONE);
    }

    /**
     * 특정 시간으로 고정된 Clock 생성
     *
     * @param instant 고정할 시간
     * @return FixedClock
     */
    public static FixedClock at(Instant instant) {
        return new FixedClock(instant, DEFAULT_ZONE);
    }

    /**
     * ISO-8601 문자열로 고정된 Clock 생성
     *
     * @param isoDateTime ISO-8601 형식의 시간 문자열
     * @return FixedClock
     */
    public static FixedClock at(String isoDateTime) {
        return new FixedClock(Instant.parse(isoDateTime), DEFAULT_ZONE);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new FixedClock(fixedInstant, zone);
    }

    @Override
    public Instant instant() {
        return fixedInstant;
    }

    public Instant getFixedInstant() {
        return fixedInstant;
    }
}
