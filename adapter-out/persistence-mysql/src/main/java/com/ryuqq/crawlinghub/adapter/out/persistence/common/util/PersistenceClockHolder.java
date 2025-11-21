package com.ryuqq.crawlinghub.adapter.out.persistence.common.util;

import java.time.Clock;
import org.springframework.stereotype.Component;

/**
 * Persistence Clock Holder
 *
 * <p>Persistence Layer에서 Clock을 제공하는 Singleton Bean
 *
 * <p>테스트 시 Clock을 교체하여 시간 제어 가능
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class PersistenceClockHolder {

    private final Clock clock;

    public PersistenceClockHolder(Clock clock) {
        this.clock = clock;
    }

    /**
     * Clock 반환
     *
     * @return Clock 인스턴스
     */
    public Clock getClock() {
        return clock;
    }
}
