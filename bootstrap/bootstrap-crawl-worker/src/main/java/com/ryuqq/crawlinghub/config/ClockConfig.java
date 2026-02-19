package com.ryuqq.crawlinghub.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clock Bean 설정
 *
 * <p>Bootstrap Layer에서 Infrastructure 관심사인 Clock 관련 Bean을 등록합니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>✅ Infrastructure 관심사는 Bootstrap에서 관리
 *   <li>✅ Clock은 SystemDefaultZone 사용 (Asia/Seoul 또는 UTC)
 *   <li>✅ Application Layer의 TimeProvider가 Clock을 주입받아 사용
 *   <li>✅ 테스트 환경에서는 FixedClock으로 교체 가능
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-21
 */
@Configuration("crawlWorkerClockConfig")
public class ClockConfig {

    /**
     * System Clock Bean 등록
     *
     * <p>System Default Zone을 사용하는 Clock을 반환합니다.
     *
     * @return System Default Zone Clock
     * @author ryu-qqq
     * @since 2025-11-21
     */
    @Bean("crawlWorkerClock")
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
