package com.ryuqq.crawlinghub.config;

import com.ryuqq.crawlinghub.domain.common.Clock;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;

/**
 * SystemClockHolder - ClockHolder 구현체
 *
 * <p>System Clock을 사용하는 ClockHolder 구현체입니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>Bootstrap Layer에서 Bean으로 등록
 *   <li>Domain Layer의 ClockHolder 인터페이스 구현
 *   <li>java.time.Clock을 감싸서 Domain Clock 제공
 * </ul>
 *
 * @author AuthHub Team
 * @since 1.0.0
 */
public record SystemClockHolder(java.time.Clock systemClock) implements ClockHolder {

    @Override
    public Clock clock() {
        return systemClock::instant;
    }
}
