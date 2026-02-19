package com.ryuqq.crawlinghub.integration.config;

import java.time.Clock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for Clock bean.
 *
 * <p>Provides {@link Primary} beans to resolve conflicts between multiple ClockConfig classes from
 * different bootstrap modules:
 *
 * <ul>
 *   <li>bootstrap-web-api: com.ryuqq.crawlinghub.bootstrap.config.ClockConfig
 *   <li>bootstrap-crawl-worker: com.ryuqq.crawlinghub.config.ClockConfig
 *   <li>bootstrap-scheduler: com.ryuqq.crawlinghub.config.ClockConfig
 * </ul>
 *
 * <p>This configuration is automatically imported via {@link TestContainersConfig}.
 *
 * @author development-team
 * @since 1.0.0
 */
@TestConfiguration
public class TestClockConfig {

    /**
     * Primary Clock bean for testing.
     *
     * <p>Uses system default zone clock.
     *
     * @return system default zone clock
     */
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.systemDefaultZone();
    }
}
