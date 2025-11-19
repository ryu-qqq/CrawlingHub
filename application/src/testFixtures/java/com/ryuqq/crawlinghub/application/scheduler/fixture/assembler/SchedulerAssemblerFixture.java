package com.ryuqq.crawlinghub.application.scheduler.fixture.assembler;

import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public final class SchedulerAssemblerFixture {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-11-18T00:00:00Z"), ZoneOffset.UTC);

    private SchedulerAssemblerFixture() {
    }

    public static SchedulerAssembler create() {
        return new SchedulerAssembler(FIXED_CLOCK);
    }

    public static Clock fixedClock() {
        return FIXED_CLOCK;
    }
}

