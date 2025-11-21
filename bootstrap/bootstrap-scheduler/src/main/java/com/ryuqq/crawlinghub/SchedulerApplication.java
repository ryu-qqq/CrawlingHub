package com.ryuqq.crawlinghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduler application entry point.
 *
 * <p>Background scheduler for periodic tasks:
 *
 * <ul>
 *   <li>OutBox retry processing
 *   <li>EventBridge synchronization
 * </ul>
 *
 * <p>No REST endpoints, only @Scheduled tasks.
 *
 * @author development-team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
